package com.example.mad_theory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CompletedSubjectsAdapter extends RecyclerView.Adapter<CompletedSubjectsAdapter.SubjectViewHolder> {

    private List<StudyTask> subjectList;
    private List<StudyTask> completedTasks;

    public CompletedSubjectsAdapter(List<StudyTask> subjectList) {
        this.subjectList = subjectList;
        this.completedTasks = new ArrayList<>();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_completed_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        StudyTask task = subjectList.get(position);
        
        holder.textSubjectName.setText(task.getSubject());
        holder.textSubjectTime.setText("Time: " + task.getStartTime() + " - " + task.getEndTime());
        holder.textSubjectDuration.setText("Duration: " + task.getTargetDuration());
        
        // Set checkbox state
        holder.checkboxSubject.setChecked(completedTasks.contains(task));
        
        // Handle checkbox click
        holder.checkboxSubject.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!completedTasks.contains(task)) {
                    completedTasks.add(task);
                }
            } else {
                completedTasks.remove(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public List<StudyTask> getCompletedTasks() {
        return completedTasks;
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxSubject;
        TextView textSubjectName;
        TextView textSubjectTime;
        TextView textSubjectDuration;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxSubject = itemView.findViewById(R.id.checkboxSubject);
            textSubjectName = itemView.findViewById(R.id.textSubjectName);
            textSubjectTime = itemView.findViewById(R.id.textSubjectTime);
            textSubjectDuration = itemView.findViewById(R.id.textSubjectDuration);
        }
    }
}
