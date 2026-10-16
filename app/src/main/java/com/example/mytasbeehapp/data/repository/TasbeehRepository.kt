package com.example.mytasbeehapp.data.repository

import com.example.mytasbeehapp.data.storage.TasbeehData
import com.example.mytasbeehapp.data.storage.TasbeehStorage
import com.example.mytasbeehapp.domain.model.DayRecord
import com.example.mytasbeehapp.domain.model.localEpochDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime

interface TasbeehRepository {
    fun getTasbeehData(): TasbeehData
    fun saveTasbeehData(data: TasbeehData)
    fun getDailyGoal(): Int
    fun saveDailyGoal(goal: Int)
    fun getHistory(): List<DayRecord>
    fun resetCycle(newStartDay: Long)
    fun getCycleStartDay(): Long

    fun getVibration(): Boolean
    fun saveVibration(enabled: Boolean)
    fun getClickSound(): Boolean
    fun saveClickSound(enabled: Boolean)
    fun getKeepScreenOn(): Boolean
    fun saveKeepScreenOn(enabled: Boolean)
    fun getVolumeButtons(): Boolean
    fun saveVolumeButtons(enabled: Boolean)

    fun getMorningEnabled(): Boolean
    fun saveMorningEnabled(enabled: Boolean)
    fun getMorningTime(): LocalTime
    fun saveMorningTime(time: LocalTime)

    fun getEveningEnabled(): Boolean
    fun saveEveningEnabled(enabled: Boolean)
    fun getEveningTime(): LocalTime
    fun saveEveningTime(time: LocalTime)

    val vibrationFlow: StateFlow<Boolean>
    val clickSoundFlow: StateFlow<Boolean>
    val keepScreenOnFlow: StateFlow<Boolean>
    val volumeButtonsFlow: StateFlow<Boolean>
}

class TasbeehRepositoryImpl(
    private val storage: TasbeehStorage
) : TasbeehRepository {

    private val _vibrationFlow = MutableStateFlow(storage.loadVibration())
    override val vibrationFlow: StateFlow<Boolean> = _vibrationFlow.asStateFlow()

    private val _clickSoundFlow = MutableStateFlow(storage.loadClickSound())
    override val clickSoundFlow: StateFlow<Boolean> = _clickSoundFlow.asStateFlow()

    private val _keepScreenOnFlow = MutableStateFlow(storage.loadKeepScreenOn())
    override val keepScreenOnFlow: StateFlow<Boolean> = _keepScreenOnFlow.asStateFlow()

    private val _volumeButtonsFlow = MutableStateFlow(storage.loadVolumeButtons())
    override val volumeButtonsFlow: StateFlow<Boolean> = _volumeButtonsFlow.asStateFlow()

    override fun getTasbeehData(): TasbeehData {
        return storage.load(localEpochDay())
    }

    override fun saveTasbeehData(data: TasbeehData) {
        storage.save(data)
        if (_vibrationFlow.value != data.hapticEnabled) {
            storage.saveVibration(data.hapticEnabled)
            _vibrationFlow.value = data.hapticEnabled
        }
    }

    override fun getDailyGoal(): Int {
        return storage.loadDailyGoal()
    }

    override fun saveDailyGoal(goal: Int) {
        storage.saveDailyGoal(goal)
        val currentData = storage.load(localEpochDay())
        if (currentData.dailyGoal != goal) {
            storage.save(currentData.copy(dailyGoal = goal))
        }
    }

    override fun getHistory(): List<DayRecord> {
        return storage.loadHistory()
    }

    override fun resetCycle(newStartDay: Long) {
        storage.resetCycle(newStartDay)
    }

    override fun getCycleStartDay(): Long {
        return storage.getCycleStartDay()
    }

    override fun getVibration(): Boolean = _vibrationFlow.value
    override fun saveVibration(enabled: Boolean) {
        storage.saveVibration(enabled)
        _vibrationFlow.value = enabled
        val currentData = storage.load(localEpochDay())
        if (currentData.hapticEnabled != enabled) {
            storage.save(currentData.copy(hapticEnabled = enabled))
        }
    }

    override fun getClickSound(): Boolean = _clickSoundFlow.value
    override fun saveClickSound(enabled: Boolean) {
        storage.saveClickSound(enabled)
        _clickSoundFlow.value = enabled
    }

    override fun getKeepScreenOn(): Boolean = _keepScreenOnFlow.value
    override fun saveKeepScreenOn(enabled: Boolean) {
        storage.saveKeepScreenOn(enabled)
        _keepScreenOnFlow.value = enabled
    }

    override fun getVolumeButtons(): Boolean = _volumeButtonsFlow.value
    override fun saveVolumeButtons(enabled: Boolean) {
        storage.saveVolumeButtons(enabled)
        _volumeButtonsFlow.value = enabled
    }

    override fun getMorningEnabled(): Boolean = storage.loadMorningEnabled()
    override fun saveMorningEnabled(enabled: Boolean) = storage.saveMorningEnabled(enabled)
    override fun getMorningTime(): LocalTime = storage.loadMorningTime()
    override fun saveMorningTime(time: LocalTime) = storage.saveMorningTime(time)

    override fun getEveningEnabled(): Boolean = storage.loadEveningEnabled()
    override fun saveEveningEnabled(enabled: Boolean) = storage.saveEveningEnabled(enabled)
    override fun getEveningTime(): LocalTime = storage.loadEveningTime()
    override fun saveEveningTime(time: LocalTime) = storage.saveEveningTime(time)
}
