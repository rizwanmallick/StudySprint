package com.example.mad_theory;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDBhelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "focus_tasks.db";
    private static final int DB_VERSION = 2;

    public TaskDBhelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTasksTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Preserve existing data; add tables incrementally
        if (oldVersion < 2) {
            createTasksTable(db);
        }
    }

    private void createTasksTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "subject TEXT," +
                "date TEXT," +
                "start_time TEXT," +
                "end_time TEXT," +
                "duration TEXT," +
                "target_duration TEXT," +
                "status TEXT DEFAULT 'Pending'," +
                "is_complete INTEGER DEFAULT 0)");
    }

    public void insertTask(String subject, String date, String start, String end) {
        long durationMillis = timeDiff(start, end);
        String duration = String.format("%d hr %d min", durationMillis / 60, durationMillis % 60);
        String targetDuration = duration; // Use calculated duration as target

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("subject", subject);
        cv.put("date", date);
        cv.put("start_time", start);
        cv.put("end_time", end);
        cv.put("duration", duration);
        cv.put("target_duration", targetDuration);
        cv.put("status", "Pending");
        db.insert("tasks", null, cv);
    }

    // Add new method to insert StudyTask
    public long insertStudyTask(StudyTask task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("subject", task.getSubject());
        cv.put("date", task.getDate());
        cv.put("start_time", task.getStartTime());
        cv.put("end_time", task.getEndTime());
        cv.put("duration", task.getTargetDuration());
        cv.put("target_duration", task.getTargetDuration());
        cv.put("status", task.getStatus());
        cv.put("is_complete", task.getStatus().equals("Completed") ? 1 : 0);
        return db.insert("tasks", null, cv);
    }

    private long timeDiff(String start, String end) {
        try {
            String[] s = start.split(":");
            String[] e = end.split(":");
            int startMin = Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
            int endMin = Integer.parseInt(e[0]) * 60 + Integer.parseInt(e[1]);
            return endMin - startMin;
        } catch (Exception ex) {
            return 0;
        }
    }

    public interface TaskCallback {
        void onTask(int id, String subject, String date, String start, String end, String duration);
    }

    public void getAllTasks(TaskCallback callback) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tasks", null);
        while (c.moveToNext()) {
            callback.onTask(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5)
            );
        }
        c.close();
    }

    public void markComplete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE tasks SET is_complete=1 WHERE id=" + id);
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM tasks WHERE id=" + id);
    }

    // Methods needed by StudyPlanActivity
    public List<StudyTask> getAllTasksForToday() {
        List<StudyTask> taskList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        
        Cursor cursor = db.rawQuery("SELECT * FROM tasks WHERE date = ? ORDER BY start_time", 
                new String[]{today});
        
        while (cursor.moveToNext()) {
            StudyTask task = new StudyTask();
            task.setId(cursor.getInt(0));
            task.setSubject(cursor.getString(1));
            task.setDate(cursor.getString(2));
            task.setStartTime(cursor.getString(3));
            task.setEndTime(cursor.getString(4));
            task.setTargetDuration(cursor.getString(6));
            task.setStatus(cursor.getString(7));
            taskList.add(task);
        }
        
        cursor.close();
        return taskList;
    }

    public void markTaskAsComplete(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", "Completed");
        cv.put("is_complete", 1);
        db.update("tasks", cv, "id = ?", new String[]{String.valueOf(taskId)});
    }

    public void markTaskAsPending(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", "Pending");
        cv.put("is_complete", 0);
        db.update("tasks", cv, "id = ?", new String[]{String.valueOf(taskId)});
    }

    // Methods for Dashboard
    public int getTaskCountForToday() {
        SQLiteDatabase db = getReadableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks WHERE date = ?", new String[]{today});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalStudyTimeForToday() {
        SQLiteDatabase db = getReadableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        
        Cursor cursor = db.rawQuery("SELECT SUM(CASE WHEN is_complete = 1 THEN " +
                "CAST(REPLACE(REPLACE(duration, ' hr ', ':'), ' min', '') AS INTEGER) ELSE 0 END) FROM tasks WHERE date = ?", 
                new String[]{today});
        
        int totalMinutes = 0;
        if (cursor.moveToFirst()) {
            totalMinutes = cursor.getInt(0);
        }
        cursor.close();
        return totalMinutes;
    }

    public int getCompletedTaskCountForToday() {
        SQLiteDatabase db = getReadableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks WHERE date = ? AND is_complete = 1", 
                new String[]{today});
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Robust helpers for Attendance: compute minutes by parsing target_duration strings
    public int getTodayGoalMinutes() {
        List<StudyTask> tasks = getAllTasksForToday();
        int total = 0;
        for (StudyTask t : tasks) {
            total += parseDurationMinutes(t.getTargetDuration());
        }
        return total;
    }

    public int getTodayStudiedMinutes() {
        SQLiteDatabase db = getReadableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        Cursor cursor = db.rawQuery("SELECT target_duration FROM tasks WHERE date = ? AND is_complete = 1",
                new String[]{today});
        int total = 0;
        while (cursor.moveToNext()) {
            total += parseDurationMinutes(cursor.getString(0));
        }
        cursor.close();
        return total;
    }

    private int parseDurationMinutes(String duration) {
        if (duration == null) return 0;
        try {
            String d = duration.trim();
            if (d.contains("h")) {
                String[] parts = d.split("h");
                int h = Integer.parseInt(parts[0].trim());
                int m = 0;
                if (parts.length > 1) {
                    String rem = parts[1].replace("m", "").replace("min", "").trim();
                    if (!rem.isEmpty()) m = Integer.parseInt(rem);
                }
                return h * 60 + m;
            }
            if (d.contains("min")) {
                return Integer.parseInt(d.replace("min", "").trim());
            }
            if (d.endsWith("m")) {
                return Integer.parseInt(d.replace("m", "").trim());
            }
        } catch (Exception ignored) {}
        return 0;
    }

    // Progress methods
    public int getTotalStudyTimeThisWeek() {
        SQLiteDatabase db = getReadableDatabase();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        String weekStart = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
        
        int totalMinutes = 0;
        Cursor cursor = db.rawQuery("SELECT target_duration FROM tasks WHERE date >= ? AND is_complete = 1", 
                new String[]{weekStart});
        while (cursor.moveToNext()) {
            String duration = cursor.getString(0);
            totalMinutes += parseDurationMinutes(duration);
        }
        cursor.close();
        return totalMinutes;
    }

    public int getCompletedTasksThisWeek() {
        SQLiteDatabase db = getReadableDatabase();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        String weekStart = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
        
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks WHERE date >= ? AND is_complete = 1", 
                new String[]{weekStart});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalTasksThisWeek() {
        SQLiteDatabase db = getReadableDatabase();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        String weekStart = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
        
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks WHERE date >= ?", 
                new String[]{weekStart});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getStudyStreak() {
        SQLiteDatabase db = getReadableDatabase();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int streak = 0;
        boolean continueStreak = true;
        
        while (continueStreak) {
            String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks WHERE date = ? AND is_complete = 1", 
                    new String[]{dateStr});
            boolean hasCompleted = false;
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                hasCompleted = true;
            }
            cursor.close();
            
            if (hasCompleted) {
                streak++;
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            } else {
                continueStreak = false;
            }
        }
        return streak;
    }

    public static class WeeklyProgress {
        public String day;
        public int studyMinutes;
        
        public WeeklyProgress(String day, int minutes) {
            this.day = day;
            this.studyMinutes = minutes;
        }
    }

    public List<WeeklyProgress> getWeeklyBreakdown() {
        List<WeeklyProgress> weeklyData = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        
        for (int i = 0; i < 7; i++) {
            String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday
            if (dayOfWeek < 0) dayOfWeek = 6;
            
            Cursor cursor = db.rawQuery("SELECT target_duration FROM tasks WHERE date = ? AND is_complete = 1", 
                    new String[]{dateStr});
            int totalMinutes = 0;
            while (cursor.moveToNext()) {
                totalMinutes += parseDurationMinutes(cursor.getString(0));
            }
            cursor.close();
            
            weeklyData.add(new WeeklyProgress(dayNames[dayOfWeek], totalMinutes));
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
        
        return weeklyData;
    }

    public static class SubjectProgress {
        public String subject;
        public int studyMinutes;
        public int completedTasks;
        
        public SubjectProgress(String subject, int minutes, int tasks) {
            this.subject = subject;
            this.studyMinutes = minutes;
            this.completedTasks = tasks;
        }
    }

    public List<SubjectProgress> getSubjectPerformance() {
        List<SubjectProgress> subjectData = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.rawQuery("SELECT subject, target_duration FROM tasks WHERE is_complete = 1", null);
        
        java.util.Map<String, Integer> subjectMinutes = new java.util.HashMap<>();
        java.util.Map<String, Integer> subjectTasks = new java.util.HashMap<>();
        
        while (cursor.moveToNext()) {
            String subject = cursor.getString(0);
            String duration = cursor.getString(1);
            int minutes = parseDurationMinutes(duration);
            
            subjectMinutes.put(subject, subjectMinutes.getOrDefault(subject, 0) + minutes);
            subjectTasks.put(subject, subjectTasks.getOrDefault(subject, 0) + 1);
        }
        cursor.close();
        
        for (String subject : subjectMinutes.keySet()) {
            subjectData.add(new SubjectProgress(subject, subjectMinutes.get(subject), subjectTasks.get(subject)));
        }
        
        // Sort by study minutes descending
        subjectData.sort((a, b) -> Integer.compare(b.studyMinutes, a.studyMinutes));
        
        return subjectData;
    }
}
