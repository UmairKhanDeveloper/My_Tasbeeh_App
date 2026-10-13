package com.example.mytasbeehapp.presentation.screens.tasbeeh

import android.content.Context
import com.example.mytasbeehapp.presentation.screens.azkar.Zikr
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.TimeZone


const val DAYS_IN_CYCLE = 7
const val DEFAULT_LIMIT = 33
const val DEFAULT_DAILY_GOAL = 300

/** Kitne din ka daily-count history rakhna hai (~1 saal). Purane din is se zyada purane trim ho jate hain. */
const val HISTORY_DAYS_KEPT = 366

enum class Dhikr(
    val arabic: String,
    val meaning: String,
    val transliteration: String
) {
    SubhanAllah("سُبْحَانَ ٱللَّهِ", "Glory be to Allah", "SubhanAllah"),
    Alhamdulillah("ٱلْحَمْدُ لِلَّهِ", "All praise is due to Allah", "Alhamdulillah"),
    AllahuAkbar("ٱللَّهُ أَكْبَرُ", "Allah is the Greatest", "Allahu Akbar")
}

/** Dhikr card ko jo kuch dikhana hai, chahe preset ka ho ya user ka chuna hua. */
data class DhikrInfo(val name: String, val arabic: String, val meaning: String)

fun Dhikr.toInfo() = DhikrInfo(transliteration, arabic, meaning)
fun Zikr.toInfo() = DhikrInfo(title, arabic, meaning)

sealed interface Limit {
    data class Fixed(val value: Int) : Limit
    data object Unlimited : Limit
}

data class PresetStep(val dhikr: Dhikr, val target: Int)

data class Preset(val name: String, val steps: List<PresetStep>) {
    val label: String
        get() = "$name (${steps.joinToString(" · ") { it.target.toString() }})"
}

object Presets {
    val TasbeehFatima = Preset(
        name = "Tasbeeh-e-Fatima",
        steps = listOf(
            PresetStep(Dhikr.SubhanAllah, 33),
            PresetStep(Dhikr.Alhamdulillah, 33),
            PresetStep(Dhikr.AllahuAkbar, 34)
        )
    )
}

/** Days since epoch in the device's local time zone. */
fun localEpochDay(): Long {
    val now = System.currentTimeMillis()
    return (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
}

/**
 * [localEpochDay] ka ulta: ek epoch day ko wapas (approx) local-midnight UTC millis mein convert
 * karta hai. Stats screen is se day labels (Mon/Tue/..) aur month grouping banata hai.
 */
fun epochDayToMillis(epochDay: Long): Long {
    val approx = epochDay * 86_400_000L
    return approx - TimeZone.getDefault().getOffset(approx)
}

/** Ek din ka saved count — Stats screen (week chart, month total, streak) isi se banta hai. */
data class DayRecord(val epochDay: Long, val count: Int)

data class TasbeehState(
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

    /** Priority: Preset step > selected Zikr > default. */
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

/** Daily counter aur Day 1..7 cycle ko [today] tak aage barhata hai. */
fun TasbeehState.syncedTo(today: Long): TasbeehState {
    val day = Math.floorMod(today - cycleStartDay, DAYS_IN_CYCLE.toLong()).toInt() + 1
    return if (dailyDate == today) copy(dayInCycle = day)
    else copy(dailyDate = today, dailyCount = 0, dayInCycle = day)
}




class TasbeehStorage(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("tasbeeh_prefs", Context.MODE_PRIVATE)

    fun load(today: Long): TasbeehState {
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

        val loaded = TasbeehState(
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
            cycleStartDay = prefs.getLong(K_CYCLE_START, today),
            hapticEnabled = prefs.getBoolean(K_HAPTIC, true)
        )

        val active = loaded.activeLimit
        val safe = if (active is Limit.Fixed && loaded.count >= active.value) loaded.copy(count = 0) else loaded
        return safe.syncedTo(today)
    }

    fun save(s: TasbeehState) {
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

        // Stats screen (week chart, month total, streak) isi per-day history se banti hai.
        recordDaily(s.dailyDate, s.dailyCount)
    }

    // ---------------------------------------------------------------------
    // Daily history (Stats screen ke liye) + shared daily goal
    // ---------------------------------------------------------------------

    /** Saved history, purane se naye din tak sorted. */
    fun loadHistory(): List<DayRecord> {
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

    /** Stats screen se goal change ho to yahan se persist hota hai (Home screen bhi isi value ko follow karta hai). */
    fun saveDailyGoal(goal: Int) {
        prefs.edit().putInt(K_DAILY_GOAL, goal).apply()
    }

    private fun recordDaily(day: Long, count: Int) {
        if (day <= 0L) return
        val history = loadHistory().toMutableList()
        val idx = history.indexOfFirst { it.epochDay == day }
        when {
            idx >= 0 && history[idx].count == count -> return // kuch badla nahi, likhne ki zarurat nahi
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
        const val K_HAPTIC = "haptic"
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