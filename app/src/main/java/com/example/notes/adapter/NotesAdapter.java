package com.example.notes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notes.R;
import com.example.notes.data.Note;
import com.example.notes.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    
    private List<Note> notes = new ArrayList<>();
    private OnNoteClickListener listener;
    
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onMoreOptionsClick(Note note, View view);
    }
    
    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.bind(currentNote);
    }
    
    @Override
    public int getItemCount() {
        return notes.size();
    }
    
    public void submitList(List<Note> newNotes) {
        this.notes = newNotes != null ? newNotes : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNoteTitle;
        private TextView tvNoteContent;
        private TextView tvTimestamp;
        private TextView tvPinnedBadge;
        private ImageView ivMoreOptions;
        
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvPinnedBadge = itemView.findViewById(R.id.tvPinnedBadge);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNoteClick(notes.get(position));
                }
            });
            
            ivMoreOptions.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMoreOptionsClick(notes.get(position), v);
                }
            });
        }
        
        public void bind(Note note) {
            tvNoteTitle.setText(note.getTitle().isEmpty() ? "Untitled" : note.getTitle());
            tvNoteContent.setText(note.getContent());
            tvTimestamp.setText(TimeUtils.getTimeAgo(note.getTimestamp()));
            
            if (note.isPinned()) {
                tvPinnedBadge.setVisibility(View.VISIBLE);
            } else {
                tvPinnedBadge.setVisibility(View.GONE);
            }
        }
    }
}

