package com.example.notes

import android.app.Application
import com.example.notes.utils.NotificationHelper
import com.example.notes.utils.ThemeHelper

class NotesApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Apply saved theme on app startup
        ThemeHelper.applyTheme(this)
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
    }
}