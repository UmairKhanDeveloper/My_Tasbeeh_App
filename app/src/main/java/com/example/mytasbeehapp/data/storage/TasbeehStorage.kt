package com.example.mytasbeehapp.data.storage

import android.content.Context
import com.example.mytasbeehapp.domain.model.Dhikr
import com.example.mytasbeehapp.domain.model.DhikrInfo
import com.example.mytasbeehapp.domain.model.Limit
import com.example.mytasbeehapp.domain.model.Preset
import com.example.mytasbeehapp.domain.model.DayRecord
import com.example.mytasbeehapp.domain.model.DAYS_IN_CYCLE
import com.example.mytasbeehapp.domain.model.DEFAULT_LIMIT
import com.example.mytasbeehapp.domain.model.DEFAULT_DAILY_GOAL
import com.example.mytasbeehapp.domain.model.HISTORY_DAYS_KEPT
import com.example.mytasbeehapp.domain.model.PresetStep
import com.example.mytasbeehapp.domain.model.localEpochDay
import com.example.mytasbeehapp.domain.model.Presets
import com.example.mytasbeehapp.presentation.screens.azkar.Zikr
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalTime

fun Dhikr.toInfo() = DhikrInfo(transliteration, arabic, meaning)
fun Zikr.toInfo() = DhikrInfo(title, arabic, meaning)

data class TasbeehData(
    val count: Int = 0,
    val limit: Limit = Limit.Fixed(DEFAULT_LIMIT),
    val customInput: String = "",
    val preset: Preset? = null,
    val zikr: Zikr? = null,
    val presetStep: Int = 0,
    val completedRounds: Int = 0,
    val completions: Int = 0,
    val dailyCount: Int = 0,
    val dailyGoal: Int = DEFAULT_DAILY_GOAL,
    val dailyDate: Long = 0L,
    val cycleStartDay: Long = 0L,
    val dayInCycle: Int = 1,
    val hapticEnabled: Boolean = true
) {
    private val currentStep: PresetStep?
        get() = preset?.steps?.getOrNull(presetStep)

    val activeLimit: Limit
        get() = currentStep?.let { Limit.Fixed(it.target) } ?: limit

    val dhikr: DhikrInfo
        get() = currentStep?.dhikr?.toInfo()
            ?: zikr?.toInfo()
            ?: Dhikr.SubhanAllah.toInfo()

    val isCustomSelected: Boolean
        get() = preset == null && (customInput.toIntOrNull() ?: 0) > 0

    val ringProgress: Float
        get() = when (val l = activeLimit) {
            is Limit.Fixed -> if (l.value > 0) (count.toFloat() / l.value).coerceIn(0f, 1f) else 0f
            Limit.Unlimited -> (count % 100) / 100f
        }

    val dailyProgress: Float
        get() = if (dailyGoal <= 0) 0f else (dailyCount.toFloat() / dailyGoal).coerceIn(0f, 1f)

    val dailyGoalReached: Boolean
        get() = dailyGoal > 0 && dailyCount >= dailyGoal
}

fun TasbeehData.syncedTo(today: Long): TasbeehData {
    val day = Math.floorMod(today - cycleStartDay, DAYS_IN_CYCLE.toLong()).toInt() + 1
    return if (dailyDate == today) copy(dayInCycle = day)
    else copy(dailyDate = today, dailyCount = 0, dayInCycle = day)
}

