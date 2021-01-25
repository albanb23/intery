package com.albaburdallo.intery.model;

import com.google.type.DateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Task extends Base implements Serializable {

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
    //tarea padre
    private Task mainTask;

    public Task() {
        super();

    }

    public Task(String name, Date startDate, Boolean allDay, Boolean notifyMe, String notes, Boolean done) {
        super();
        this.name = name;
        this.startDate = startDate;
        this.allDay = allDay;
        this.notifyMe = notifyMe;
        this.notes = notes;
        this.done = done;
    }

    public Task(String name, Date startDate, Date startTime, Date endDate, Date endTime, Boolean allDay, Boolean notifyMe, String notes, Boolean done) {
        super();
        this.name = name;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.allDay = allDay;
        this.notifyMe = notifyMe;
        this.notes = notes;
        this.done = done;
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

}
