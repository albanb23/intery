package com.albaburdallo.intery.util.entities;

import java.util.Date;

public class Habit extends Base{

    private String id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private String color;
    private Boolean notifyMe;
    private Date when;
    private Integer frequency;

    public Habit() {

    }

    public Habit(String id, String name, String description, Date startDate, Date endDate, String color, Boolean notifyMe, Date when, Integer frequency) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.color = color;
        this.notifyMe = notifyMe;
        this.when = when;
        this.frequency = frequency;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    @Override
    public String toString() {
        return "Habit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", color='" + color + '\'' +
                ", notifyMe=" + notifyMe +
                ", when=" + when +
                ", frequency=" + frequency +
                '}';
    }
}
