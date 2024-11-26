package com.example.lolimobilee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserLogAdapter extends RecyclerView.Adapter<UserLogAdapter.UserLogViewHolder> {

    private List<log> userLogs;

    public UserLogAdapter(List<log> userLogs) {
        this.userLogs = userLogs;
    }

    @NonNull
    @Override
    public UserLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_log_item, parent, false);
        return new UserLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserLogViewHolder holder, int position) {
        log log = userLogs.get(position);
        holder.userMessageText.setText(log.getUserinput());
        holder.robotMessageText.setText(log.getLolioutput());
        holder.userTimestamp.setText(log.getTime());
        holder.robotTimestamp.setText(log.getTime());
    }

    @Override
    public int getItemCount() {
        return userLogs.size();
    }

    static class UserLogViewHolder extends RecyclerView.ViewHolder {
        TextView userMessageText, robotMessageText, userTimestamp, robotTimestamp;

        public UserLogViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessageText = itemView.findViewById(R.id.userMessageText);
            robotMessageText = itemView.findViewById(R.id.robotMessageText);
            userTimestamp = itemView.findViewById(R.id.userTimestamp);
            robotTimestamp = itemView.findViewById(R.id.robotTimestamp);
        }
    }
}
