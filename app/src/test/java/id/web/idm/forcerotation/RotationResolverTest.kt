package id.web.idm.forcerotation

import android.view.Surface
import id.web.idm.forcerotation.platform.NaturalOrientation
import id.web.idm.forcerotation.platform.RotationResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class RotationResolverTest {

    @Test
    fun testRotationToDegrees() {
        assertEquals(0, RotationResolver.rotationToDegrees(Surface.ROTATION_0))
        assertEquals(90, RotationResolver.rotationToDegrees(Surface.ROTATION_90))
        assertEquals(180, RotationResolver.rotationToDegrees(Surface.ROTATION_180))
        assertEquals(270, RotationResolver.rotationToDegrees(Surface.ROTATION_270))
    }

    @Test
    fun testDegreesToRotation() {
        assertEquals(Surface.ROTATION_0, RotationResolver.degreesToRotation(0))
        assertEquals(Surface.ROTATION_90, RotationResolver.degreesToRotation(90))
        assertEquals(Surface.ROTATION_180, RotationResolver.degreesToRotation(180))
        assertEquals(Surface.ROTATION_270, RotationResolver.degreesToRotation(270))
        assertEquals(Surface.ROTATION_0, RotationResolver.degreesToRotation(360))
    }

    @Test
    fun testResolveNaturalOrientation_PhonePortrait() {
        // Standard phone held upright: 1080x2400 at ROTATION_0
        val natural = RotationResolver.resolveNaturalOrientation(
            currentSurfaceRotation = Surface.ROTATION_0,
            widthPx = 1080,
            heightPx = 2400
        )
        assertEquals(NaturalOrientation.PORTRAIT, natural)

        val resolved = RotationResolver.getResolvedRotations(natural)
        assertEquals(Surface.ROTATION_0, resolved.primaryPortrait)
        assertEquals(Surface.ROTATION_180, resolved.reversePortrait)
        assertEquals(Surface.ROTATION_90, resolved.primaryLandscape)
        assertEquals(Surface.ROTATION_270, resolved.reverseLandscape)
    }

    @Test
    fun testResolveNaturalOrientation_PhoneRotatedLandscape() {
        // Phone rotated sideways: 2400x1080 at ROTATION_90
        val natural = RotationResolver.resolveNaturalOrientation(
            currentSurfaceRotation = Surface.ROTATION_90,
            widthPx = 2400,
            heightPx = 1080
        )
        assertEquals(NaturalOrientation.PORTRAIT, natural)
    }

    @Test
    fun testResolveNaturalOrientation_TabletLandscape() {
        // Tablet held naturally in landscape: 2560x1600 at ROTATION_0
        val natural = RotationResolver.resolveNaturalOrientation(
            currentSurfaceRotation = Surface.ROTATION_0,
            widthPx = 2560,
            heightPx = 1600
        )
        assertEquals(NaturalOrientation.LANDSCAPE, natural)

        val resolved = RotationResolver.getResolvedRotations(natural)
        assertEquals(Surface.ROTATION_90, resolved.primaryPortrait)
        assertEquals(Surface.ROTATION_270, resolved.reversePortrait)
        assertEquals(Surface.ROTATION_0, resolved.primaryLandscape)
        assertEquals(Surface.ROTATION_180, resolved.reverseLandscape)
    }

    @Test
    fun testResolveNaturalOrientation_SquareDisplay() {
        val natural = RotationResolver.resolveNaturalOrientation(
            currentSurfaceRotation = Surface.ROTATION_0,
            widthPx = 1080,
            heightPx = 1080
        )
        assertEquals(NaturalOrientation.SQUARE, natural)
    }
}
