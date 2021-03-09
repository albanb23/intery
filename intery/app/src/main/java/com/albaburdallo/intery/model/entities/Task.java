package com.albaburdallo.intery.model.entities;

import java.util.Date;

public class Task extends Base {
    private String id;
    private String name;
    private boolean done;
    private Date startDate;
    private Date startTime;
    private Date endDate;
    private Date endTime;
    private Boolean allDay;
    private Boolean notifyMe;
    private String notes;
    //calendario
    private Calendar calendar;

    public Task() {

    }

    public Task(String id, String name, Date startDate, Boolean allDay, Boolean notifyMe, String notes, Boolean done, Calendar calendar) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.allDay = allDay;
        this.notifyMe = notifyMe;
        this.notes = notes;
        this.done = done;
        this.calendar = calendar;
    }

    public Task(String id, String name, Date startDate, Date startTime, Date endDate, Date endTime, Boolean allDay, Boolean notifyMe, String notes, Boolean done, Calendar calendar) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.allDay = allDay;
        this.notifyMe = notifyMe;
        this.notes = notes;
        this.done = done;
        this.calendar = calendar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getNotifyMe() {
        return notifyMe;
    }

    public void setNotifyMe(Boolean notifyMe) {
        this.notifyMe = notifyMe;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", startDate=" + startDate +
                ", startTime=" + startTime +
                ", endDate=" + endDate +
                ", endTime=" + endTime +
                '}';
    }
}
