package com.example.mad_theory;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudyPlanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudyTaskAdapter adapter;
    private TaskDBhelper dbHelper; // Your database helper for tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        SettingsActivity.applySavedTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_plan);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new TaskDBhelper(this);
        List<StudyTask> taskList = dbHelper.getAllTasksForToday(); // Implement this based on your DB
        adapter = new StudyTaskAdapter(taskList, new StudyTaskAdapter.OnMenuClickListener() {
            @Override
            public void onMenuClick(View view, int position) {
                showPopupMenu(view, position);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_task_item); // You need to create this menu in res/menu/
        popupMenu.setOnMenuItemClickListener(item -> {
            StudyTask selectedTask = adapter.getTaskAt(position);
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                // Handle edit
                Toast.makeText(this, "Edit: "+selectedTask.getSubject(), Toast.LENGTH_SHORT).show();
                // Launch add/edit task activity with this task's data
            } else if (id == R.id.action_mark_complete) {
                // Handle mark as complete
                dbHelper.markTaskAsComplete(selectedTask.getId());
                adapter.updateTasks(dbHelper.getAllTasksForToday());
            } else if (id == R.id.action_delete) {
                // Handle delete
                dbHelper.deleteTask(selectedTask.getId());
                adapter.updateTasks(dbHelper.getAllTasksForToday());
            }
            return true;
        });
        popupMenu.show();
    }
}