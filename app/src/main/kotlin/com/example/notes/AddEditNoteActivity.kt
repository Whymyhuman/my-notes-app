package com.example.notes

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes.data.Note
import com.example.notes.databinding.ActivityAddEditNoteBinding
import com.example.notes.utils.ExportUtils
import com.example.notes.utils.RichTextEditor
import com.example.notes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

class AddEditNoteActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddEditNoteBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private var currentNote: Note? = null
    private var isEditMode = false
    private var isPinned = false
    private lateinit var richTextEditor: RichTextEditor
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AddEditNote", "onCreate started")
        
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Log.d("AddEditNote", "Layout inflated")
        
        setupToolbar()
        setupRichTextEditor()
        setupFormattingToolbar()
        checkEditMode()
        setupClickListeners()
        setupBackPressedCallback()
        
        Log.d("AddEditNote", "onCreate completed")
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEditMode) {
            menuInflater.inflate(R.menu.note_edit_menu, menu)
        }
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                currentNote?.let { note ->
                    ExportUtils.shareNote(this, note)
                }
                true
            }
            R.id.action_export_pdf -> {
                currentNote?.let { note ->
                    lifecycleScope.launch {
                        val pdfPath = ExportUtils.exportNoteToPdf(this@AddEditNoteActivity, note)
                        if (pdfPath != null) {
                            Toast.makeText(this@AddEditNoteActivity, "PDF exported to: $pdfPath", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@AddEditNoteActivity, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    
    private fun checkEditMode() {
        val noteId = intent.getIntExtra(MainActivity.EXTRA_NOTE_ID, -1)
        if (noteId != -1) {
            isEditMode = true
            binding.ivDelete.visibility = android.view.View.VISIBLE
            invalidateOptionsMenu() // Refresh menu
            loadNote(noteId)
        }
    }
    
    private fun loadNote(noteId: Int) {
        lifecycleScope.launch {
            try {
                currentNote = noteViewModel.getNoteById(noteId)
                currentNote?.let { note ->
                    binding.etTitle.setText(note.title)
                    try {
                        richTextEditor.setFormattedText(note.content)
                    } catch (e: Exception) {
                        // Fallback to plain text
                        richTextEditor.setText(note.content)
                    }
                    isPinned = note.isPinned
                    updatePinIcon()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditNoteActivity, "Error loading note: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        Log.d("AddEditNote", "Setting up click listeners")
        
        binding.ivSave.setOnClickListener {
            Log.d("AddEditNote", "Save button clicked")
            saveNote()
        }
        
        binding.ivPin.setOnClickListener {
            Log.d("AddEditNote", "Pin button clicked")
            isPinned = !isPinned
            updatePinIcon()
        }
        
        binding.ivDelete.setOnClickListener {
            Log.d("AddEditNote", "Delete button clicked")
            deleteNote()
        }
    }
    
    private fun updatePinIcon() {
        if (isPinned) {
            binding.ivPin.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            binding.ivPin.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }
    
    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = try {
            richTextEditor.getFormattedText().trim()
        } catch (e: Exception) {
            // Fallback to plain text if rich text fails
            richTextEditor.text.toString().trim()
        }
        
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            Log.d("AddEditNote", "Saving note - Title: '$title', Content length: ${content.length}, IsEdit: $isEditMode")
            
            if (isEditMode && currentNote != null) {
                // Update existing note
                val updatedNote = currentNote!!.apply {
                    this.title = title
                    this.content = content
                    this.isPinned = this@AddEditNoteActivity.isPinned
                    this.timestamp = System.currentTimeMillis()
                }
                Log.d("AddEditNote", "Updating existing note with ID: ${updatedNote.id}")
                noteViewModel.updateNote(updatedNote)
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
            } else {
                // Create new note
                val newNote = Note(
                    title,
                    content,
                    System.currentTimeMillis(),
                    isPinned
                )
                Log.d("AddEditNote", "Creating new note")
                noteViewModel.insertNote(newNote)
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AddEditNote", "Error saving note", e)
            Toast.makeText(this, "Error saving note: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }
        
        finish()
    }
    
    private fun deleteNote() {
        currentNote?.let { note ->
            noteViewModel.moveToTrash(note)
            finish()
        }
    }
    
    private fun setupRichTextEditor() {
        richTextEditor = binding.etContent
    }
    
    private fun setupFormattingToolbar() {
        try {
            binding.formattingToolbar.btnBold.setOnClickListener {
                try {
                    richTextEditor.toggleBold()
                    updateFormattingButtons()
                } catch (e: Exception) {
                    Toast.makeText(this, "Bold formatting error", Toast.LENGTH_SHORT).show()
                }
            }
            
            binding.formattingToolbar.btnItalic.setOnClickListener {
                try {
                    richTextEditor.toggleItalic()
                    updateFormattingButtons()
                } catch (e: Exception) {
                    Toast.makeText(this, "Italic formatting error", Toast.LENGTH_SHORT).show()
                }
            }
            
            binding.formattingToolbar.btnUnderline.setOnClickListener {
                try {
                    richTextEditor.toggleUnderline()
                    updateFormattingButtons()
                } catch (e: Exception) {
                    Toast.makeText(this, "Underline formatting error", Toast.LENGTH_SHORT).show()
                }
            }
            
            binding.formattingToolbar.btnClearFormatting.setOnClickListener {
                try {
                    richTextEditor.clearFormatting()
                    updateFormattingButtons()
                } catch (e: Exception) {
                    Toast.makeText(this, "Clear formatting error", Toast.LENGTH_SHORT).show()
                }
            }
            
            binding.formattingToolbar.btnAddImage.setOnClickListener {
                // TODO: Implement image attachment
                Toast.makeText(this, "Image attachment coming soon!", Toast.LENGTH_SHORT).show()
            }
            
            binding.formattingToolbar.btnSetReminder.setOnClickListener {
                // TODO: Implement reminder
                Toast.makeText(this, "Reminder feature coming soon!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up formatting toolbar", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateFormattingButtons() {
        try {
            // Update button states based on current formatting
            binding.formattingToolbar.btnBold.isSelected = richTextEditor.isBoldEnabled()
            binding.formattingToolbar.btnItalic.isSelected = richTextEditor.isItalicEnabled()
            binding.formattingToolbar.btnUnderline.isSelected = richTextEditor.isUnderlineEnabled()
        } catch (e: Exception) {
            // Ignore formatting button update errors
        }
    }
    
    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val title = binding.etTitle.text.toString().trim()
                val content = richTextEditor.getFormattedText().trim()
                
                // Auto-save if there's content
                if (title.isNotEmpty() || content.isNotEmpty()) {
                    saveNote()
                } else {
                    finish()
                }
            }
        })
    }
}

