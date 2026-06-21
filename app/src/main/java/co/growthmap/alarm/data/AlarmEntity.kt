package co.growthmap.alarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One configured alarm (ADR 0006: multiple recurring alarms, scan-existing target).
 *
 * @param repeatDaysMask bitmask of days; bit 0 = Monday .. bit 6 = Sunday.
 *        0 means a one-shot alarm.
 * @param targetCode the registered barcode value (the "particular object"). Null until configured.
 * @param requireCharger whether the Charging Gate applies (ADR 0003). Default true.
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String = "",
    val hour: Int,
    val minute: Int,
    val repeatDaysMask: Int = 0,
    val enabled: Boolean = true,
    val targetCode: String? = null,
    val targetLabel: String? = null,
    val requireCharger: Boolean = true,
) {
    val isOneShot: Boolean get() = repeatDaysMask == 0
    val isConfigured: Boolean get() = !targetCode.isNullOrBlank()
}

object Weekdays {
    /** bit index 0..6 -> Mon..Sun */
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    fun isSet(mask: Int, dayIndex: Int) = (mask shr dayIndex) and 1 == 1
    fun toggle(mask: Int, dayIndex: Int) = mask xor (1 shl dayIndex)
}
