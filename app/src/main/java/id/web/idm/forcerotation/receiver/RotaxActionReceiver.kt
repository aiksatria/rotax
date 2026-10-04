package id.web.idm.forcerotation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import id.web.idm.forcerotation.data.RotaxDataStore
import id.web.idm.forcerotation.domain.OrientationController
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.notification.RotaxNotificationManager
import id.web.idm.forcerotation.service.OrientationOverlayService
import id.web.idm.forcerotation.tile.RotaxTileService
import id.web.idm.forcerotation.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RotaxActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SET_PORTRAIT = "id.web.idm.forcerotation.ACTION_SET_PORTRAIT"
        const val ACTION_SET_LANDSCAPE = "id.web.idm.forcerotation.ACTION_SET_LANDSCAPE"
        const val ACTION_SET_AUTO = "id.web.idm.forcerotation.ACTION_SET_AUTO"
        const val ACTION_STOP_FLOAT = "id.web.idm.forcerotation.ACTION_STOP_FLOAT"
    }

    private val receiverScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Logger.i("RotaxActionReceiver received action: $action")

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                val dataStore = RotaxDataStore(context)
                val orientationController = OrientationController(context, dataStore)

                when (action) {
                    ACTION_SET_PORTRAIT -> {
                        orientationController.applyMode(OrientationMode.PORTRAIT)
                    }
                    ACTION_SET_LANDSCAPE -> {
                        orientationController.applyMode(OrientationMode.LANDSCAPE)
                    }
                    ACTION_SET_AUTO -> {
                        orientationController.applyMode(OrientationMode.AUTO)
                    }
                    ACTION_STOP_FLOAT -> {
                        dataStore.setFloatingEnabled(false)
                        OrientationOverlayService.stopService(context)
                    }
                }

                // Update notification and tile if needed
                val selectedMode = dataStore.selectedModeFlow.first()
                val hasOverride = dataStore.hasActiveOverrideFlow.first()
                val isFloating = dataStore.floatingEnabledFlow.first()
                val isNotifEnabled = dataStore.notificationEnabledFlow.first()

                if (isNotifEnabled || isFloating) {
                    RotaxNotificationManager.updateNotification(
                        context,
                        selectedMode,
                        hasOverride,
                        isFloating
                    )
                }

                RotaxTileService.requestListeningState(context)
            } catch (e: Exception) {
                Logger.e("Error handling action in RotaxActionReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
