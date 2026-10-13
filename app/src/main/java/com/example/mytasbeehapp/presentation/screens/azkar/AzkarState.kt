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
    Zikr(1, "SubhanAllah", "سُبْحَانَ ٱللَّٰهِ", "Glory be to Allah", 33),
    Zikr(2, "Alhamdulillah", "ٱلْحَمْدُ لِلَّٰهِ", "All praise is for Allah", 33),
    Zikr(3, "Allahu Akbar", "ٱللَّٰهُ أَكْبَرُ", "Allah is the Greatest", 34),
    Zikr(4, "Durood Sharif", "صَلَّى ٱللَّٰهُ عَلَيْهِ وَسَلَّمَ", "Peace and blessings upon the Prophet", 100),
    Zikr(5, "Istighfar", "أَسْتَغْفِرُ ٱللَّٰهَ", "I seek for[giveness from Allah", 100),
    Zikr(6, "La ilaha illallah", "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ", "There is no god but Allah", 100)
)