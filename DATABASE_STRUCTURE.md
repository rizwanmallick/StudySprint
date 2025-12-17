# Database Structure & Data Flow

## **Number of Tables**

Your project has **2 separate SQLite databases**. Active database has **2 tables**:

### **Database 1: `focus_tasks.db`** (TaskDBhelper)
- **Table Names**: `tasks`, `users`
- **File**: `TaskDBhelper.java`
- **Used By**: Main application (AddTaskActivity, StudyPlanActivity, DashboardActivity, AttendanceActivity, ProgressActivity) and authentication (LoginActivity, SignupActivity)

### **Database 2: `studyplanner.db`** (DatabaseHelper)
- **Table Name**: `tasks`
- **File**: `DatabaseHelper.java`
- **Status**: Legacy database (not actively used in current implementation)

---

## **Table Schema: `tasks` (in focus_tasks.db)**

```sql
CREATE TABLE tasks(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject TEXT,
    date TEXT,
    start_time TEXT,
    end_time TEXT,
    duration TEXT,
    target_duration TEXT,
    status TEXT DEFAULT 'Pending',
    is_complete INTEGER DEFAULT 0
)
```

**Columns Explanation:**
- `id` - Unique identifier (auto-incremented)
- `subject` - Subject name (e.g., "Mathematics", "Physics")
- `date` - Date in format "yyyy-MM-dd"
- `start_time` - Start time in format "HH:mm" (e.g., "14:00")
- `end_time` - End time in format "HH:mm" (e.g., "15:30")
- `duration` - Calculated duration (e.g., "60 min")
- `target_duration` - Target study duration (e.g., "60 min")
- `status` - Task status ("Pending" or "Completed")
- `is_complete` - Completion flag (0 = not complete, 1 = complete)

---

## **Table Schema: `users` (in focus_tasks.db)**

```sql
CREATE TABLE users(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at INTEGER DEFAULT (strftime('%s','now'))
)
```

**Used For Authentication:**
- Sign up inserts a new row (name, email, password_hash).
- Sign in verifies email + password_hash.
- Session is cached in `UserPrefs` (user_id, name, email) after successful auth.

---

## **Data Saving Flow - By User Action**

### **1. Adding a New Task** 📝
**Action**: User clicks "Add" button in `AddTaskActivity`

**Location**: `AddTaskActivity.java` → `btnAdd.setOnClickListener()`

**Data Saved**:
```java
StudyTask studyTask = new StudyTask();
studyTask.setSubject(subject);           // From spinner selection
studyTask.setDate(currentDate);          // Current date (yyyy-MM-dd)
studyTask.setStartTime(startTime);       // Selected start time (HH:mm)
studyTask.setEndTime(endTime);           // Selected end time (HH:mm)
studyTask.setTargetDuration(durationMinutes + " min"); // Calculated duration
studyTask.setStatus("Pending");          // Default status

taskDb.insertStudyTask(studyTask);       // Saves to database
```

**Database Method**: `TaskDBhelper.insertStudyTask()`

**Fields Inserted**:
- ✅ Subject (from dropdown/custom)
- ✅ Date (current date)
- ✅ Start time (user selected)
- ✅ End time (user selected)
- ✅ Duration (calculated)
- ✅ Target duration (calculated)
- ✅ Status = "Pending"
- ✅ is_complete = 0

---

### **2. Marking Task as Complete (Study Plan)** ✅
**Action**: User clicks 3-dot menu → "Mark as Complete" in `StudyPlanActivity`

**Location**: `StudyPlanActivity.java` → `popupMenu.setOnMenuItemClickListener()` → `action_mark_complete`

**Data Updated**:
```java
dbHelper.markTaskAsComplete(selectedTask.getId());
```

**Database Method**: `TaskDBhelper.markTaskAsComplete()`

**Fields Updated**:
- ✅ status = "Completed"
- ✅ is_complete = 1

---

### **3. Marking Tasks as Complete (Attendance Checkout)** ✅
**Action**: User clicks "Check Out" → Selects subjects → Clicks "Mark Completed" in `AttendanceActivity`

**Location**: `AttendanceActivity.java` → `checkOut()` → `showCompletedSubjectsDialog()` → `markTasksAsCompleted()`

