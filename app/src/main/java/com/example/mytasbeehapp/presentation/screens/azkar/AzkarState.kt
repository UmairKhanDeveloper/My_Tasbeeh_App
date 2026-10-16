package com.example.mytasbeehapp.presentation.screens.azkar

data class AzkarState(
    val azkar: List<Zikr> = defaultAzkar,
    val streakDays: Int = 0,
    val nameInput: String = "",
    val countInput: String = "33",
    val textInput: String = ""
) {
    val canAdd: Boolean
        get() = nameInput.isNotBlank() && (countInput.toIntOrNull() ?: 0) > 0
}

val defaultAzkar = listOf(
    Zikr(1L, "SubhanAllah", "سُبْحَانَ ٱللَّٰهِ", "Glory be to Allah", 33),
    Zikr(2L, "Alhamdulillah", "ٱلْحَمْدُ لِلَّٰهِ", "All praise is for Allah", 33),
    Zikr(3L, "Allahu Akbar", "ٱللَّٰهُ أَكْبَرُ", "Allah is the Greatest", 34),
    Zikr(4L, "Durood Sharif", "صَلَّى ٱللَّٰهُ عَلَيْهِ وَسَلَّمَ", "Peace and blessings upon the Prophet", 100),
    Zikr(5L, "Istighfar", "أَسْتَغْفِرُ ٱللَّٰهَ", "I seek for[giveness from Allah", 100),
    Zikr(6L, "La ilaha illallah", "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ", "There is no god but Allah", 100)
)