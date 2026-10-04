package id.web.idm.forcerotation.platform

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import id.web.idm.forcerotation.util.Logger

enum class NaturalOrientation {
    PORTRAIT,
    LANDSCAPE,
    SQUARE
}

data class ResolvedRotations(
    val primaryPortrait: Int,
    val reversePortrait: Int,
    val primaryLandscape: Int,
    val reverseLandscape: Int,
    val naturalOrientation: NaturalOrientation
)

object RotationResolver {

    fun rotationToDegrees(surfaceRotation: Int): Int = when (surfaceRotation) {
        Surface.ROTATION_0 -> 0
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }

    fun degreesToRotation(degrees: Int): Int = when (degrees % 360) {
        90 -> Surface.ROTATION_90
        180 -> Surface.ROTATION_180
        270 -> Surface.ROTATION_270
        else -> Surface.ROTATION_0
    }

    /**
     * Resolves natural orientation given current surface rotation and width/height dimensions.
     * Pure function suitable for unit tests.
     */
    fun resolveNaturalOrientation(
        currentSurfaceRotation: Int,
        widthPx: Int,
        heightPx: Int
    ): NaturalOrientation {
        if (widthPx == heightPx) return NaturalOrientation.SQUARE

        val isCurrentlyWide = widthPx > heightPx
        val isRotatedQuarter = currentSurfaceRotation == Surface.ROTATION_90 ||
                currentSurfaceRotation == Surface.ROTATION_270

        val naturalIsWide = if (isRotatedQuarter) !isCurrentlyWide else isCurrentlyWide
        return if (naturalIsWide) NaturalOrientation.LANDSCAPE else NaturalOrientation.PORTRAIT
    }

    /**
     * Calculates the portrait and landscape Surface rotation constants
     * for the detected natural orientation.
     */
    fun getResolvedRotations(naturalOrientation: NaturalOrientation): ResolvedRotations {
        return when (naturalOrientation) {
            NaturalOrientation.LANDSCAPE -> ResolvedRotations(
                primaryPortrait = Surface.ROTATION_90,
                reversePortrait = Surface.ROTATION_270,
                primaryLandscape = Surface.ROTATION_0,
                reverseLandscape = Surface.ROTATION_180,
                naturalOrientation = naturalOrientation
            )
            NaturalOrientation.PORTRAIT, NaturalOrientation.SQUARE -> ResolvedRotations(
                primaryPortrait = Surface.ROTATION_0,
                reversePortrait = Surface.ROTATION_180,
                primaryLandscape = Surface.ROTATION_90,
                reverseLandscape = Surface.ROTATION_270,
                naturalOrientation = naturalOrientation
            )
        }
    }

    /**
     * Detects current device orientation and metrics from Context.
     */
    fun detect(context: Context): ResolvedRotations {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val displayRotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.display.rotation
                } catch (e: Exception) {
                    @Suppress("DEPRECATION")
                    windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
                }
            } else {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }

            val metrics = context.resources.displayMetrics
            val natural = resolveNaturalOrientation(
                currentSurfaceRotation = displayRotation,
                widthPx = metrics.widthPixels,
                heightPx = metrics.heightPixels
            )
            getResolvedRotations(natural)
        } catch (e: Exception) {
            Logger.w("Failed to detect display metrics, defaulting to standard portrait", e)
            getResolvedRotations(NaturalOrientation.PORTRAIT)
        }
    }
}
