package com.example.closetly.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.closetly.utils.BackgroundRemovalUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.concurrent.Executors

class CommonRepoImpl : CommonRepo {

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dsck5fzdm",
            "api_key" to "427515648758314",
            "api_secret" to "FZwiSrZKEyhB795JZM8F-jWYzfc"
        )
    )

    override fun uploadImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun uploadImageWithBackgroundRemoval(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            BackgroundRemovalUtil.removeBackground(context, imageUri) { processedUri ->
                if (processedUri != null) {
                    // Upload the processed image (with white background)
                    uploadImage(context, processedUri, callback)
                } else {
                    // If background removal fails, upload original image
                    uploadImage(context, imageUri, callback)
                }
            }
        }
    }

    override fun getFileNameFromUri(
        context: Context,
        imageUri: Uri
    ): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(imageUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}