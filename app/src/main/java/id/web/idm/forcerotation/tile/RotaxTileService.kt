package id.web.idm.forcerotation.tile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import id.web.idm.forcerotation.MainActivity
import id.web.idm.forcerotation.R
import id.web.idm.forcerotation.data.RotaxDataStore
import id.web.idm.forcerotation.domain.OrientationController
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.notification.RotaxNotificationManager
import id.web.idm.forcerotation.platform.SystemSettingsController
import id.web.idm.forcerotation.util.IntentUtils
import id.web.idm.forcerotation.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RotaxTileService : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var dataStore: RotaxDataStore
    private lateinit var orientationController: OrientationController

    override fun onCreate() {
        super.onCreate()
        dataStore = RotaxDataStore(applicationContext)
        orientationController = OrientationController(applicationContext, dataStore)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!SystemSettingsController.canWriteSettings(applicationContext)) {
            // Permission missing: open write settings page rather than faking success
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = android.app.PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        serviceScope.launch {
            try {
                val currentMode = dataStore.selectedModeFlow.first()
                val nextMode = currentMode.next()

                val result = orientationController.applyMode(nextMode)
                if (result.isSuccess) {
                    val isOverride = nextMode != OrientationMode.AUTO
                    val isFloating = dataStore.floatingEnabledFlow.first()
                    val isNotifEnabled = dataStore.notificationEnabledFlow.first()

                    if (isNotifEnabled || isFloating) {
                        RotaxNotificationManager.updateNotification(
                            applicationContext,
                            nextMode,
                            isOverride,
                            isFloating
                        )
                    }
                }
                updateTileState()
            } catch (e: Exception) {
                Logger.e("Error cycling mode in tile", e)
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        serviceScope.launch {
            try {
                val canWrite = SystemSettingsController.canWriteSettings(applicationContext)
                val currentMode = dataStore.selectedModeFlow.first()
                val hasOverride = dataStore.hasActiveOverrideFlow.first()

                tile.label = getString(R.string.tile_label)
                tile.icon = Icon.createWithResource(applicationContext, R.drawable.ic_rotax_tile)

                if (!canWrite) {
                    tile.state = Tile.STATE_UNAVAILABLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        tile.subtitle = "Izin Diperlukan"
                    }
                } else {
                    when (currentMode) {
                        OrientationMode.AUTO -> {
                            tile.state = Tile.STATE_INACTIVE
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                tile.subtitle = "Auto Rotate"
                            }
                        }
                        OrientationMode.PORTRAIT -> {
                            tile.state = Tile.STATE_ACTIVE
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                tile.subtitle = "Portrait"
                            }
                        }
                        OrientationMode.LANDSCAPE -> {
                            tile.state = Tile.STATE_ACTIVE
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                tile.subtitle = "Landscape"
                            }
                        }
                    }
                }
                tile.updateTile()
            } catch (e: Exception) {
                Logger.e("Error updating tile state", e)
            }
        }
    }

    companion object {
        fun requestListeningState(context: Context) {
            try {
                requestListeningState(
                    context,
                    ComponentName(context, RotaxTileService::class.java)
                )
            } catch (e: Exception) {
                Logger.d("TileService requestListeningState not supported or failed: ${e.message}")
            }
        }
    }
}
