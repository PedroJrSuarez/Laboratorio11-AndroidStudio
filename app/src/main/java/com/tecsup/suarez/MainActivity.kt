package com.tecsup.suarez

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tecsup.suarez.ui.AuthApp
import com.tecsup.suarez.ui.theme.MisCursosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MisCursosTheme {
                AuthApp()
            }
        }
    }
}
