package com.example.notes.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MediaManager(private val context: Context) {
    
    companion object {
        private const val TAG = "MediaManager"
        private const val IMAGES_DIR = "images"
        private const val AUDIO_DIR = "audio"
        private const val FILES_DIR = "files"
        private const val MAX_IMAGE_SIZE = 1024 * 1024 // 1MB
        private const val IMAGE_QUALITY = 85
    }
    
    private val appDir = File(context.filesDir, "media")
    private val imagesDir = File(appDir, IMAGES_DIR)
    private val audioDir = File(appDir, AUDIO_DIR)
    private val filesDir = File(appDir, FILES_DIR)
    
    init {
        createDirectories()
    }
    
    private fun createDirectories() {
        try {
            if (!appDir.exists()) appDir.mkdirs()
            if (!imagesDir.exists()) imagesDir.mkdirs()
            if (!audioDir.exists()) audioDir.mkdirs()
            if (!filesDir.exists()) filesDir.mkdirs()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating directories", e)
        }
    }
    
    /**
     * Save image from URI and return the saved file path
     */
    suspend fun saveImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open input stream"))
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
                ?: return@withContext Result.failure(Exception("Cannot decode image"))
            
            inputStream.close()
            
            // Compress image if too large
            val compressedBitmap = if (getImageSize(bitmap) > MAX_IMAGE_SIZE) {
                compressImage(bitmap)
            } else {
                bitmap
            }
            
            // Generate unique filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "IMG_${timestamp}.jpg"
            val file = File(imagesDir, filename)
            
            // Save compressed image
            val outputStream = FileOutputStream(file)
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
            outputStream.close()
            
            if (!bitmap.isRecycled) bitmap.recycle()
            if (compressedBitmap != bitmap && !compressedBitmap.isRecycled) {
                compressedBitmap.recycle()
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Save audio file and return the saved file path
     */
    suspend fun saveAudioFile(sourceFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "AUDIO_${timestamp}.m4a"
            val destFile = File(audioDir, filename)
            
            sourceFile.copyTo(destFile, overwrite = true)
            
            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving audio file", e)
            Result.failure(e)
        }
    }
    
    /**
     * Save any file and return the saved file path
     */
    suspend fun saveFile(uri: Uri, originalName: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open input stream"))
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val extension = originalName?.substringAfterLast('.', "") ?: "file"
            val filename = "FILE_${timestamp}.${extension}"
            val file = File(filesDir, filename)
            
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving file", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete media file
     */
    suspend fun deleteFile(filePath: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            val deleted = if (file.exists()) file.delete() else true
            Result.success(deleted)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get file size in bytes
     */
    fun getFileSize(filePath: String): Long {
        return try {
            File(filePath).length()
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Get human readable file size
     */
    fun getFormattedFileSize(filePath: String): String {
        val bytes = getFileSize(filePath)
        return formatFileSize(bytes)
    }
    
    /**
     * Check if file exists
     */
    fun fileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get file extension
     */
    fun getFileExtension(filePath: String): String {
        return File(filePath).extension.lowercase()
    }
    
    /**
     * Check if file is an image
     */
    fun isImageFile(filePath: String): Boolean {
        val extension = getFileExtension(filePath)
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }
    
    /**
     * Check if file is an audio file
     */
    fun isAudioFile(filePath: String): Boolean {
        val extension = getFileExtension(filePath)
        return extension in listOf("mp3", "m4a", "wav", "aac", "ogg", "flac")
    }
    
    /**
     * Get thumbnail for image
     */
    suspend fun getImageThumbnail(filePath: String, maxSize: Int = 200): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)
            
            val scale = calculateInSampleSize(options, maxSize, maxSize)
            options.inSampleSize = scale
            options.inJustDecodeBounds = false
            
            BitmapFactory.decodeFile(filePath, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating thumbnail", e)
            null
        }
    }
    
    /**
     * Clean up unused media files
     */
    suspend fun cleanupUnusedFiles(usedFilePaths: List<String>) = withContext(Dispatchers.IO) {
        try {
            val allFiles = mutableListOf<File>()
            allFiles.addAll(imagesDir.listFiles()?.toList() ?: emptyList())
            allFiles.addAll(audioDir.listFiles()?.toList() ?: emptyList())
            allFiles.addAll(filesDir.listFiles()?.toList() ?: emptyList())
            
            val usedPaths = usedFilePaths.toSet()
            var deletedCount = 0
            
            for (file in allFiles) {
                if (file.absolutePath !in usedPaths) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            
            Log.i(TAG, "Cleaned up $deletedCount unused files")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    // Private helper methods
    private fun getImageSize(bitmap: Bitmap): Int {
        return bitmap.byteCount
    }
    
    private fun compressImage(bitmap: Bitmap): Bitmap {
        val ratio = Math.sqrt(MAX_IMAGE_SIZE.toDouble() / getImageSize(bitmap))
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}
