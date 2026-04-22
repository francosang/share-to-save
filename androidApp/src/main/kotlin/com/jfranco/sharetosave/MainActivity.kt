package com.jfranco.sharetosave

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
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
import com.jfranco.sharetosave.features.posts.shared.SharedDataRepository
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    // Inject the repository directly into MainActivity
    @Inject
    lateinit var sharedDataRepository: SharedDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MyApp", "MainActivity: onCreate -----")

        // Handle intent if the app is launched fresh
        handleSharedIntent(intent)

        setContent {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                combine(
                    mainViewModel.imageToDisplay,
                    mainViewModel.sharedText
                ) { image, text -> image to text }
                    .filter { (image, text) -> image != null || text != null }
                    .collect { (image, text) ->
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

        // Update the Activity's intent and handle the new shared data
        setIntent(intent)
        handleSharedIntent(intent)
    }

    private fun handleSharedIntent(intent: Intent?) {
        if (intent == null) return

        val action = intent.action

        // Check if this is a share intent
        if (action == Intent.ACTION_SEND) {
            val imageUri: Uri? = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
            val sharedText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)

            Log.i("MyApp", "MainActivity - Received imageUri: $imageUri")
            Log.i("MyApp", "MainActivity - Received sharedText: $sharedText")

            if (imageUri != null) {
                sharedDataRepository.setSharedImageUri(imageUri)
            }
            if (sharedText != null) {
                sharedDataRepository.setSharedText(sharedText)
            }
        }
    }
}