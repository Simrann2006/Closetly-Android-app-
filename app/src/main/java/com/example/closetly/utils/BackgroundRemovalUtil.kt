package com.example.closetly.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dev.eren.removebg.RemoveBg
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import java.io.File
import java.io.FileOutputStream

object BackgroundRemovalUtil {

    private const val TAG = "BackgroundRemoval"

    suspend fun removeBackground(
        context: Context,
        imageUri: Uri,
        preprocessImage: Boolean = true,
        onComplete: (Uri?) -> Unit
    ) {
        try {
            Log.d(TAG, "Starting background removal with RemoveBg library")
            
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap")
                onComplete(null)
                return
            }

            val remover = RemoveBg(context)
            var resultBitmap: Bitmap? = null
            var hasError = false

            remover.clearBackground(originalBitmap)
                .onStart {
                    Log.d(TAG, "Background removal started")
                }
                .catch { e ->
                    Log.e(TAG, "Background removal failed: ${e.message}", e)
                    hasError = true
                }
                .onCompletion {
                    Log.d(TAG, "Background removal completed")
                }
                .collect { output ->
                    resultBitmap = output
                }

            if (hasError || resultBitmap == null) {
                Log.e(TAG, "Background removal failed")
                originalBitmap.recycle()
                onComplete(null)
                return
            }

            val tempFile = File(context.cacheDir, "bg_removed_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(tempFile)
            resultBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            originalBitmap.recycle()
            resultBitmap?.recycle()

            Log.d(TAG, "Background removal successful")
            onComplete(Uri.fromFile(tempFile))

        } catch (e: Exception) {
            Log.e(TAG, "Error during background removal: ${e.message}", e)
            onComplete(null)
        }
    }
}
