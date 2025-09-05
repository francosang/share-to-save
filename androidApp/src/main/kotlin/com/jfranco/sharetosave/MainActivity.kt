package com.jfranco.sharetosave

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import com.jfranco.sharetosave.common.theme.CatanCompanionTheme
import com.jfranco.sharetosave.features.posts.addEdit.AddEditScreen
import com.jfranco.sharetosave.features.posts.list.HandleIntent
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MyApp", "MainActivity: onCreate")

        setContent {
            CatanCompanionTheme {
                Log.d("MyApp", "MainActivity: new nav host")

                DestinationsNavHost(navGraph = NavGraphs.root) {
                    Log.d("MyApp", "MainActivity: new stuff in nav host")

                    composable(AddEditScreenDestination) {
                        AddEditScreen(
                            note = navArgs.note,
                            navigator = destinationsNavigator,
                        )
                    }
                }
            }

            HandleIntent(this) {
                Log.d("MyApp", "MainActivity: new intent received -----")

                Log.d("MyApp", "handle intent: $it")

                Log.d("MyApp", "clip data: ${it.clipData}")
                Log.d("MyApp", "dataString: ${it.dataString}")
                Log.d("MyApp", "action: ${it.action}")
                Log.d("MyApp", "data: ${it.data}")
                try {
                    (it.extras?.keySet()?.toList() ?: emptyList<String>())
                        .forEach { key ->
                            Log.d("MyApp", "extra key: $key")
                            Log.d("MyApp", "extra value: ${it.extras?.get(key)}")
                        }

                    val imageSelected: Uri? = it.extras?.getParcelable(Intent.EXTRA_STREAM)
                    Log.d("MyApp", "imageSelected: $imageSelected")

                    if (imageSelected != null) {
                        // try to get file path from uri
                        val file = copySharedUriToCacheFile(
                            this,
                            imageSelected,
                            "shared_image_${System.currentTimeMillis()}.jpg"
                        )
                        Log.d("MyApp", "file: $file")
                    } else {
                        Log.d("MyApp", "imageSelected is null")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}

/** Copy shared Uri to a file in cache and return that File (useful if some APIs require a file). */
fun copySharedUriToCacheFile(context: Context, uri: Uri, targetFileName: String): File? {
    val cr = context.contentResolver
    val outFile = File(context.cacheDir, targetFileName)
    try {
        cr.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outFile
    } catch (e: FileNotFoundException) {
        Log.e("MyApp", "copySharedUriToCacheFile", e)
        // try inner-decoded uri for Photos wrapper
        try {
            val decoded = Uri.decode(uri.toString())
            val idx = decoded.indexOf("content://")
            if (idx >= 0) {
                val inner = decoded.substring(idx).toUri()
                cr.openInputStream(inner)?.use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                return outFile
            }
        } catch (e: Exception) {
            Log.e("MyApp", "copySharedUriToCacheFile", e)
        }
    } catch (e: Exception) {
        Log.e("MyApp", "copySharedUriToCacheFile", e)
    }
    return null
}