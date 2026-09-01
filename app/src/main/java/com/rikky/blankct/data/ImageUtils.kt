package com.rikky.blankct.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    private const val MAX_DIMENSION = 800
    private const val JPEG_QUALITY = 60

    fun uriToCompressedBase64(context: Context, uri: Uri): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(input)
            input.close()

            val resized = resizeBitmap(original, MAX_DIMENSION)
            val outputStream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            val bytes = outputStream.toByteArray()

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio: Float = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
