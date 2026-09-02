package com.rikky.blankct.data

import android.util.Base64
import java.io.File

object AudioUtils {
    fun fileToBase64(file: File): String? {
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun base64ToFile(base64: String, outputFile: File): File? {
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            outputFile.writeBytes(bytes)
            outputFile
        } catch (e: Exception) {
            null
        }
    }
}