class TasbeehStorage(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("tasbeeh_prefs", Context.MODE_PRIVATE)

    fun load(today: Long): TasbeehData {
        checkDayRollover(today)
        val preset = if (prefs.getBoolean(K_PRESET, false)) Presets.TasbeehFatima else null
        val limitValue = prefs.getInt(K_LIMIT, DEFAULT_LIMIT)
        val limit = if (limitValue <= 0) Limit.Unlimited else Limit.Fixed(limitValue)
        val custom = prefs.getString(K_CUSTOM, "").orEmpty()

        val zikr = if (preset == null && prefs.contains(K_ZIKR_TITLE)) {
            Zikr(
                id = prefs.getLong(K_ZIKR_ID, 0L),
                title = prefs.getString(K_ZIKR_TITLE, "").orEmpty(),
                arabic = prefs.getString(K_ZIKR_ARABIC, "").orEmpty(),
                meaning = prefs.getString(K_ZIKR_MEANING, "").orEmpty(),
                count = prefs.getInt(K_ZIKR_COUNT, DEFAULT_LIMIT)
            )
        } else null

        // Initialize cycle start day if not set (first time user)
        val cycleInitialized = prefs.getBoolean(K_CYCLE_INITIALIZED, false)
        val cycleStartDay = if (cycleInitialized) {
            prefs.getLong(K_CYCLE_START, today)
        } else {
            // Initialize cycle to start on today
            prefs.edit()
                .putBoolean(K_CYCLE_INITIALIZED, true)
                .putLong(K_CYCLE_START, today)
                .apply()
            today
        }

        val loaded = TasbeehData(
            count = prefs.getInt(K_COUNT, 0).coerceAtLeast(0),
            limit = limit,
            customInput = if (custom.toIntOrNull() == limitValue) custom else "",
            preset = preset,
            zikr = zikr,
            presetStep = prefs.getInt(K_STEP, 0).coerceIn(0, preset?.steps?.lastIndex ?: 0),
            completedRounds = prefs.getInt(K_ROUNDS, 0),
            completions = prefs.getInt(K_COMPLETIONS, 0),
            dailyCount = prefs.getInt(K_DAILY_COUNT, 0).coerceAtLeast(0),
            dailyGoal = prefs.getInt(K_DAILY_GOAL, DEFAULT_DAILY_GOAL),
            dailyDate = prefs.getLong(K_DAILY_DATE, today),
            cycleStartDay = cycleStartDay,
            hapticEnabled = prefs.getBoolean(K_HAPTIC, true)
        )

        val active = loaded.activeLimit
        val safe = if (active is Limit.Fixed && loaded.count >= active.value) loaded.copy(count = 0) else loaded
        return safe.syncedTo(today)
    }

    fun save(s: TasbeehData) {
        prefs.edit().apply {
            putInt(K_COUNT, s.count)
            putInt(K_LIMIT, (s.limit as? Limit.Fixed)?.value ?: 0)
            putString(K_CUSTOM, s.customInput)
            putBoolean(K_PRESET, s.preset != null)
            putInt(K_STEP, s.presetStep)
            putInt(K_ROUNDS, s.completedRounds)
            putInt(K_COMPLETIONS, s.completions)
            putInt(K_DAILY_COUNT, s.dailyCount)
            putInt(K_DAILY_GOAL, s.dailyGoal)
            putLong(K_DAILY_DATE, s.dailyDate)
            putLong(K_CYCLE_START, s.cycleStartDay)
            putBoolean(K_HAPTIC, s.hapticEnabled)

            val z = s.zikr
            if (z != null) {
                putLong(K_ZIKR_ID, z.id)
                putString(K_ZIKR_TITLE, z.title)
                putString(K_ZIKR_ARABIC, z.arabic)
                putString(K_ZIKR_MEANING, z.meaning)
                putInt(K_ZIKR_COUNT, z.count)
            } else {
                remove(K_ZIKR_ID)
                remove(K_ZIKR_TITLE)
                remove(K_ZIKR_ARABIC)
                remove(K_ZIKR_MEANING)
                remove(K_ZIKR_COUNT)
            }
        }.apply()

        recordDaily(s.dailyDate, s.dailyCount)
    }

    fun loadHistory(): List<DayRecord> {
        checkDayRollover(localEpochDay())
        val raw = prefs.getString(K_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                val day = obj.optLong(JSON_DAY, -1L)
                val count = obj.optInt(JSON_COUNT, -1)
                if (day < 0 || count < 0) null else DayRecord(day, count)
            }.sortedBy { it.epochDay }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun loadDailyGoal(): Int = prefs.getInt(K_DAILY_GOAL, DEFAULT_DAILY_GOAL)

    fun saveDailyGoal(goal: Int) {
        prefs.edit().putInt(K_DAILY_GOAL, goal).apply()
    }

    fun resetCycle(newStartDay: Long) {
        prefs.edit()
            .putLong(K_CYCLE_START, newStartDay)
            .putBoolean(K_CYCLE_INITIALIZED, true)
            .apply()
    }

    fun getCycleStartDay(): Long {
        val initialized = prefs.getBoolean(K_CYCLE_INITIALIZED, false)
        return if (initialized) {
            prefs.getLong(K_CYCLE_START, localEpochDay())
        } else {
            localEpochDay()
        }
    }

    fun loadVibration(): Boolean = prefs.getBoolean(K_HAPTIC, true)
    fun saveVibration(enabled: Boolean) { prefs.edit().putBoolean(K_HAPTIC, enabled).apply() }

    fun loadClickSound(): Boolean = prefs.getBoolean(K_CLICK_SOUND, false)
    fun saveClickSound(enabled: Boolean) { prefs.edit().putBoolean(K_CLICK_SOUND, enabled).apply() }

    fun loadKeepScreenOn(): Boolean = prefs.getBoolean(K_KEEP_SCREEN_ON, true)
    fun saveKeepScreenOn(enabled: Boolean) { prefs.edit().putBoolean(K_KEEP_SCREEN_ON, enabled).apply() }

    fun loadVolumeButtons(): Boolean = prefs.getBoolean(K_VOLUME_BUTTONS, true)
    fun saveVolumeButtons(enabled: Boolean) { prefs.edit().putBoolean(K_VOLUME_BUTTONS, enabled).apply() }

    fun loadMorningEnabled(): Boolean = prefs.getBoolean(K_MORNING_ENABLED, true)
    fun saveMorningEnabled(enabled: Boolean) { prefs.edit().putBoolean(K_MORNING_ENABLED, enabled).apply() }

    fun loadMorningTime(): LocalTime {
        val hour = prefs.getInt(K_MORNING_HOUR, 5)
        val minute = prefs.getInt(K_MORNING_MINUTE, 30)
        return LocalTime.of(hour, minute)
    }
    fun saveMorningTime(time: LocalTime) {
        prefs.edit()
            .putInt(K_MORNING_HOUR, time.hour)
            .putInt(K_MORNING_MINUTE, time.minute)
            .apply()
    }

    fun loadEveningEnabled(): Boolean = prefs.getBoolean(K_EVENING_ENABLED, true)
    fun saveEveningEnabled(enabled: Boolean) { prefs.edit().putBoolean(K_EVENING_ENABLED, enabled).apply() }

    fun loadEveningTime(): LocalTime {
        val hour = prefs.getInt(K_EVENING_HOUR, 18)
        val minute = prefs.getInt(K_EVENING_MINUTE, 30)
        return LocalTime.of(hour, minute)
    }
    fun saveEveningTime(time: LocalTime) {
        prefs.edit()
            .putInt(K_EVENING_HOUR, time.hour)
            .putInt(K_EVENING_MINUTE, time.minute)
            .apply()
    }

    private fun recordDaily(day: Long, count: Int) {
        if (day <= 0L) return
        val history = loadHistory().toMutableList()
        val idx = history.indexOfFirst { it.epochDay == day }
        when {
            idx >= 0 && history[idx].count == count -> return
            idx >= 0 -> history[idx] = DayRecord(day, count)
            else -> history.add(DayRecord(day, count))
        }
        val trimmed = history.sortedBy { it.epochDay }.takeLast(HISTORY_DAYS_KEPT)
        saveHistory(trimmed)
    }

    private fun saveHistory(history: List<DayRecord>) {
        val array = JSONArray()
        history.forEach { record ->
            array.put(
                JSONObject().apply {
                    put(JSON_DAY, record.epochDay)
                    put(JSON_COUNT, record.count)
                }
            )
        }
        prefs.edit().putString(K_HISTORY, array.toString()).apply()
    }

    private fun checkDayRollover(today: Long) {
        val savedDailyDate = prefs.getLong(K_DAILY_DATE, 0L)
        val savedDailyCount = prefs.getInt(K_DAILY_COUNT, 0).coerceAtLeast(0)

        if (savedDailyDate > 0L && savedDailyDate < today) {
            if (savedDailyCount > 0) {
                recordDaily(savedDailyDate, savedDailyCount)
            }
            prefs.edit()
                .putLong(K_DAILY_DATE, today)
                .putInt(K_DAILY_COUNT, 0)
                .apply()
        } else if (savedDailyDate == 0L) {
            prefs.edit()
                .putLong(K_DAILY_DATE, today)
                .apply()
        }
    }

    private companion object {
        const val K_COUNT = "count"
        const val K_LIMIT = "limit"
        const val K_CUSTOM = "custom"
        const val K_PRESET = "preset_on"
        const val K_STEP = "preset_step"
        const val K_ROUNDS = "rounds"
        const val K_COMPLETIONS = "completions"
        const val K_DAILY_COUNT = "daily_count"
        const val K_DAILY_GOAL = "daily_goal"
        const val K_DAILY_DATE = "daily_date"
        const val K_CYCLE_START = "cycle_start"
        const val K_CYCLE_INITIALIZED = "cycle_initialized"
        const val K_HAPTIC = "haptic"
        const val K_CLICK_SOUND = "click_sound"
        const val K_KEEP_SCREEN_ON = "keep_screen_on"
        const val K_VOLUME_BUTTONS = "volume_buttons"
        const val K_MORNING_ENABLED = "morning_enabled"
        const val K_MORNING_HOUR = "morning_hour"
        const val K_MORNING_MINUTE = "morning_minute"
        const val K_EVENING_ENABLED = "evening_enabled"
        const val K_EVENING_HOUR = "evening_hour"
        const val K_EVENING_MINUTE = "evening_minute"
        const val K_HISTORY = "daily_history"
        const val K_ZIKR_ID = "zikr_id"
        const val K_ZIKR_TITLE = "zikr_title"
        const val K_ZIKR_ARABIC = "zikr_arabic"
        const val K_ZIKR_MEANING = "zikr_meaning"
        const val K_ZIKR_COUNT = "zikr_count"

        const val JSON_DAY = "d"
        const val JSON_COUNT = "c"
    }
}
