package id.web.idm.forcerotation.platform

import android.content.Context
import android.os.Build
import android.provider.Settings

object OverlayPermissionController {

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}
