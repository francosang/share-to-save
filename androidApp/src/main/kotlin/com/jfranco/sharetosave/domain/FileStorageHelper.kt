import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.util.UUID

object FileStorageHelper {

    fun saveSharedImageInternal(context: Context, sharedUri: Uri): String? {
        return try {
            // 1. Open the temporary stream
            val inputStream = context.contentResolver.openInputStream(sharedUri) ?: return null

            // 2. Create a dedicated directory for shared images
            val directory = File(context.filesDir, "shared_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // 3. Create a unique file name to prevent overwriting
            val fileName = "IMG_${UUID.randomUUID()}.jpg"
            val destinationFile = File(directory, fileName)

            // 4. Copy the bytes
            inputStream.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // 5. Return the absolute path so Room can save it
            destinationFile.absolutePath
        } catch (e: Exception) {
            Log.e("FileStorageHelper", "Error saving shared image", e)
            null
        }
    }
}