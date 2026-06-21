package co.growthmap.alarm.alarm

import co.growthmap.alarm.data.AlarmEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Pure-logic tests for [AlarmScheduler.nextTriggerMillis]. The day-of-week bitmask
 * mapping (bit 0 = Mon .. bit 6 = Sun) and the "already past today" roll-forward are
 * the parts most likely to break, so they are pinned here. No Android framework needed.
 */
class AlarmSchedulerTest {

    private val utc = TimeZone.getTimeZone("UTC")

    /** A fixed "now" so tests are deterministic. Wed 2024-01-10, 08:00 UTC. */
    private fun fixedNow(): Calendar = Calendar.getInstance(utc).apply {
        clear()
        set(2024, Calendar.JANUARY, 10, 8, 0, 0) // 2024-01-10 is a Wednesday
    }

    private fun maskOf(vararg bitIndices: Int): Int =
        bitIndices.fold(0) { acc, i -> acc or (1 shl i) }

    private fun resultCalendar(millis: Long): Calendar =
        Calendar.getInstance(utc).apply { timeInMillis = millis }

    @Test
    fun oneShot_laterToday_firesToday() {
        val alarm = AlarmEntity(hour = 9, minute = 30, repeatDaysMask = 0)
        val millis = AlarmScheduler.nextTriggerMillis(alarm, fixedNow())!!
        val c = resultCalendar(millis)
        assertEquals(10, c.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, c.get(Calendar.MINUTE))
    }

    @Test
    fun oneShot_earlierToday_rollsToTomorrow() {
        val alarm = AlarmEntity(hour = 7, minute = 0, repeatDaysMask = 0) // before 08:00 now
        val millis = AlarmScheduler.nextTriggerMillis(alarm, fixedNow())!!
        val c = resultCalendar(millis)
        assertEquals(11, c.get(Calendar.DAY_OF_MONTH)) // next day
        assertEquals(7, c.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun recurring_today_laterTime_firesToday() {
        // Wednesday = bit index 2 (Mon=0).
        val alarm = AlarmEntity(hour = 9, minute = 0, repeatDaysMask = maskOf(2))
        val millis = AlarmScheduler.nextTriggerMillis(alarm, fixedNow())!!
        val c = resultCalendar(millis)
        assertEquals(Calendar.WEDNESDAY, c.get(Calendar.DAY_OF_WEEK))
        assertEquals(10, c.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun recurring_today_pastTime_rollsToNextMatchingDay() {
        // Only Wednesdays, but 07:00 already passed -> next Wednesday (the 17th).
        val alarm = AlarmEntity(hour = 7, minute = 0, repeatDaysMask = maskOf(2))
        val millis = AlarmScheduler.nextTriggerMillis(alarm, fixedNow())!!
        val c = resultCalendar(millis)
        assertEquals(Calendar.WEDNESDAY, c.get(Calendar.DAY_OF_WEEK))
        assertEquals(17, c.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun recurring_picksNearestEnabledDay() {
        // Enabled Mon(0) and Fri(4). From Wed, nearest future is Fri the 12th.
        val alarm = AlarmEntity(hour = 6, minute = 0, repeatDaysMask = maskOf(0, 4))
        val millis = AlarmScheduler.nextTriggerMillis(alarm, fixedNow())!!
        val c = resultCalendar(millis)
        assertEquals(Calendar.FRIDAY, c.get(Calendar.DAY_OF_WEEK))
        assertEquals(12, c.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun recurring_sundayBitMapsCorrectly() {
        // Sunday = bit index 6. From Wed the 10th, next Sunday is the 14th.
        val alarm = AlarmEntity(hour = 6, minute = 0, repeatDaysMask = maskOf(6))
        val millis = AlarmScheduler.nextTriggerMillis(alarm, fixedNow())!!
        val c = resultCalendar(millis)
        assertEquals(Calendar.SUNDAY, c.get(Calendar.DAY_OF_WEEK))
        assertEquals(14, c.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun recurring_everyDay_firesNextOccurrenceWithinADay() {
        val alarm = AlarmEntity(hour = 9, minute = 0, repeatDaysMask = maskOf(0, 1, 2, 3, 4, 5, 6))
        val millis = AlarmScheduler.nextTriggerMillis(alarm, fixedNow())!!
        assertTrue(millis > fixedNow().timeInMillis)
        val c = resultCalendar(millis)
        assertEquals(10, c.get(Calendar.DAY_OF_MONTH)) // today, 09:00
    }

    @Test
    fun recurring_emptyMaskIsTreatedAsOneShot() {
        // mask 0 => isOneShot => fires today at 09:00.
        val alarm = AlarmEntity(hour = 9, minute = 0, repeatDaysMask = 0)
        val millis = AlarmScheduler.nextTriggerMillis(alarm, fixedNow())!!
        assertEquals(10, resultCalendar(millis).get(Calendar.DAY_OF_MONTH))
    }
}
