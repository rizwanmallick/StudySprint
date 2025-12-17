package com.example.mad_theory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudyTaskAdapter extends RecyclerView.Adapter<StudyTaskAdapter.StudyTaskViewHolder> {

    private List<StudyTask> taskList;
    private final OnMenuClickListener menuClickListener;

    public interface OnMenuClickListener {
        void onMenuClick(View view, int position);
    }

    public StudyTaskAdapter(List<StudyTask> taskList, OnMenuClickListener menuClickListener) {
        this.taskList = taskList;
        this.menuClickListener = menuClickListener;
    }

    @NonNull
    @Override
    public StudyTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_task, parent, false);
        return new StudyTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudyTaskViewHolder holder, int position) {
        StudyTask task = taskList.get(position);
        
        // Set time range
        holder.textTime.setText(task.getStartTime() + " - " + task.getEndTime());
        
        // Set subject
        holder.textSubject.setText(task.getSubject());
        
        // Set target duration
        holder.textTarget.setText("Target: " + task.getTargetDuration());
        
        // Set status
        holder.textStatus.setText(task.getStatus());
        
        // Set click listener for menu button
        holder.menuButton.setOnClickListener(v -> {
            if (menuClickListener != null) {
                menuClickListener.onMenuClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public StudyTask getTaskAt(int position) {
        if (position >= 0 && position < taskList.size()) {
            return taskList.get(position);
        }
        return null;
    }

    public void updateTasks(List<StudyTask> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    public static class StudyTaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTime;
        TextView textSubject;
        TextView textTarget;
        TextView textStatus;
        ImageView menuButton;

        public StudyTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.textTime);
            textSubject = itemView.findViewById(R.id.textSubject);
            textTarget = itemView.findViewById(R.id.textTarget);
            textStatus = itemView.findViewById(R.id.textStatus);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}
