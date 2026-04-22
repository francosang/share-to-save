package com.jfranco.sharetosave

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import com.jfranco.sharetosave.common.theme.CatanCompanionTheme
import com.jfranco.sharetosave.features.posts.addEdit.AddEditScreen
import com.jfranco.sharetosave.features.posts.addEdit.AddEditScreenDestinationArgs
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

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
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                combine(
                    mainViewModel.imageToDisplay,
                    mainViewModel.sharedText
                ) { image, text -> image to text }
                    .filter { (image, text) -> image != null || text != null }
                    .collect {
                        Log.d("MyApp", "MainActivity: navigating to AddEditScreen")
                        navController.navigate(
                            AddEditScreenDestination(AddEditScreenDestinationArgs()).route
                        )
                    }
            }

            CatanCompanionTheme {
                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                    navController = navController,
                ) {
                    composable(AddEditScreenDestination) {
                        AddEditScreen(
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
