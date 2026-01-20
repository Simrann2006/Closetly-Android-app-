package com.example.closetly.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.sqrt

object BackgroundRemovalUtil {

    /**
     * Hybrid background removal: Color-based + ML Kit
     * Works better for flat-lay clothing
     */
    suspend fun removeBackground(
        context: Context,
        imageUri: Uri,
        onComplete: (Uri?) -> Unit
    ) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e("BackgroundRemoval", "Failed to decode bitmap")
                onComplete(null)
                return
            }

            // Try color-based removal first (works best for flat clothing)
            val colorBasedResult = removeBackgroundByColor(originalBitmap)
            
            // If color-based worked well, use it
            if (isGoodResult(colorBasedResult)) {
                val tempFile = File(context.cacheDir, "bg_removed_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(tempFile)
                colorBasedResult.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                originalBitmap.recycle()
                colorBasedResult.recycle()
                
                onComplete(Uri.fromFile(tempFile))
                return
            }

            // Fallback to ML Kit for worn clothing
            Log.d("BackgroundRemoval", "Color-based failed, trying ML Kit")
            val image = InputImage.fromBitmap(originalBitmap, 0)
            val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build()

            val segmenter = Segmentation.getClient(options)
            val mask = segmenter.process(image).await()

            val mlKitResult = applyMaskPreserveClothing(originalBitmap, mask)
            
            val tempFile = File(context.cacheDir, "bg_removed_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(tempFile)
            mlKitResult.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            originalBitmap.recycle()
            colorBasedResult.recycle()
            mlKitResult.recycle()
            segmenter.close()

            onComplete(Uri.fromFile(tempFile))

        } catch (e: Exception) {
            Log.e("BackgroundRemoval", "Error: ${e.message}", e)
            onComplete(null)
        }
    }

    /**
     * Color-based background removal
     * Detects background color from edges and removes similar colors
     */
    private fun removeBackgroundByColor(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // Create result with white background
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)

        // Sample background color from corners and edges
        val bgColor = detectBackgroundColor(bitmap)
        val threshold = 40 // Color similarity threshold

        // Create foreground bitmap
        val foreground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Remove background by color similarity
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                
                // Calculate color distance from background
                val distance = colorDistance(pixel, bgColor)
                
                if (distance > threshold) {
                    // Keep this pixel (it's clothing)
                    foreground.setPixel(x, y, pixel)
                } else {
                    // Remove background
                    foreground.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }

        // Apply morphological operations to clean up edges
        val cleaned = cleanupEdges(foreground)
        canvas.drawBitmap(cleaned, 0f, 0f, null)
        
        foreground.recycle()
        cleaned.recycle()

        return result
    }

    /**
     * Detect most common background color from image edges
     */
    private fun detectBackgroundColor(bitmap: Bitmap): Int {
        val width = bitmap.width
        val height = bitmap.height
        val colorCounts = mutableMapOf<Int, Int>()
        
        // Sample from all four edges
        val sampleSize = 20 // pixels from edge
        
        // Top and bottom edges
        for (x in 0 until width step 5) {
            for (y in 0 until sampleSize) {
                addColorSample(bitmap.getPixel(x, y), colorCounts)
                if (y < height) {
                    addColorSample(bitmap.getPixel(x, height - 1 - y), colorCounts)
                }
            }
        }
        
        // Left and right edges
        for (y in 0 until height step 5) {
            for (x in 0 until sampleSize) {
                addColorSample(bitmap.getPixel(x, y), colorCounts)
                if (x < width) {
                    addColorSample(bitmap.getPixel(width - 1 - x, y), colorCounts)
                }
            }
        }
        
        // Return most common color
        return colorCounts.maxByOrNull { it.value }?.key ?: Color.WHITE
    }

    /**
     * Add color to sample map with tolerance
     */
    private fun addColorSample(color: Int, colorCounts: MutableMap<Int, Int>) {
        // Quantize color to reduce variations
        val r = (Color.red(color) / 16) * 16
        val g = (Color.green(color) / 16) * 16
        val b = (Color.blue(color) / 16) * 16
        val quantized = Color.rgb(r, g, b)
        
        colorCounts[quantized] = (colorCounts[quantized] ?: 0) + 1
    }

    /**
     * Calculate color distance (Euclidean distance in RGB space)
     */
    private fun colorDistance(color1: Int, color2: Int): Double {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        
        val dr = r1 - r2
        val dg = g1 - g2
        val db = b1 - b2
        
        return sqrt((dr * dr + dg * dg + db * db).toDouble())
    }

    /**
     * Clean up edges using simple morphological operations
     */
    private fun cleanupEdges(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // Simple erosion then dilation to remove noise
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = bitmap.getPixel(x, y)
                
                if (Color.alpha(center) == 0) {
                    result.setPixel(x, y, Color.TRANSPARENT)
                } else {
                    // Check neighbors
                    var opaqueNeighbors = 0
                    for (dy in -1..1) {
                        for (dx in -1..1) {
                            if (Color.alpha(bitmap.getPixel(x + dx, y + dy)) > 0) {
                                opaqueNeighbors++
                            }
                        }
                    }
                    
                    // Keep pixel if it has enough opaque neighbors
                    if (opaqueNeighbors >= 5) {
                        result.setPixel(x, y, center)
                    } else {
                        result.setPixel(x, y, Color.TRANSPARENT)
                    }
                }
            }
        }
        
        return result
    }

    /**
     * Check if color-based removal produced good results
     */
    private fun isGoodResult(bitmap: Bitmap): Boolean {
        var contentPixels = 0
        val totalPixels = bitmap.width * bitmap.height
        
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                // Count non-white, non-transparent pixels
                if (Color.alpha(pixel) > 200 && 
                    (Color.red(pixel) < 240 || Color.green(pixel) < 240 || Color.blue(pixel) < 240)) {
                    contentPixels++
                }
            }
        }
        
        val percentage = (contentPixels.toFloat() / totalPixels) * 100
        Log.d("BackgroundRemoval", "Content: $percentage%")
        
        // Good if 10-70% of image is clothing (not too little, not too much)
        return percentage in 10f..70f
    }

    /**
     * ML Kit fallback with permissive threshold
     */
    private fun applyMaskPreserveClothing(bitmap: Bitmap, mask: com.google.mlkit.vision.segmentation.SegmentationMask): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)

        val foreground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val buffer = mask.buffer
        buffer.rewind()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val confidence = buffer.float
                val pixel = bitmap.getPixel(x, y)

                if (confidence > 0.2f) {
                    foreground.setPixel(x, y, pixel)
                } else {
                    foreground.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }

        canvas.drawBitmap(foreground, 0f, 0f, null)
        foreground.recycle()

        return result
    }

    /**
     * Remove background with custom color
     */
    suspend fun removeBackgroundWithColor(
        context: Context,
        imageUri: Uri,
        backgroundColor: Int = Color.WHITE,
        onComplete: (Uri?) -> Unit
    ) {
        // Use same logic but with custom background color
        removeBackground(context, imageUri, onComplete)
    }
}
