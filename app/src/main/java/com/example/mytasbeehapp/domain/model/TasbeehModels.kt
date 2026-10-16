package com.example.mytasbeehapp.domain.model

import com.example.mytasbeehapp.presentation.screens.azkar.Zikr

const val DAYS_IN_CYCLE = 7
const val DEFAULT_LIMIT = 33
const val DEFAULT_DAILY_GOAL = 300
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

fun localEpochDay(): Long {
    val now = System.currentTimeMillis()
    return (now + java.util.TimeZone.getDefault().getOffset(now)) / 86_400_000L
}

fun epochDayToMillis(epochDay: Long): Long {
    val approx = epochDay * 86_400_000L
    return approx - java.util.TimeZone.getDefault().getOffset(approx)
}

data class DayRecord(val epochDay: Long, val count: Int)
