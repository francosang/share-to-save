package com.jfranco.sharetosave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cafe.adriel.voyager.navigator.Navigator
import com.jfranco.sharetosave.common.theme.CatanCompanionTheme
import com.jfranco.sharetosave.features.posts.list.NotesScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CatanCompanionTheme {
                Navigator(screen = NotesScreen())
            }
        }
    }
}