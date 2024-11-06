package com.example.lolimobilee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.JournalEntryViewHolder> {

    private List<JournalEntry> journalEntries;

    public JournalEntryAdapter(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }

    @NonNull
    @Override
    public JournalEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_entry_item, parent, false);
        return new JournalEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalEntryViewHolder holder, int position) {
        JournalEntry entry = journalEntries.get(position);
        holder.entryTitle.setText(entry.getTitle());
        holder.entryDate.setText(entry.getDate());
        holder.entryPreview.setText(entry.getPreview());
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    public static class JournalEntryViewHolder extends RecyclerView.ViewHolder {
        public TextView entryTitle;
        public TextView entryDate;
        public TextView entryPreview;

        public JournalEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            entryTitle = itemView.findViewById(R.id.journalEntryTitle);
            entryDate = itemView.findViewById(R.id.journalEntryDate);
            entryPreview = itemView.findViewById(R.id.journalEntryPreview);
        }
    }
}
