package com.example.lolimobilee;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.JournalEntryViewHolder> {

    private List<JournalEntry> journalEntries;
    private Context context;
    private String userId; // userId field to manage entries per user
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance

    // Constructor to initialize context, journal entries list, and userId
    public JournalEntryAdapter(Context context, List<JournalEntry> journalEntries, String userId) {
        this.context = context;
        this.journalEntries = journalEntries;
        this.userId = userId;
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
        holder.entryPreview.setText(entry.getDescription());

        // Set click listener for the entire item to navigate to expandjournal with full details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, expandjournal.class);
            intent.putExtra("title", entry.getTitle());
            intent.putExtra("date", entry.getDate());
            intent.putExtra("description", entry.getDescription()); // Use description instead of preview
            intent.putExtra("entryId", entry.getId()); // Pass the unique entry ID
            intent.putExtra("userId", userId); // Pass userId for context
            context.startActivity(intent);
        });

        // Set click listener for the edit icon to navigate to editjournal with full details
        holder.editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(context, editjournal.class);
            intent.putExtra("title", entry.getTitle());
            intent.putExtra("date", entry.getDate());
            intent.putExtra("description", entry.getDescription()); // Use description instead of preview
            intent.putExtra("entryId", entry.getId()); // Pass the unique entry ID
            intent.putExtra("userId", userId); // Pass userId for context
            context.startActivity(intent);
        });

        // Set click listener for the delete icon to show a confirmation dialog
        holder.deleteIcon.setOnClickListener(v -> showDeleteConfirmationDialog(entry, position));
    }

    private void showDeleteConfirmationDialog(JournalEntry entry, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Journal")
                .setMessage("Are you sure you want to delete this journal entry?")
                .setPositiveButton("Yes", (dialog, which) -> deleteJournalEntry(entry, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteJournalEntry(JournalEntry entry, int position) {
        // Access the journal entry in Firestore using the "journal" collection and entry ID
        db.collection("journal")
                .document(entry.getId()) // Use the journal entry's document ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list and notify adapter
                    journalEntries.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Journal entry deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete journal entry", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    public static class JournalEntryViewHolder extends RecyclerView.ViewHolder {
        public TextView entryTitle;
        public TextView entryDate;
        public TextView entryPreview;
        public ImageView editIcon;
        public ImageView deleteIcon;

        public JournalEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            entryTitle = itemView.findViewById(R.id.journalEntryTitle);
            entryDate = itemView.findViewById(R.id.journalEntryDate);
            entryPreview = itemView.findViewById(R.id.journalEntryPreview);
            editIcon = itemView.findViewById(R.id.editIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }
}
