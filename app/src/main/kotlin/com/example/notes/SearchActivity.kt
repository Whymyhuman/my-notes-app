package com.example.notes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notes.adapter.NotesAdapter
import com.example.notes.data.Note
import com.example.notes.databinding.ActivitySearchBinding
import com.example.notes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity(), NotesAdapter.OnNoteClickListener {
    
    private lateinit var binding: ActivitySearchBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var notesAdapter: NotesAdapter
    private var allNotes = listOf<Note>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        loadNotes()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search Notes"
        
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
    
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter()
        notesAdapter.setOnNoteClickListener(this)
        
        binding.recyclerView.apply {
            adapter = notesAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }
    
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNotes(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Focus on search field
        binding.etSearch.requestFocus()
    }
    
    private fun loadNotes() {
        noteViewModel.allNotes.observe(this) { notes ->
            allNotes = notes
            filterNotes(binding.etSearch.text.toString())
        }
    }
    
    private fun filterNotes(query: String) {
        val filteredNotes = if (query.isEmpty()) {
            allNotes
        } else {
            allNotes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true)
            }
        }
        
        notesAdapter.submitList(filteredNotes)
        
        // Show/hide empty state
        if (filteredNotes.isEmpty() && query.isNotEmpty()) {
            binding.tvEmptyState.text = "No notes found for \"$query\""
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
        } else if (filteredNotes.isEmpty()) {
            binding.tvEmptyState.text = "No notes available"
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
        }
    }
    
    override fun onNoteClick(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_NOTE_ID, note.id)
        startActivity(intent)
    }
    
    override fun onMoreOptionsClick(note: Note, view: android.view.View) {
        // Handle more options click if needed
    }
}