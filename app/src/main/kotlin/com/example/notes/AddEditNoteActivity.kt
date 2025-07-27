package com.example.notes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRichTextEditor()
        setupFormattingToolbar()
        checkEditMode()
        setupClickListeners()
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
            onBackPressed()
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
            currentNote = noteViewModel.getNoteById(noteId)
            currentNote?.let { note ->
                binding.etTitle.setText(note.title)
                richTextEditor.setFormattedText(note.content)
                isPinned = note.isPinned
                updatePinIcon()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.ivSave.setOnClickListener {
            saveNote()
        }
        
        binding.ivPin.setOnClickListener {
            isPinned = !isPinned
            updatePinIcon()
        }
        
        binding.ivDelete.setOnClickListener {
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
        val content = richTextEditor.getFormattedText().trim()
        
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (isEditMode && currentNote != null) {
            // Update existing note
            val updatedNote = currentNote!!.apply {
                this.title = title
                this.content = content
                this.isPinned = this@AddEditNoteActivity.isPinned
                this.timestamp = System.currentTimeMillis()
            }
            noteViewModel.updateNote(updatedNote)
        } else {
            // Create new note
            val newNote = Note(
                title,
                content,
                System.currentTimeMillis(),
                isPinned
            )
            noteViewModel.insertNote(newNote)
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
        binding.formattingToolbar.btnBold.setOnClickListener {
            richTextEditor.toggleBold()
            updateFormattingButtons()
        }
        
        binding.formattingToolbar.btnItalic.setOnClickListener {
            richTextEditor.toggleItalic()
            updateFormattingButtons()
        }
        
        binding.formattingToolbar.btnUnderline.setOnClickListener {
            richTextEditor.toggleUnderline()
            updateFormattingButtons()
        }
        
        binding.formattingToolbar.btnClearFormatting.setOnClickListener {
            richTextEditor.clearFormatting()
            updateFormattingButtons()
        }
        
        binding.formattingToolbar.btnAddImage.setOnClickListener {
            // TODO: Implement image attachment
            Toast.makeText(this, "Image attachment coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        binding.formattingToolbar.btnSetReminder.setOnClickListener {
            // TODO: Implement reminder
            Toast.makeText(this, "Reminder feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateFormattingButtons() {
        // Update button states based on current formatting
        binding.formattingToolbar.btnBold.isSelected = richTextEditor.isBoldEnabled()
        binding.formattingToolbar.btnItalic.isSelected = richTextEditor.isItalicEnabled()
        binding.formattingToolbar.btnUnderline.isSelected = richTextEditor.isUnderlineEnabled()
    }
    
    override fun onBackPressed() {
        val title = binding.etTitle.text.toString().trim()
        val content = richTextEditor.getFormattedText().trim()
        
        // Auto-save if there's content
        if (title.isNotEmpty() || content.isNotEmpty()) {
            saveNote()
        } else {
            super.onBackPressed()
        }
    }
}

