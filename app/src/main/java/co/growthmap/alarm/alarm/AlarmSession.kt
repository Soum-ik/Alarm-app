package co.growthmap.alarm.alarm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for the currently-ringing Alarm Session, shared between
 * AlarmService (sound, charging poll, backstop timer) and RingActivity (UI, scan).
 *
 * Only one session is active at a time; if alarms collide, the later one wins the UI
 * but the service keeps a single ringing state.
 */
object AlarmSession {
    const val BACKSTOP_MILLIS = 30 * 60 * 1000L // ADR 0005

    data class State(
        val alarmId: Long = -1L,
        val targetCode: String? = null,
        val requireCharger: Boolean = true,
        val label: String = "",
        val charging: Boolean = false,
        val scanned: Boolean = false,
        val elapsedMillis: Long = 0L,
    ) {
        val backstopAvailable: Boolean get() = elapsedMillis >= BACKSTOP_MILLIS
        // Dismissal Conditions (glossary): scan AND (charger satisfied or not required).
        val canDismiss: Boolean get() = scanned && (charging || !requireCharger)
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun update(transform: (State) -> State) { _state.value = transform(_state.value) }
    fun reset() { _state.value = State() }
}
