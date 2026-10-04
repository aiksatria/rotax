package id.web.idm.forcerotation.domain

import android.content.Context
import id.web.idm.forcerotation.data.RotaxDataStore
import id.web.idm.forcerotation.platform.NaturalOrientation
import id.web.idm.forcerotation.platform.RotationResolver
import id.web.idm.forcerotation.platform.SystemSettingsController
import id.web.idm.forcerotation.util.Logger

class OrientationController(
    private val context: Context,
    private val dataStore: RotaxDataStore
) {

    data class SystemStateSnapshot(
        val accelerometer: Int,
        val userRotation: Int,
        val isAutoRotate: Boolean,
        val rotationDegrees: Int
    )

    fun getCurrentSystemState(): SystemStateSnapshot {
        val accel = SystemSettingsController.getAccelerometerRotation(context)
        val userRot = SystemSettingsController.getUserRotation(context)
        return SystemStateSnapshot(
            accelerometer = accel,
            userRotation = userRot,
            isAutoRotate = accel == 1,
            rotationDegrees = RotationResolver.rotationToDegrees(userRot)
        )
    }

    suspend fun applyMode(mode: OrientationMode): Result<Unit> {
        if (!SystemSettingsController.canWriteSettings(context)) {
            return Result.failure(
                SecurityException("ROTAX belum memiliki izin mengubah setelan sistem.")
            )
        }

        val resolved = RotationResolver.detect(context)
        val currentState = getCurrentSystemState()

        when (mode) {
            OrientationMode.PORTRAIT -> {
                // If override was not active, take snapshot of original settings first
                val ownership = dataStore.getOwnershipRecord()
                if (!ownership.hasActiveOverride) {
                    val snapshot = OrientationSnapshot(
                        accelerometerRotation = currentState.accelerometer,
                        userRotation = currentState.userRotation,
                        timestamp = System.currentTimeMillis()
                    )
                    dataStore.saveSnapshot(snapshot)
                    Logger.i("Captured original snapshot before Portrait: $snapshot")
                }

                val targetRotation = resolved.primaryPortrait
                val writeResult = SystemSettingsController.writeRotation(
                    context = context,
                    accelerometerRotation = 0,
                    userRotation = targetRotation
                )

                if (writeResult.isSuccess) {
                    dataStore.saveLastWritten(0, targetRotation)
                    dataStore.saveSelectedMode(OrientationMode.PORTRAIT)
                    return Result.success(Unit)
                } else {
                    return Result.failure(
                        writeResult.exceptionOrNull()
                            ?: IllegalStateException("Android menolak perubahan orientasi Portrait.")
                    )
                }
            }

            OrientationMode.LANDSCAPE -> {
                val ownership = dataStore.getOwnershipRecord()
                if (!ownership.hasActiveOverride) {
                    val snapshot = OrientationSnapshot(
                        accelerometerRotation = currentState.accelerometer,
                        userRotation = currentState.userRotation,
                        timestamp = System.currentTimeMillis()
                    )
                    dataStore.saveSnapshot(snapshot)
                    Logger.i("Captured original snapshot before Landscape: $snapshot")
                }

                val targetRotation = resolved.primaryLandscape
                val writeResult = SystemSettingsController.writeRotation(
                    context = context,
                    accelerometerRotation = 0,
                    userRotation = targetRotation
                )

                if (writeResult.isSuccess) {
                    dataStore.saveLastWritten(0, targetRotation)
                    dataStore.saveSelectedMode(OrientationMode.LANDSCAPE)
                    return Result.success(Unit)
                } else {
                    return Result.failure(
                        writeResult.exceptionOrNull()
                            ?: IllegalStateException("Android menolak perubahan orientasi Landscape.")
                    )
                }
            }

            OrientationMode.AUTO -> {
                return restoreOriginalSettings()
            }
        }
    }

    /**
     * Safe restore with compare-before-restore logic:
     * - If current system settings match what ROTAX last wrote: restore original settings.
     * - If current settings changed outside ROTAX: do not blindly overwrite, release ownership.
     */
    suspend fun restoreOriginalSettings(): Result<Unit> {
        if (!SystemSettingsController.canWriteSettings(context)) {
            dataStore.saveSelectedMode(OrientationMode.AUTO)
            dataStore.clearOverrideOwnership()
            return Result.failure(SecurityException("ROTAX belum memiliki izin mengubah setelan sistem."))
        }

        val ownership = dataStore.getOwnershipRecord()
        val currentAccel = SystemSettingsController.getAccelerometerRotation(context)
        val currentUserRot = SystemSettingsController.getUserRotation(context)

        if (ownership.hasActiveOverride && ownership.lastWrittenAccelerometer != null) {
            val settingsStillMatchRotax = (currentAccel == ownership.lastWrittenAccelerometer) &&
                    (ownership.lastWrittenAccelerometer == 1 || currentUserRot == ownership.lastWrittenUserRotation)

            if (!settingsStillMatchRotax) {
                Logger.i("System state changed externally outside ROTAX. Releasing ownership without overwrite.")
                dataStore.clearOverrideOwnership()
                dataStore.saveSelectedMode(OrientationMode.AUTO)
                return Result.failure(
                    IllegalStateException("System state berubah di luar ROTAX. ROTAX tidak menimpa perubahan tersebut.")
                )
            }

            // Restore original settings
            val targetAccel = ownership.originalAccelerometer ?: 1
            val targetUserRot = ownership.originalUserRotation ?: 0

            val result = SystemSettingsController.writeRotation(
                context = context,
                accelerometerRotation = targetAccel,
                userRotation = targetUserRot
            )

            dataStore.clearOverrideOwnership()
            dataStore.saveSelectedMode(OrientationMode.AUTO)

            return if (result.isSuccess) {
                Logger.i("Restored original orientation settings: accel=$targetAccel, userRot=$targetUserRot")
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: IllegalStateException("Gagal memulihkan setelan sistem."))
            }
        } else {
            // No active snapshot stored, set to standard Auto-rotate
            val result = SystemSettingsController.writeRotation(
                context = context,
                accelerometerRotation = 1,
                userRotation = currentUserRot
            )
            dataStore.clearOverrideOwnership()
            dataStore.saveSelectedMode(OrientationMode.AUTO)
            return Result.success(Unit)
        }
    }

    /**
     * Emergency restore: sets ACCELEROMETER_ROTATION = 1 (System Auto Rotate) and clears ownership.
     */
    suspend fun emergencyRestoreDefault(): Result<Unit> {
        if (!SystemSettingsController.canWriteSettings(context)) {
            return Result.failure(SecurityException("ROTAX belum memiliki izin mengubah setelan sistem."))
        }
        val currentRot = SystemSettingsController.getUserRotation(context)
        val result = SystemSettingsController.writeRotation(
            context = context,
            accelerometerRotation = 1,
            userRotation = currentRot
        )
        dataStore.clearOverrideOwnership()
        dataStore.saveSelectedMode(OrientationMode.AUTO)
        return result.map { }
    }
}
