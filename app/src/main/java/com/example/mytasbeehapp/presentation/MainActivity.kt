package com.example.mytasbeehapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.mytasbeehapp.presentation.navigation.Navigation
import com.example.mytasbeehapp.presentation.ui.theme.MyTasbeehAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyTasbeehAppTheme {
                val navController = rememberNavController()
                Navigation(navController)
            }
        }
    }
}