**Data Updated**:
```java
for (StudyTask task : completedTasks) {
    taskDb.markTaskAsComplete(task.getId());
}
```

**Database Method**: `TaskDBhelper.markTaskAsComplete()`

**Fields Updated**:
- ✅ status = "Completed" (for selected tasks)
- ✅ is_complete = 1 (for selected tasks)

**Note**: This allows marking multiple tasks as complete at once after checkout.

---

### **4. Deleting a Task** 🗑️
**Action**: User clicks 3-dot menu → "Delete" in `StudyPlanActivity`

**Location**: `StudyPlanActivity.java` → `popupMenu.setOnMenuItemClickListener()` → `action_delete`

**Data Deleted**:
```java
dbHelper.deleteTask(selectedTask.getId());
```

**Database Method**: `TaskDBhelper.deleteTask()`

**Action**: Entire row is deleted from `tasks` table

---

### **5. Editing a Task** ✏️
**Action**: User clicks 3-dot menu → "Edit" in `StudyPlanActivity`

**Location**: `StudyPlanActivity.java` → `popupMenu.setOnMenuItemClickListener()` → `action_edit`

**Status**: ⚠️ Currently shows a toast message only. Edit functionality is not fully implemented yet.

---

## **Data Reading Flow**

### **Dashboard Activity**
- **Method**: `TaskDBhelper.getTaskCountForToday()` - Gets count of today's tasks
- **Method**: `TaskDBhelper.getTotalStudyTimeForToday()` - Gets total study time for today
- **Method**: `TaskDBhelper.getCompletedTaskCountForToday()` - Gets completed tasks count
- **Method**: `TaskDBhelper.getAllTasksForToday()` - Gets all today's tasks for display

### **Study Plan Activity**
- **Method**: `TaskDBhelper.getAllTasksForToday()` - Gets all tasks for today

### **Attendance Activity**
- **Method**: `TaskDBhelper.getAllTasksForToday()` - Gets tasks to calculate goal time
- **Method**: `TaskDBhelper.getTodayGoalMinutes()` - Calculates total goal minutes
- **Method**: `TaskDBhelper.getTodayStudiedMinutes()` - Gets studied minutes

### **Progress Activity**
- **Method**: `TaskDBhelper.getTotalStudyTimeThisWeek()` - Weekly study time
- **Method**: `TaskDBhelper.getCompletedTasksThisWeek()` - Weekly completed tasks
- **Method**: `TaskDBhelper.getTotalTasksThisWeek()` - Total weekly tasks
- **Method**: `TaskDBhelper.getStudyStreak()` - Calculates study streak
- **Method**: `TaskDBhelper.getWeeklyBreakdown()` - Daily breakdown for week
- **Method**: `TaskDBhelper.getSubjectPerformance()` - Subject-wise statistics

---

## **Summary Table**

| User Action | Screen | Database Method | Data Saved/Updated |
|------------|--------|----------------|-------------------|
| Click "Add" button | AddTaskActivity | `insertStudyTask()` | New task row (subject, date, times, duration, status) |
| Click "Mark as Complete" | StudyPlanActivity | `markTaskAsComplete()` | Updates: status="Completed", is_complete=1 |
| Click "Mark Completed" after checkout | AttendanceActivity | `markTaskAsComplete()` (multiple) | Updates: status="Completed", is_complete=1 (for selected tasks) |
| Click "Delete" | StudyPlanActivity | `deleteTask()` | Deletes entire row |
| Click "Edit" | StudyPlanActivity | (Not implemented) | - |

---

## **Important Notes**

1. **Two Databases**: You have 2 separate databases (`focus_tasks.db` and `studyplanner.db`), but only `focus_tasks.db` is actively used.

2. **No Data on App Launch**: The database is empty when first installed. Tasks are only saved when user adds them.

3. **Date Format**: All dates are stored as "yyyy-MM-dd" (e.g., "2024-01-15")

4. **Time Format**: All times are stored as "HH:mm" (24-hour format, e.g., "14:30")

5. **Completion Tracking**: Tasks can be marked complete in two ways:
   - Individually from Study Plan
   - Batch selection after Attendance checkout

6. **No Edit Function**: Edit functionality is currently not implemented - clicking Edit only shows a toast message.

