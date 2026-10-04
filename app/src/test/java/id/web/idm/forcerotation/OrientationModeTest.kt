package id.web.idm.forcerotation

import id.web.idm.forcerotation.domain.OrientationMode
import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationModeTest {

    @Test
    fun testModeCycle() {
        assertEquals(OrientationMode.PORTRAIT, OrientationMode.AUTO.next())
        assertEquals(OrientationMode.LANDSCAPE, OrientationMode.PORTRAIT.next())
        assertEquals(OrientationMode.AUTO, OrientationMode.LANDSCAPE.next())
    }

    @Test
    fun testDisplayNames() {
        assertEquals("Auto", OrientationMode.AUTO.displayName)
        assertEquals("Portrait", OrientationMode.PORTRAIT.displayName)
        assertEquals("Landscape", OrientationMode.LANDSCAPE.displayName)
    }
}
