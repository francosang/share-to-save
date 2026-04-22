package com.jfranco.sharetosave.features.posts.shared

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import com.jfranco.sharetosave.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareHandlerActivity : ComponentActivity() {

    @Inject
    lateinit var sharedDataRepository: SharedDataRepository

    // ... in onCreate or wherever you process the intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("MyApp", "ShareHandlerActivity: onCreate -----")
        Log.i("MyApp", "intent: $intent")
        Log.i("MyApp", "clip data: ${intent.clipData}")
        Log.i("MyApp", "dataString: ${intent.dataString}")
        Log.i("MyApp", "action: ${intent.action}")
        Log.i("MyApp", "data: ${intent.data}")
        Log.i("MyApp", "flags: ${intent.flags}")

        val imageUri: Uri? = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        val sharedText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)

        Log.i("MyApp", "ShareHandlerActivity - Received imageUri: $imageUri")
        Log.i("MyApp", "ShareHandlerActivity - Received sharedText: $sharedText")

        if (imageUri != null) {
            Log.i("MyApp", "ShareHandlerActivity - Storing imageUri in repository")
            sharedDataRepository.setSharedImageUri(imageUri)
        }
        if (sharedText != null) {
            Log.i("MyApp", "ShareHandlerActivity - Storing sharedText in repository")
            sharedDataRepository.setSharedText(sharedText)
        }

        // Now, start MainActivity (if not already running or to bring to front)
        // and ensure it doesn't clear the task
        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            // Important: Add flags to bring existing MainActivity to front without clearing task
            // and to ensure onNewIntent is called if MainActivity is already on top.
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        Log.i("MyApp", "ShareHandlerActivity - Starting MainActivity")
        startActivity(mainActivityIntent)
        finish() // Finish ShareHandlerActivity
    }
}