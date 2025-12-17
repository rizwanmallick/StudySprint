package com.example.mad_theory;

public class StudyTask {
    private int id;
    private String subject;
    private String startTime;
    private String endTime;
    private String targetDuration;
    private String status;
    private String date;

    // Default constructor
    public StudyTask() {
    }

    // Constructor with all parameters
    public StudyTask(int id, String subject, String startTime, String endTime, 
                    String targetDuration, String status, String date) {
        this.id = id;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
        this.targetDuration = targetDuration;
        this.status = status;
        this.date = date;
    }

    // Constructor without ID (for new tasks)
    public StudyTask(String subject, String startTime, String endTime, 
                    String targetDuration, String status, String date) {
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
        this.targetDuration = targetDuration;
        this.status = status;
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTargetDuration() {
        return targetDuration;
    }

    public void setTargetDuration(String targetDuration) {
        this.targetDuration = targetDuration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
