package com.example.notes.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getIntExtra("note_id", -1)
        val noteTitle = intent.getStringExtra("note_title") ?: ""
        
        if (noteId != -1) {
            NotificationHelper.showNotification(context, noteId, noteTitle)
        }
    }
}