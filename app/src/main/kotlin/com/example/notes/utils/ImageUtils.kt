package com.example.notes.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    
    private const val IMAGE_QUALITY = 80
    private const val MAX_IMAGE_SIZE = 1024
    
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Compress and resize image
            val compressedBitmap = compressImage(bitmap)
            
            // Create unique filename
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_${timeStamp}.jpg"
            
            // Save to internal storage
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val imageFile = File(imagesDir, fileName)
            val outputStream = FileOutputStream(imageFile)
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
            outputStream.close()
            
            return imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    private fun compressImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return bitmap
        }
        
        val ratio = if (width > height) {
            MAX_IMAGE_SIZE.toFloat() / width
        } else {
            MAX_IMAGE_SIZE.toFloat() / height
        }
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    fun deleteImage(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    fun imagePathsToJson(imagePaths: List<String>): String {
        val jsonArray = JSONArray()
        for (path in imagePaths) {
            jsonArray.put(path)
        }
        return jsonArray.toString()
    }
    
    fun jsonToImagePaths(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        
        val imagePaths = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                imagePaths.add(jsonArray.getString(i))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return imagePaths
    }
    
    fun cleanupOrphanedImages(context: Context, allImagePaths: List<String>) {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) return
        
        val allFiles = imagesDir.listFiles() ?: return
        for (file in allFiles) {
            if (!allImagePaths.contains(file.absolutePath)) {
                file.delete()
            }
        }
    }
}