package co.growthmap.alarm.alarm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the Dismissal Conditions: the alarm completes ONLY when the object scan
 * matched (scanned = true) AND the charging requirement is satisfied. These mirror
 * the runtime gate in RingActivity + AlarmSession.State.canDismiss.
 */
class AlarmSessionTest {

    private fun state(
        targetCode: String? = "MILK-123",
        scanned: Boolean = false,
        charging: Boolean = false,
        requireCharger: Boolean = true,
    ) = AlarmSession.State(
        alarmId = 1L,
        targetCode = targetCode,
        requireCharger = requireCharger,
        scanned = scanned,
        charging = charging,
    )

    // --- The scan-match rule (as enforced in RingActivity) ---

    private fun scanMatches(target: String?, scanned: String): Boolean =
        !target.isNullOrBlank() && scanned == target

    @Test
    fun correctCode_matches() {
        assertTrue(scanMatches("MILK-123", "MILK-123"))
    }

    @Test
    fun wrongCode_doesNotMatch() {
        assertFalse(scanMatches("MILK-123", "SOAP-999"))
    }

    @Test
    fun nullTarget_neverMatches() {
        assertFalse(scanMatches(null, "MILK-123"))
        assertFalse(scanMatches("", "MILK-123"))
    }

    // --- The completion rule (canDismiss) ---

    @Test
    fun notDismissed_whenNothingDone() {
        assertFalse(state().canDismiss)
    }

    @Test
    fun notDismissed_whenScannedButNotCharging() {
        assertFalse(state(scanned = true, charging = false).canDismiss)
    }

    @Test
    fun notDismissed_whenChargingButNotScanned() {
        assertFalse(state(scanned = false, charging = true).canDismiss)
    }

    @Test
    fun dismissed_whenScannedAndCharging() {
        assertTrue(state(scanned = true, charging = true).canDismiss)
    }

    @Test
    fun dismissed_whenScanned_andChargerNotRequired() {
        // If the user turned the charger gate off, a match alone completes it.
        assertTrue(state(scanned = true, charging = false, requireCharger = false).canDismiss)
    }

    @Test
    fun notDismissed_whenChargerNotRequiredButNotScanned() {
        assertFalse(state(scanned = false, requireCharger = false).canDismiss)
    }
}
