package com.example.notes.media

import android.content.Context
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorder(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioRecorder"
        private const val AUDIO_FORMAT = MediaRecorder.OutputFormat.MPEG_4
        private const val AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC
        private const val SAMPLE_RATE = 44100
        private const val BIT_RATE = 128000
    }
    
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    private var isRecording = false
    private var _isPlaying = false
    
    private val tempDir = File(context.cacheDir, "temp_audio")
    
    init {
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
    }
    
    /**
     * Start recording audio
     */
    suspend fun startRecording(): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                return@withContext Result.failure(Exception("Already recording"))
            }
            
            stopPlayback() // Stop any ongoing playback
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "temp_audio_${timestamp}.m4a"
            currentRecordingFile = File(tempDir, fileName)
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(AUDIO_FORMAT)
                setAudioEncoder(AUDIO_ENCODER)
                setAudioSamplingRate(SAMPLE_RATE)
                setAudioEncodingBitRate(BIT_RATE)
                setOutputFile(currentRecordingFile!!.absolutePath)
                
                prepare()
                start()
            }
            
            isRecording = true
            Log.d(TAG, "Recording started: ${currentRecordingFile!!.absolutePath}")
            
            Result.success(currentRecordingFile!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Stop recording and return the recorded file
     */
    suspend fun stopRecording(): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!isRecording) {
                return@withContext Result.failure(Exception("Not recording"))
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            val recordedFile = currentRecordingFile
            if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0) {
                Log.d(TAG, "Recording stopped: ${recordedFile.absolutePath}")
                Result.success(recordedFile)
            } else {
                Result.failure(Exception("Recording file is empty or doesn't exist"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Cancel current recording
     */
    suspend fun cancelRecording(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
            }
            
            // Delete the temp file
            currentRecordingFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                }
            }
            currentRecordingFile = null
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling recording", e)
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Play audio file
     */
    suspend fun playAudio(filePath: String, onCompletion: (() -> Unit)? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (_isPlaying) {
                stopPlayback()
            }
            
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Audio file doesn't exist"))
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnCompletionListener {
                    _isPlaying = false
                    onCompletion?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying = false
                    true
                }
                prepare()
                start()
            }
            
            _isPlaying = true
            Log.d(TAG, "Started playing: $filePath")
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio", e)
            _isPlaying = false
            Result.failure(e)
        }
    }
    
    /**
     * Stop audio playback
     */
    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying()) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            _isPlaying = false
            Log.d(TAG, "Playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        }
    }
    
    /**
     * Pause audio playback
     */
    fun pausePlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying()) {
                    pause()
                    _isPlaying = false
                    Log.d(TAG, "Playback paused")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback", e)
        }
    }
    
    /**
     * Resume audio playback
     */
    fun resumePlayback() {
        try {
            mediaPlayer?.apply {
                if (!isPlaying()) {
                    start()
                    _isPlaying = true
                    Log.d(TAG, "Playback resumed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming playback", e)
        }
    }
    
    /**
     * Get current playback position in milliseconds
     */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Get total duration of audio in milliseconds
     */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Seek to position in milliseconds
     */
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking to position", e)
        }
    }
    
    /**
     * Get audio duration from file path
     */
    suspend fun getAudioDuration(filePath: String): Int = withContext(Dispatchers.IO) {
        var tempPlayer: MediaPlayer? = null
        try {
            tempPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
            }
            tempPlayer.duration
        } catch (e: Exception) {
            Log.e(TAG, "Error getting audio duration", e)
            0
        } finally {
            tempPlayer?.release()
        }
    }
    
    /**
     * Format duration in mm:ss format
     */
    fun formatDuration(durationMs: Int): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * Check if currently playing
     */
    fun isPlaying(): Boolean = _isPlaying
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
            }
            
            if (_isPlaying) {
                stopPlayback()
            }
            
            // Clean up temp files
            tempDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("temp_audio_")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    /**
     * Release all resources
     */
    fun release() {
        cleanup()
    }
}
