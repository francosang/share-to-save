package com.jfranco.sharetosave

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.jfranco.sharetosave.common.theme.CatanCompanionTheme
import com.jfranco.sharetosave.domain.Note
import com.jfranco.sharetosave.features.posts.addEdit.AddEditScreen
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestinationNavArgs
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MyApp", "MainActivity: onCreate -----")
        Log.i("MyApp", "intent: $intent")
        Log.i("MyApp", "clip data: ${intent.clipData}")
        Log.i("MyApp", "dataString: ${intent.dataString}")
        Log.i("MyApp", "action: ${intent.action}")
        Log.i("MyApp", "data: ${intent.data}")
        Log.i("MyApp", "flags: ${intent.flags}")

        setContent {
            val mainViewModel = hiltViewModel<MainViewModel>()
            val image by mainViewModel.imageToDisplay.collectAsState()
            val sharedText by mainViewModel.sharedText.collectAsState()

            val navController = rememberNavController()

            LaunchedEffect(image, sharedText) {
                Log.d("MyApp", "MainActivity: LaunchedEffect fired — image=$image, sharedText=$sharedText")
                if (image != null || sharedText != null) {
                    val note = Note(
                        id = null,
                        title = null,
                        content = sharedText,
                        image = image?.toString(),
                        created = LocalDateTime.now(),
                        edited = null,
                        color = Note.noteColors.first().toArgb(),
                    )
                    val direction = AddEditScreenDestination(AddEditScreenDestinationNavArgs(note))
                    Log.d("MyApp", "MainActivity: navigating to AddEditScreen — route=${direction.route}")
                    navController.navigate(direction.route)
                    mainViewModel.consumeSharedData()
                }
            }

            CatanCompanionTheme {
                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                    navController = navController,
                ) {
                    composable(AddEditScreenDestination) {
                        AddEditScreen(
                            note = navArgs.note,
                            navigator = destinationsNavigator,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MyApp", "MainActivity: onNewIntent — action=${intent.action}")
        setIntent(intent)
    }
}
