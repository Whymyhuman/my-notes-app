package com.example.notes

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notes.adapter.NotesAdapter
import com.example.notes.data.Note
import com.example.notes.databinding.ActivityTrashBinding
import com.example.notes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

class TrashActivity : AppCompatActivity(), NotesAdapter.OnNoteClickListener {
    
    private lateinit var binding: ActivityTrashBinding
    private lateinit var notesAdapter: NotesAdapter
    private val noteViewModel: NoteViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        observeDeletedNotes()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.trash_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_empty_trash -> {
                showEmptyTrashDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Trash"
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter()
        notesAdapter.setOnNoteClickListener(this)
        
        binding.rvDeletedNotes.apply {
            adapter = notesAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
        }
    }
    
    private fun observeDeletedNotes() {
        noteViewModel.deletedNotes.observe(this) { deletedNotes ->
            notesAdapter.submitList(deletedNotes)
            updateEmptyState(deletedNotes.isEmpty())
            updateTrashCount(deletedNotes.size)
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvDeletedNotes.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvDeletedNotes.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }
    
    private fun updateTrashCount(count: Int) {
        binding.tvTrashCount.text = getString(R.string.trash_count, count)
    }
    
    override fun onNoteClick(note: Note) {
        // Show preview dialog instead of editing
        showNotePreviewDialog(note)
    }
    
    override fun onMoreOptionsClick(note: Note, view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.trash_note_options_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_restore -> {
                    restoreNote(note)
                    true
                }
                R.id.action_delete_permanently -> {
                    showPermanentDeleteDialog(note)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
    
    private fun showNotePreviewDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle(note.title.ifEmpty { "Untitled" })
            .setMessage(note.content)
            .setPositiveButton("Restore") { _, _ ->
                restoreNote(note)
            }
            .setNegativeButton("Close", null)
            .setNeutralButton("Delete Permanently") { _, _ ->
                showPermanentDeleteDialog(note)
            }
            .show()
    }
    
    private fun restoreNote(note: Note) {
        lifecycleScope.launch {
            noteViewModel.restoreFromTrash(note.id)
            Toast.makeText(this@TrashActivity, "Note restored", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showPermanentDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Permanently")
            .setMessage("This note will be permanently deleted and cannot be recovered. Continue?")
            .setPositiveButton("Delete") { _, _ ->
                noteViewModel.deleteNote(note)
                Toast.makeText(this, "Note permanently deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEmptyTrashDialog() {
        lifecycleScope.launch {
            val trashCount = noteViewModel.getTrashCount()
            if (trashCount == 0) {
                Toast.makeText(this@TrashActivity, "Trash is already empty", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            AlertDialog.Builder(this@TrashActivity)
                .setTitle("Empty Trash")
                .setMessage("This will permanently delete all $trashCount notes in trash. This action cannot be undone.")
                .setPositiveButton("Empty Trash") { _, _ ->
                    noteViewModel.emptyTrash()
                    Toast.makeText(this@TrashActivity, "Trash emptied", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}