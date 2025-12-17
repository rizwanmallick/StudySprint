package com.example.mad_theory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubjectProgressAdapter extends RecyclerView.Adapter<SubjectProgressAdapter.SubjectViewHolder> {

    private List<TaskDBhelper.SubjectProgress> subjectData;

    public SubjectProgressAdapter() {
        this.subjectData = new ArrayList<>();
    }

    public void updateData(List<TaskDBhelper.SubjectProgress> data) {
        this.subjectData = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject_progress, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        TaskDBhelper.SubjectProgress progress = subjectData.get(position);
        
        holder.tvSubject.setText(progress.subject);
        
        String tasksText = progress.completedTasks + (progress.completedTasks == 1 ? " task completed" : " tasks completed");
        holder.tvTasks.setText(tasksText);
        
        // Format time
        int hours = progress.studyMinutes / 60;
        int minutes = progress.studyMinutes % 60;
        String timeText;
        if (hours > 0) {
            timeText = String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        } else {
            timeText = String.format(Locale.getDefault(), "%dm", minutes);
        }
        holder.tvStudyTime.setText(timeText);
    }

    @Override
    public int getItemCount() {
        return subjectData.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject;
        TextView tvTasks;
        TextView tvStudyTime;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvTasks = itemView.findViewById(R.id.tvTasks);
            tvStudyTime = itemView.findViewById(R.id.tvStudyTime);
        }
    }
}
