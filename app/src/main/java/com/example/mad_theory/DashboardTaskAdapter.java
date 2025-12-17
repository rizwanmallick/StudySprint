package com.example.mad_theory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DashboardTaskAdapter extends RecyclerView.Adapter<DashboardTaskAdapter.TaskViewHolder> {

    private List<StudyTask> taskList;

    public DashboardTaskAdapter() {
        this.taskList = new java.util.ArrayList<>();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        StudyTask task = taskList.get(position);
        
        holder.textTaskSubject.setText(task.getSubject());
        holder.textTaskTime.setText(task.getStartTime() + " - " + task.getEndTime());
        holder.textTaskStatus.setText(task.getStatus());
        
        // Set status color
        if (task.getStatus().equals("Completed")) {
            holder.textTaskStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_light, null));
        } else {
            holder.textTaskStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light, null));
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<StudyTask> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTaskSubject;
        TextView textTaskTime;
        TextView textTaskStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTaskSubject = itemView.findViewById(R.id.textTaskSubject);
            textTaskTime = itemView.findViewById(R.id.textTaskTime);
            textTaskStatus = itemView.findViewById(R.id.textTaskStatus);
        }
    }
}
