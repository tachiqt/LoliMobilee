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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.JournalEntryViewHolder> {

    private List<JournalEntry> journalEntries;
    private Context context;
    private String userId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, expandjournal.class);
            intent.putExtra("title", entry.getTitle());
            intent.putExtra("date", entry.getDate());
            intent.putExtra("description", entry.getDescription());
            intent.putExtra("entryId", entry.getId());
            intent.putExtra("userId", userId);
            context.startActivity(intent);
        });

        holder.editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(context, editjournal.class);
            intent.putExtra("title", entry.getTitle());
            intent.putExtra("date", entry.getDate());
            intent.putExtra("description", entry.getDescription());
            intent.putExtra("entryId", entry.getId());
            intent.putExtra("userId", userId);
            context.startActivity(intent);
        });

        holder.deleteIcon.setOnClickListener(v -> showDeleteConfirmationDialog(entry, position));
    }

    private void showDeleteConfirmationDialog(JournalEntry entry, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Journal")
                .setMessage("Are you sure you want to delete this journal entry?")
                .setPositiveButton("Yes", (dialog, which) -> markEntryAsDeletedAndTransfer(entry, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void markEntryAsDeletedAndTransfer(JournalEntry entry, int position) {
        db.collection("journalEntries")
                .document(entry.getId())
                .update("status", "deleted")
                .addOnSuccessListener(aVoid -> {
                    db.collection("journaldeleted")
                            .document(entry.getId())
                            .set(entry)
                            .addOnSuccessListener(aVoid2 -> {
                                // Remove the item and notify the adapter
                                journalEntries.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Journal entry deleted and transferred", Toast.LENGTH_SHORT).show();

                                // Refresh via activity
                                if (context instanceof homepage) {
                                    ((homepage) context).refreshJournalList();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to transfer journal entry to deleted collection", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update journal entry status", Toast.LENGTH_SHORT).show();
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
