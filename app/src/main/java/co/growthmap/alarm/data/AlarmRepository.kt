package co.growthmap.alarm.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Thin repository over the DAO so UI + scheduling share one access point. */
class AlarmRepository(private val dao: AlarmDao) {

    fun observeAll(): Flow<List<AlarmEntity>> = dao.observeAll()
    suspend fun getEnabled(): List<AlarmEntity> = dao.getEnabled()
    suspend fun getById(id: Long): AlarmEntity? = dao.getById(id)
    suspend fun upsert(alarm: AlarmEntity): Long =
        if (alarm.id == 0L) dao.insert(alarm) else { dao.update(alarm); alarm.id }
    suspend fun delete(alarm: AlarmEntity) = dao.delete(alarm)

    companion object {
        fun from(context: Context) =
            AlarmRepository(AlarmDatabase.get(context).alarmDao())
    }
}
