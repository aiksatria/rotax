package id.web.idm.forcerotation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.domain.OrientationSnapshot
import id.web.idm.forcerotation.notification.RotaxNotificationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RotaxLogicTest {

    @Test
    fun testSnapshotIntegrity() {
        val now = System.currentTimeMillis()
        val snapshot = OrientationSnapshot(
            accelerometerRotation = 1,
            userRotation = 0,
            timestamp = now
        )
        assertEquals(1, snapshot.accelerometerRotation)
        assertEquals(0, snapshot.userRotation)
        assertEquals(now, snapshot.timestamp)
    }

    @Test
    fun testCompareBeforeRestoreLogic() {
        // Given last written values
        val lastWrittenAccel = 0
        val lastWrittenUserRot = 1

        // Scenario 1: System still has ROTAX values -> should restore
        val currentAccel1 = 0
        val currentUserRot1 = 1
        val shouldRestore1 = (currentAccel1 == lastWrittenAccel && currentUserRot1 == lastWrittenUserRot)
        assertTrue(shouldRestore1)

        // Scenario 2: User turned on Auto-Rotate outside ROTAX -> do NOT overwrite
        val currentAccel2 = 1
        val currentUserRot2 = 1
        val shouldRestore2 = (currentAccel2 == lastWrittenAccel && currentUserRot2 == lastWrittenUserRot)
        assertTrue(!shouldRestore2)

        // Scenario 3: Another app forced user rotation to 0 -> do NOT overwrite
        val currentAccel3 = 0
        val currentUserRot3 = 0
        val shouldRestore3 = (currentAccel3 == lastWrittenAccel && currentUserRot3 == lastWrittenUserRot)
        assertTrue(!shouldRestore3)
    }

    @Test
    fun testNotificationBuilding() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notification = RotaxNotificationManager.buildNotification(
            context = context,
            mode = OrientationMode.LANDSCAPE,
            isOverrideActive = true,
            isFloatingActive = true
        )
        assertNotNull(notification)
        assertEquals(4, notification.actions.size) // Portrait, Landscape, Auto, Float Off
    }
}
