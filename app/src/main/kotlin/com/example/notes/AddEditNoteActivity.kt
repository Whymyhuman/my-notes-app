package com.example.notes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes.data.Note
import com.example.notes.databinding.ActivityAddEditNoteBinding
import com.example.notes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

class AddEditNoteActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddEditNoteBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private var currentNote: Note? = null
    private var isEditMode = false
    private var isPinned = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        checkEditMode()
        setupClickListeners()
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
            loadNote(noteId)
        }
    }
    
    private fun loadNote(noteId: Int) {
        lifecycleScope.launch {
            currentNote = noteViewModel.getNoteById(noteId)
            currentNote?.let { note ->
                binding.etTitle.setText(note.title)
                binding.etContent.setText(note.content)
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
        val content = binding.etContent.text.toString().trim()
        
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
            noteViewModel.deleteNote(note)
            finish()
        }
    }
    
    override fun onBackPressed() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        
        // Auto-save if there's content
        if (title.isNotEmpty() || content.isNotEmpty()) {
            saveNote()
        } else {
            super.onBackPressed()
        }
    }
}

