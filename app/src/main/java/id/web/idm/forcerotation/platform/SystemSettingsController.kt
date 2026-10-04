package id.web.idm.forcerotation.platform

import android.content.Context
import android.provider.Settings
import android.view.Surface
import id.web.idm.forcerotation.util.Logger

object SystemSettingsController {

    fun canWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun getAccelerometerRotation(context: Context): Int {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                1
            )
        } catch (e: Exception) {
            Logger.w("Failed to read ACCELEROMETER_ROTATION", e)
            1
        }
    }

    fun getUserRotation(context: Context): Int {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.USER_ROTATION,
                Surface.ROTATION_0
            )
        } catch (e: Exception) {
            Logger.w("Failed to read USER_ROTATION", e)
            Surface.ROTATION_0
        }
    }

    /**
     * Writes accelerometer and user rotation settings and verifies their values.
     * Returns true only if both settings were successfully written and verified.
     */
    fun writeRotation(
        context: Context,
        accelerometerRotation: Int,
        userRotation: Int
    ): Result<Boolean> {
        if (!canWriteSettings(context)) {
            return Result.failure(SecurityException("Izin ubah setelan sistem diperlukan."))
        }

        return try {
            val contentResolver = context.contentResolver

            // Write USER_ROTATION first when disabling accelerometer so system rotates to target immediately
            val writeUserRotSuccess = Settings.System.putInt(
                contentResolver,
                Settings.System.USER_ROTATION,
                userRotation
            )

            val writeAccelSuccess = Settings.System.putInt(
                contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                accelerometerRotation
            )

            // Verify immediately
            val readAccel = getAccelerometerRotation(context)
            val readUserRot = getUserRotation(context)

            val verified = (readAccel == accelerometerRotation) &&
                    (accelerometerRotation == 1 || readUserRot == userRotation)

            if (verified) {
                Logger.i("Orientation applied successfully: accel=$readAccel, userRot=$readUserRot")
                Result.success(true)
            } else {
                Logger.w("System did not accept requested values: expected(accel=$accelerometerRotation, rot=$userRotation), got(accel=$readAccel, rot=$readUserRot)")
                Result.failure(IllegalStateException("Android menolak perubahan orientasi pada kondisi perangkat saat ini."))
            }
        } catch (se: SecurityException) {
            Logger.e("SecurityException writing settings", se)
            Result.failure(se)
        } catch (e: Exception) {
            Logger.e("Exception writing rotation settings", e)
            Result.failure(e)
        }
    }
}
