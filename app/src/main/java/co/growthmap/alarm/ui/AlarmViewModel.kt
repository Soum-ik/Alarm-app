package co.growthmap.alarm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.growthmap.alarm.alarm.AlarmScheduler
import co.growthmap.alarm.data.AlarmEntity
import co.growthmap.alarm.data.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AlarmRepository.from(app)

    val alarms: StateFlow<List<AlarmEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun load(id: Long): AlarmEntity? = repo.getById(id)

    fun save(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = repo.upsert(alarm)
            val saved = alarm.copy(id = id)
            AlarmScheduler.reschedule(getApplication(), saved)
        }
    }

    fun toggle(alarm: AlarmEntity, enabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(enabled = enabled)
            repo.upsert(updated)
            AlarmScheduler.reschedule(getApplication(), updated)
        }
    }

    fun delete(alarm: AlarmEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancel(getApplication(), alarm.id)
            repo.delete(alarm)
        }
    }
}
