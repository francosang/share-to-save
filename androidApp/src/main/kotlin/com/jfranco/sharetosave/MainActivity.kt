package com.jfranco.sharetosave

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.jfranco.sharetosave.common.theme.CatanCompanionTheme
import com.jfranco.sharetosave.features.posts.addEdit.AddEditScreen
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import dagger.hilt.android.AndroidEntryPoint

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

            val showDialog = remember { mutableStateOf(false) }

            CatanCompanionTheme {
                DestinationsNavHost(navGraph = NavGraphs.root) {
                    composable(AddEditScreenDestination) {
                        AddEditScreen(
                            note = navArgs.note,
                            navigator = destinationsNavigator,
                        )
                    }
                }

                if (image != null) {
                    // Show the AddEditScreen when there's a shared image
                    AlertDialog(
                        onDismissRequest = { showDialog.value = false },
                        title = { Text("Title") },
                        text = { Text("This dialog sits on top of any screen.\nImage: $image") },
                        confirmButton = {
                            TextButton(onClick = { showDialog.value = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        Log.i("MyApp", "MainActivity: onNewIntent -----")
//        Log.i("MyApp", "intent: $intent")
//        Log.i("MyApp", "clip data: ${intent.clipData}")
//        Log.i("MyApp", "dataString: ${intent.dataString}")
//        Log.i("MyApp", "action: ${intent.action}")
//        Log.i("MyApp", "data: ${intent.data}")
//        Log.i("MyApp", "flags: ${intent.flags}")
//
//        // Update the intent to be used by HandleIntent
//        setIntent(intent)
////        handleIntent(intent)
//    }
//
//    private fun handleIntent(intent: Intent) {
//        Log.d("MyApp", "MainActivity: new intent received -----")
//        Log.d("MyApp", "intent: $intent")
//        Log.d("MyApp", "clip data: ${intent.clipData}")
//        Log.d("MyApp", "dataString: ${intent.dataString}")
//        Log.d("MyApp", "action: ${intent.action}")
//        Log.d("MyApp", "data: ${intent.data}")
//        Log.d("MyApp", "flags: ${intent.flags}")
//
//        try {
//            (intent.extras?.keySet()?.toList() ?: emptyList<String>())
//                .forEach { key ->
//                    Log.d("MyApp", "extra key: $key")
//                    Log.d("MyApp", "extra value: ${intent.extras?.get(key)}")
//                }
//
//            val imageSelected: Uri? = intent.extras?.getParcelable(Intent.EXTRA_STREAM)
//            Log.d("MyApp", "imageSelected: $imageSelected")
//
//            if (imageSelected != null) {
//                // try to get file path from uri
//                val file = copySharedUriToCacheFile(
//                    this,
//                    imageSelected,
//                    "shared_image_${System.currentTimeMillis()}.jpg"
//                )
//                Log.d("MyApp", "file: $file")
//            } else {
//                Log.d("MyApp", "imageSelected is null")
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
}

///** Copy shared Uri to a file in cache and return that File (useful if some APIs require a file). */
//fun copySharedUriToCacheFile(context: Context, uri: Uri, targetFileName: String): File? {
//    val cr = context.contentResolver
//    val outFile = File(context.cacheDir, targetFileName)
//    try {
//        cr.openInputStream(uri)?.use { input ->
//            outFile.outputStream().use { output ->
//                input.copyTo(output)
//            }
//        }
//        return outFile
//    } catch (e: FileNotFoundException) {
//        Log.e("MyApp", "copySharedUriToCacheFile", e)
//        // try inner-decoded uri for Photos wrapper
//        try {
//            val decoded = Uri.decode(uri.toString())
//            val idx = decoded.indexOf("content://")
//            if (idx >= 0) {
//                val inner = decoded.substring(idx).toUri()
//                cr.openInputStream(inner)?.use { input ->
//                    outFile.outputStream().use { output ->
//                        input.copyTo(output)
//                    }
//                }
//                return outFile
//            }
//        } catch (e: Exception) {
//            Log.e("MyApp", "copySharedUriToCacheFile", e)
//        }
//    } catch (e: Exception) {
//        Log.e("MyApp", "copySharedUriToCacheFile", e)
//    }
//    return null
//}