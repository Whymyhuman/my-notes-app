package com.example.notes.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2
    
    fun applyTheme(context: Context) {
        val themeMode = getThemeMode(context)
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    fun setThemeMode(context: Context, themeMode: Int) {
        val prefs = getPreferences(context)
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply()
        applyTheme(context)
    }
    
    fun getThemeMode(context: Context): Int {
        val prefs = getPreferences(context)
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }
    
    fun getThemeName(context: Context): String {
        return when (getThemeMode(context)) {
            THEME_LIGHT -> "Light"
            THEME_DARK -> "Dark"
            THEME_SYSTEM -> "System Default"
            else -> "System Default"
        }
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}