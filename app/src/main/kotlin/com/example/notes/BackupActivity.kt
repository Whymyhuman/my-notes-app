package com.example.notes

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes.databinding.ActivityBackupBinding
import com.example.notes.utils.BackupHelper
import com.example.notes.viewmodel.CategoryViewModel
import com.example.notes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class BackupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBackupBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                restoreFromBackup(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        updateLastBackupInfo()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Backup and Export"
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCreateBackup.setOnClickListener {
            createBackup()
        }
        
        binding.btnRestoreBackup.setOnClickListener {
            selectBackupFile()
        }
        
        binding.btnExportText.setOnClickListener {
            exportToText()
        }
        
        binding.btnExportJson.setOnClickListener {
            createBackup(true)
        }
    }
    
    private fun updateLastBackupInfo() {
        // This would typically check for the last backup timestamp
        // For now, we'll show a placeholder
        binding.tvLastBackup.text = "No backup created yet"
    }
    
    private fun createBackup(forExport: Boolean = false) {
        lifecycleScope.launch {
            try {
                binding.btnCreateBackup.isEnabled = false
                binding.btnExportJson.isEnabled = false
                
                val notes = noteViewModel.getAllNotesSync()
                val categories = categoryViewModel.allCategories.value ?: emptyList()
                
                val backupFile = BackupHelper.createBackup(this@BackupActivity, notes, categories)
                
                if (backupFile != null) {
                    if (forExport) {
                        BackupHelper.shareFile(this@BackupActivity, backupFile, "application/json")
                        Toast.makeText(this@BackupActivity, "Backup file ready to share", Toast.LENGTH_SHORT).show()
                    } else {
                        showBackupSuccessDialog(backupFile.name)
                        updateLastBackupInfo()
                    }
                } else {
                    Toast.makeText(this@BackupActivity, "Failed to create backup", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BackupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnCreateBackup.isEnabled = true
                binding.btnExportJson.isEnabled = true
            }
        }
    }
    
    private fun exportToText() {
        lifecycleScope.launch {
            try {
                binding.btnExportText.isEnabled = false
                
                val notes = noteViewModel.getAllNotesSync()
                val categories = categoryViewModel.allCategories.value ?: emptyList()
                
                val exportFile = BackupHelper.exportNotesToText(this@BackupActivity, notes, categories)
                
                if (exportFile != null) {
                    BackupHelper.shareFile(this@BackupActivity, exportFile, "text/plain")
                    Toast.makeText(this@BackupActivity, "Notes exported to text file", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BackupActivity, "Failed to export notes", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BackupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnExportText.isEnabled = true
            }
        }
    }
    
    private fun selectBackupFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun restoreFromBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val backupContent = reader.readText()
                reader.close()
                
                val backupData = BackupHelper.parseBackup(backupContent)
                
                if (backupData != null) {
                    showRestoreConfirmationDialog(backupData)
                } else {
                    Toast.makeText(this@BackupActivity, "Invalid backup file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BackupActivity, "Error reading backup file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showBackupSuccessDialog(fileName: String) {
        AlertDialog.Builder(this)
            .setTitle("Backup Created")
            .setMessage("Backup file created successfully:\\n$fileName\\n\\nWould you like to share it?")
            .setPositiveButton("Share") { _, _ ->
                // Share the backup file
                val backupFile = java.io.File(getExternalFilesDir(null), fileName)
                BackupHelper.shareFile(this, backupFile, "application/json")
            }
            .setNegativeButton("OK", null)
            .show()
    }
    
    private fun showRestoreConfirmationDialog(backupData: BackupHelper.BackupData) {
        val backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(backupData.timestamp))
        
        AlertDialog.Builder(this)
            .setTitle("Restore Backup")
            .setMessage(
                "Backup Information:\\n" +
                "Date: $backupDate\\n" +
                "Notes: ${backupData.notes.size}\\n" +
                "Categories: ${backupData.categories.size}\\n\\n" +
                "⚠️ This will replace all current data. Continue?"
            )
            .setPositiveButton("Restore") { _, _ ->
                performRestore(backupData)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performRestore(backupData: BackupHelper.BackupData) {
        lifecycleScope.launch {
            try {
                // Clear existing data
                noteViewModel.deleteAllNotes()
                // Clear categories would need to be implemented
                
                // Restore categories first
                backupData.categories.forEach { category ->
                    categoryViewModel.insertCategory(category)
                }
                
                // Restore notes
                backupData.notes.forEach { note ->
                    noteViewModel.insertNote(note)
                }
                
                Toast.makeText(this@BackupActivity, "Backup restored successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@BackupActivity, "Error restoring backup: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}