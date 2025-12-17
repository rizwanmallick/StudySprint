package com.example.mad_theory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeeklyProgressAdapter extends RecyclerView.Adapter<WeeklyProgressAdapter.WeeklyViewHolder> {

    private List<TaskDBhelper.WeeklyProgress> weeklyData;
    private int maxMinutes;

    public WeeklyProgressAdapter() {
        this.weeklyData = new ArrayList<>();
        this.maxMinutes = 1;
    }

    public void updateData(List<TaskDBhelper.WeeklyProgress> data, int maxMinutes) {
        this.weeklyData = data;
        this.maxMinutes = maxMinutes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeeklyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weekly_progress, parent, false);
        return new WeeklyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeklyViewHolder holder, int position) {
        TaskDBhelper.WeeklyProgress progress = weeklyData.get(position);
        
        holder.tvDay.setText(progress.day);
        
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
        
        // Set progress bar
        int progressPercent = maxMinutes > 0 ? (progress.studyMinutes * 100) / maxMinutes : 0;
        holder.progressBarDay.setProgress(progressPercent);
    }

    @Override
    public int getItemCount() {
        return weeklyData.size();
    }

    public static class WeeklyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        TextView tvStudyTime;
        ProgressBar progressBarDay;

        public WeeklyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvStudyTime = itemView.findViewById(R.id.tvStudyTime);
            progressBarDay = itemView.findViewById(R.id.progressBarDay);
        }
    }
}
