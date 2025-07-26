package com.example.notes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.data.Note
import com.example.notes.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {
    
    private var onNoteClickListener: OnNoteClickListener? = null
    
    interface OnNoteClickListener {
        fun onNoteClick(note: Note)
        fun onMoreOptionsClick(note: Note, view: View)
    }
    
    fun setOnNoteClickListener(listener: OnNoteClickListener) {
        onNoteClickListener = listener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class NoteViewHolder(private val binding: ItemNoteBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(note: Note) {
            binding.apply {
                tvTitle.text = note.title
                tvContent.text = note.content
                tvTimestamp.text = formatTimestamp(note.timestamp)
                
                // Show pin indicator
                ivPin.visibility = if (note.isPinned) View.VISIBLE else View.GONE
                
                // Set click listeners
                root.setOnClickListener {
                    onNoteClickListener?.onNoteClick(note)
                }
                
                ivMore.setOnClickListener {
                    onNoteClickListener?.onMoreOptionsClick(note, it)
                }
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
    
    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}