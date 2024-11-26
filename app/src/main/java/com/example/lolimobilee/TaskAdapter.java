package com.example.lolimobilee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskCompleteListener {
        void onTaskComplete(Task task, boolean isComplete);
    }

    private final List<Task> taskList;
    private final OnTaskCompleteListener taskCompleteListener;

    public TaskAdapter(List<Task> taskList, OnTaskCompleteListener taskCompleteListener) {
        this.taskList = taskList;
        this.taskCompleteListener = taskCompleteListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_entry_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskDueDate.setText("Date: " + task.getDate());
        holder.taskStatus.setText("Status: " + (task.getStatus().equals("done") ? "Done" : "Incomplete"));
        holder.priorityIcon.setVisibility(task.isPriority() ? View.VISIBLE : View.GONE);

        holder.taskCheckbox.setChecked(task.isChecked());
        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setChecked(isChecked);
            taskCompleteListener.onTaskComplete(task, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDueDate, taskDescription, taskStatus;
        CheckBox taskCheckbox;
        ImageView priorityIcon;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskStatus = itemView.findViewById(R.id.taskStatus);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            priorityIcon = itemView.findViewById(R.id.priorityIcon);
        }
    }
}
