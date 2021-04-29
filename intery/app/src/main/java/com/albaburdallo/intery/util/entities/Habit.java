package com.albaburdallo.intery.util.entities;

import java.util.Date;

public class Habit extends Base {

    private String id;
    private String name;
    private String notes;
    private Date startDate;
    private String color;
    private Boolean notifyMe;
    private Date when;
    private Integer period;
    private Integer times;
    private Double progress;
    private Date updated;
    private String daysCompleted;

    public Habit() {

    }

    public Habit(String id, String name, String notes, Date startDate, String color, Boolean notifyMe,
                 Date when, Integer period, Integer times, Double progress, Date updated, String daysCompleted) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.name = name;
        this.notes = notes;
        this.startDate = startDate;
        this.color = color;
        this.notifyMe = notifyMe;
        this.when = when;
        this.period = period;
        this.times = times;
        this.progress = progress;
        this.updated = updated;
        this.daysCompleted = daysCompleted;
    }

    public Habit(String id, String name, String notes, Date startDate, String color, Boolean notifyMe,
                 Integer period, Integer times, Double progress, Date updated, String daysCompleted) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.name = name;
        this.notes = notes;
        this.startDate = startDate;
        this.color = color;
        this.notifyMe = notifyMe;
        this.period = period;
        this.times = times;
        this.progress = progress;
        this.updated = updated;
        this.daysCompleted = daysCompleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getNotifyMe() {
        return notifyMe;
    }

    public void setNotifyMe(Boolean notifyMe) {
        this.notifyMe = notifyMe;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getDaysCompleted() {
        return daysCompleted;
    }

    public void setDaysCompleted(String daysCompleted) {
        this.daysCompleted = daysCompleted;
    }
}