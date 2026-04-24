package com.jfranco.sharetosave

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.jfranco.sharetosave.common.theme.CatanCompanionTheme
import com.jfranco.sharetosave.features.posts.addEdit.AddEditScreen
import com.jfranco.sharetosave.features.posts.addEdit.AddEditScreenDestinationArgs
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import dagger.hilt.android.AndroidEntryPoint
import org.orbitmvi.orbit.compose.collectSideEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MyApp", "MainActivity: onCreate -----")

        // Handle intent if the app is launched fresh
        handleSharedIntent(intent)

        setContent {
            val navController = rememberNavController()
            val navigator = navController.rememberDestinationsNavigator()

            mainViewModel.collectSideEffect {
                when (it) {
                    is MainViewSideEffect.OnDataShared ->
                        navigator.navigate(
                            AddEditScreenDestination(
                                AddEditScreenDestinationArgs(
                                    text = it.text,
                                    fileUri = it.fileUri,
                                    mimeType = it.mimeType,
                                    fromShare = true,
                                )
                            )
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
            val fileUri: Uri? = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
            val sharedText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
            val mimeType: String? = intent.type

            Log.i("MyApp", "MainActivity - Received fileUri: $fileUri")
            Log.i("MyApp", "MainActivity - Received sharedText: $sharedText")
            Log.i("MyApp", "MainActivity - Received mimeType: $mimeType")

            mainViewModel.onEvent(MainEvent.OnDataShared(sharedText, fileUri, mimeType))

            setIntent(null)
        }
    }
}