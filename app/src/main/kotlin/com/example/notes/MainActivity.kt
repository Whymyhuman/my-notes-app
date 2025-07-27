package com.example.notes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notes.adapter.NotesAdapter
import com.example.notes.data.Note
import com.example.notes.databinding.ActivityMainBinding
import com.example.notes.viewmodel.NoteViewModel

class MainActivity : AppCompatActivity(), NotesAdapter.OnNoteClickListener {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var notesAdapter: NotesAdapter
    private val noteViewModel: NoteViewModel by viewModels()
    
    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
    
    private val addNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Handle result if needed
    }
    
    private val editNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Handle result if needed
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupSearchView()
        setupFab()
        observeNotes()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_categories -> {
                val intent = Intent(this, CategoriesActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_trash -> {
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter()
        notesAdapter.setOnNoteClickListener(this)
        
        binding.rvNotes.apply {
            adapter = notesAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
        }
    }
    
    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                noteViewModel.searchNotes(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            addNoteResultLauncher.launch(intent)
        }
    }
    
    private fun observeNotes() {
        noteViewModel.allNotes.observe(this) { notes ->
            notesAdapter.submitList(notes)
            updateNotesCount(notes.size)
            updateEmptyState(notes.isEmpty())
        }
    }
    
    private fun updateNotesCount(count: Int) {
        binding.tvNotesCount.text = getString(R.string.notes_count, count)
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvNotes.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvNotes.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }
    
    override fun onNoteClick(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java)
        intent.putExtra(EXTRA_NOTE_ID, note.id)
        editNoteResultLauncher.launch(intent)
    }
    
    override fun onMoreOptionsClick(note: Note, view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.note_options_menu, popup.menu)
        
        // Update pin menu item text
        val pinMenuItem = popup.menu.findItem(R.id.action_pin)
        pinMenuItem.title = if (note.isPinned) getString(R.string.unpin) else getString(R.string.pin)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_pin -> {
                    noteViewModel.togglePinStatus(note)
                    true
                }
                R.id.action_delete -> {
                    noteViewModel.moveToTrash(note)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}

