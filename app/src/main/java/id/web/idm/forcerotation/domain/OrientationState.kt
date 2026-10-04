package id.web.idm.forcerotation.domain

data class OrientationSnapshot(
    val accelerometerRotation: Int,
    val userRotation: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class OrientationDiagnostics(
    val actualAccelerometerRotation: Int,
    val actualUserRotation: Int,
    val actualRotationDegrees: Int,
    val isRotaxOwner: Boolean,
    val canWriteSettings: Boolean,
    val canDrawOverlays: Boolean,
    val canPostNotifications: Boolean,
    val naturalOrientation: String,
    val lastSnapshot: OrientationSnapshot?
)

enum class OverrideStatus {
    INACTIVE,
    ACTIVE,
    PERMISSION_REQUIRED,
    SYSTEM_REJECTED,
    EXTERNALLY_MODIFIED
}

data class RotaxUiState(
    val mode: OrientationMode = OrientationMode.AUTO,
    val currentRotation: Int = 0,
    val currentRotationDegrees: Int = 0,
    val overrideStatus: OverrideStatus = OverrideStatus.INACTIVE,
    val canWriteSettings: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val canPostNotifications: Boolean = false,
    val floatingEnabled: Boolean = false,
    val notificationEnabled: Boolean = false,
    val safeRestoreEnabled: Boolean = true,
    val statusMessage: String = "",
    val errorMessage: String? = null,
    val diagnostics: OrientationDiagnostics? = null,
    val isFirstRun: Boolean = false
)
