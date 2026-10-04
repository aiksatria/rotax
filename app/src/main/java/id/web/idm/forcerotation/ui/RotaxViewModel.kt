package id.web.idm.forcerotation.ui

import android.app.Application
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.web.idm.forcerotation.data.RotaxDataStore
import id.web.idm.forcerotation.domain.OrientationController
import id.web.idm.forcerotation.domain.OrientationDiagnostics
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.domain.OrientationSnapshot
import id.web.idm.forcerotation.domain.OverrideStatus
import id.web.idm.forcerotation.domain.RotaxUiState
import id.web.idm.forcerotation.notification.RotaxNotificationManager
import id.web.idm.forcerotation.platform.NotificationPermissionController
import id.web.idm.forcerotation.platform.OverlayPermissionController
import id.web.idm.forcerotation.platform.RotationResolver
import id.web.idm.forcerotation.platform.SystemSettingsController
import id.web.idm.forcerotation.service.OrientationOverlayService
import id.web.idm.forcerotation.tile.RotaxTileService
import id.web.idm.forcerotation.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RotaxViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val dataStore = RotaxDataStore(context)
    val orientationController = OrientationController(context, dataStore)

    private val _uiState = MutableStateFlow(RotaxUiState())
    val uiState: StateFlow<RotaxUiState> = _uiState.asStateFlow()

    private val systemSettingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            refreshState()
        }
    }

    init {
        observeSettings()
        registerSettingsObserver()
    }

    private fun registerSettingsObserver() {
        try {
            val resolver = context.contentResolver
            resolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                false,
                systemSettingsObserver
            )
            resolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.USER_ROTATION),
                false,
                systemSettingsObserver
            )
        } catch (e: Exception) {
            Logger.e("Failed to register settings ContentObserver", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.contentResolver.unregisterContentObserver(systemSettingsObserver)
        } catch (e: Exception) {
            Logger.e("Failed to unregister settings ContentObserver", e)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                dataStore.selectedModeFlow,
                dataStore.hasActiveOverrideFlow,
                dataStore.floatingEnabledFlow,
                dataStore.notificationEnabledFlow
            ) { mode, hasOverride, floating, notif ->
                val safeRestore = dataStore.safeRestoreEnabledFlow.first()
                val isFirst = !dataStore.firstRunCompletedFlow.first()
                refreshInternalState(mode, hasOverride, floating, notif, safeRestore, isFirst)
            }.collect { }
        }
    }

    fun refreshState() {
        viewModelScope.launch {
            val mode = dataStore.selectedModeFlow.first()
            val hasOverride = dataStore.hasActiveOverrideFlow.first()
            val floating = dataStore.floatingEnabledFlow.first()
            val notif = dataStore.notificationEnabledFlow.first()
            val safeRestore = dataStore.safeRestoreEnabledFlow.first()
            val isFirst = !dataStore.firstRunCompletedFlow.first()

            refreshInternalState(mode, hasOverride, floating, notif, safeRestore, isFirst)
        }
    }

    private suspend fun refreshInternalState(
        mode: OrientationMode,
        hasOverride: Boolean,
        floating: Boolean,
        notification: Boolean,
        safeRestore: Boolean,
        isFirstRun: Boolean
    ) {
        val canWrite = SystemSettingsController.canWriteSettings(context)
        val canOverlay = OverlayPermissionController.canDrawOverlays(context)
        val canNotify = NotificationPermissionController.hasNotificationPermission(context)

        val actualAccel = SystemSettingsController.getAccelerometerRotation(context)
        val actualUserRot = SystemSettingsController.getUserRotation(context)
        val degrees = RotationResolver.rotationToDegrees(actualUserRot)
        val resolved = RotationResolver.detect(context)
        val ownership = dataStore.getOwnershipRecord()
        val snapshot = dataStore.snapshotFlow.first()

        val overrideStatus = when {
            !canWrite -> OverrideStatus.PERMISSION_REQUIRED
            hasOverride -> {
                // Verify whether system settings still match last written value
                if (ownership.lastWrittenAccelerometer != null &&
                    (actualAccel != ownership.lastWrittenAccelerometer ||
                            (actualAccel == 0 && actualUserRot != ownership.lastWrittenUserRotation))
                ) {
                    OverrideStatus.EXTERNALLY_MODIFIED
                } else {
                    OverrideStatus.ACTIVE
                }
            }
            mode == OrientationMode.AUTO -> OverrideStatus.INACTIVE
            else -> OverrideStatus.INACTIVE
        }

        val diagnostics = OrientationDiagnostics(
            actualAccelerometerRotation = actualAccel,
            actualUserRotation = actualUserRot,
            actualRotationDegrees = degrees,
            isRotaxOwner = hasOverride && overrideStatus == OverrideStatus.ACTIVE,
            canWriteSettings = canWrite,
            canDrawOverlays = canOverlay,
            canPostNotifications = canNotify,
            naturalOrientation = resolved.naturalOrientation.name,
            lastSnapshot = snapshot
        )

        _uiState.update { current ->
            current.copy(
                mode = mode,
                currentRotation = actualUserRot,
                currentRotationDegrees = degrees,
                overrideStatus = overrideStatus,
                canWriteSettings = canWrite,
                canDrawOverlays = canOverlay,
                canPostNotifications = canNotify,
                floatingEnabled = floating,
                notificationEnabled = notification,
                safeRestoreEnabled = safeRestore,
                diagnostics = diagnostics,
                isFirstRun = isFirstRun
            )
        }
    }

    fun applyMode(mode: OrientationMode) {
        viewModelScope.launch {
            if (!SystemSettingsController.canWriteSettings(context)) {
                _uiState.update {
                    it.copy(
                        errorMessage = "ROTAX belum memiliki izin mengubah setelan sistem."
                    )
                }
                return@launch
            }

            val result = orientationController.applyMode(mode)
            if (result.isSuccess) {
                _uiState.update { it.copy(errorMessage = null) }
                syncBackgroundControls(mode)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Android menolak perubahan orientasi pada kondisi perangkat saat ini."
                _uiState.update { it.copy(errorMessage = errorMsg) }
            }
            refreshState()
        }
    }

    fun toggleFloating(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !OverlayPermissionController.canDrawOverlays(context)) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Overlay belum diizinkan. Aktifkan izin Tampilkan di atas aplikasi lain."
                    )
                }
                return@launch
            }

            dataStore.setFloatingEnabled(enabled)
            if (enabled) {
                OrientationOverlayService.startService(context)
            } else {
                OrientationOverlayService.stopService(context)
            }
            refreshState()
        }
    }

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !NotificationPermissionController.hasNotificationPermission(context)) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Notifikasi belum diizinkan. Berikan izin notifikasi untuk persistent control."
                    )
                }
                return@launch
            }

            dataStore.setNotificationEnabled(enabled)
            val currentMode = dataStore.selectedModeFlow.first()
            val hasOverride = dataStore.hasActiveOverrideFlow.first()
            val isFloating = dataStore.floatingEnabledFlow.first()

            if (enabled) {
                RotaxNotificationManager.updateNotification(
                    context,
                    currentMode,
                    hasOverride,
                    isFloating
                )
            } else if (!isFloating) {
                RotaxNotificationManager.cancelNotification(context)
            }
            refreshState()
        }
    }

    fun emergencyRestore() {
        viewModelScope.launch {
            val result = orientationController.emergencyRestoreDefault()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        errorMessage = null,
                        statusMessage = "Setelan sistem berhasil dipulihkan ke default Auto-Rotate."
                    )
                }
                syncBackgroundControls(OrientationMode.AUTO)
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Gagal memulihkan setelan sistem."
                    )
                }
            }
            refreshState()
        }
    }

    fun resetFloatingPosition() {
        viewModelScope.launch {
            dataStore.saveFloatingPosition(20, 300)
            if (dataStore.floatingEnabledFlow.first()) {
                OrientationOverlayService.stopService(context)
                OrientationOverlayService.startService(context)
            }
        }
    }

    fun setSafeRestore(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setSafeRestoreEnabled(enabled)
        }
    }

    fun setDarkTheme(theme: String) {
        viewModelScope.launch {
            dataStore.setDarkTheme(theme)
        }
    }

    fun completeFirstRun() {
        viewModelScope.launch {
            dataStore.setFirstRunCompleted(true)
            _uiState.update { it.copy(isFirstRun = false) }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun syncBackgroundControls(mode: OrientationMode) {
        val hasOverride = mode != OrientationMode.AUTO
        val isFloating = dataStore.floatingEnabledFlow.first()
        val isNotif = dataStore.notificationEnabledFlow.first()

        if (isNotif || isFloating) {
            RotaxNotificationManager.updateNotification(
                context,
                mode,
                hasOverride,
                isFloating
            )
        }
        RotaxTileService.requestListeningState(context)
    }
}
