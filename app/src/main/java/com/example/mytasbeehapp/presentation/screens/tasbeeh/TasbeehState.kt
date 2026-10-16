package com.example.mytasbeehapp.presentation.screens.tasbeeh

import com.example.mytasbeehapp.data.storage.TasbeehData
import com.example.mytasbeehapp.domain.model.Dhikr
import com.example.mytasbeehapp.domain.model.DhikrInfo
import com.example.mytasbeehapp.domain.model.Limit
import com.example.mytasbeehapp.domain.model.Preset
import com.example.mytasbeehapp.domain.model.Presets
import com.example.mytasbeehapp.domain.model.DAYS_IN_CYCLE
import com.example.mytasbeehapp.domain.model.DEFAULT_LIMIT
import com.example.mytasbeehapp.domain.model.DEFAULT_DAILY_GOAL
import com.example.mytasbeehapp.domain.model.localEpochDay
import com.example.mytasbeehapp.presentation.screens.azkar.Zikr

fun Dhikr.toInfo() = DhikrInfo(transliteration, arabic, meaning)
fun Zikr.toInfo() = DhikrInfo(title, arabic, meaning)

typealias TasbeehState = TasbeehData
typealias TasbeehStorage = com.example.mytasbeehapp.data.storage.TasbeehStorage