package com.example.notes

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.notes.databinding.ActivitySettingsBinding
import com.example.notes.utils.ThemeHelper

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupThemeSettings()
        setupDataSection()
        setupAboutSection()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
    
    private fun setupThemeSettings() {
        updateThemeDisplay()
        
        binding.layoutTheme.setOnClickListener {
            showThemeDialog()
        }
    }
    
    private fun setupDataSection() {
        binding.layoutBackup.setOnClickListener {
            val intent = Intent(this, BackupActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupAboutSection() {
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }
        
        binding.layoutVersion.setOnClickListener {
            // Show version info or changelog
            showVersionDialog()
        }
    }
    
    private fun updateThemeDisplay() {
        binding.tvThemeValue.text = ThemeHelper.getThemeName(this)
    }
    
    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val currentTheme = ThemeHelper.getThemeMode(this)
        
        AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, currentTheme) { dialog, which ->
                ThemeHelper.setThemeMode(this, which)
                updateThemeDisplay()
                dialog.dismiss()
                
                // Recreate activity to apply theme immediately
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About My Notes")
            .setMessage(
                "My Notes App v1.1\\n\\n" +
                "A modern, feature-rich notes application built with:\\n" +
                "• Kotlin & Java\\n" +
                "• Room Database\\n" +
                "• Material Design\\n" +
                "• MVVM Architecture\\n\\n" +
                "Features:\\n" +
                "• Create and edit notes\\n" +
                "• Search functionality\\n" +
                "• Categories system\\n" +
                "• Pin important notes\\n" +
                "• Dark mode support\\n\\n" +
                "Developed with ❤️ by Whymyhuman"
            )
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showVersionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Version 1.1")
            .setMessage(
                "What's New:\\n\\n" +
                "✓ Search functionality\\n" +
                "✓ Categories system\\n" +
                "✓ Dark mode support\\n" +
                "✓ Improved UI/UX\\n" +
                "✓ Better performance\\n" +
                "✓ Bug fixes and improvements"
            )
            .setPositiveButton("OK", null)
            .show()
    }
}