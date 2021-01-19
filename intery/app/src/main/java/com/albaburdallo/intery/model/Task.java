package com.albaburdallo.intery.model;

import com.google.type.DateTime;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Task implements Serializable {

    private String name;
    private boolean done;
    private Calendar start;
    private Calendar end;
    private Boolean allDay;
    private String description;
    private Calendar created;
    //calendario
    //tarea padre

    public Task() {

    }

    public Task(String name) {
        this.name = name;
        this.done = false;
        this.created = java.util.Calendar.getInstance();
    }

    public Task(String name, Calendar start, Calendar end, Boolean allDay, String description) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
        this.description = description;
        this.done = false;
        this.created = java.util.Calendar.getInstance();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", this.name);
        map.put("start", this.start);
        map.put("end", this.end);
        map.put("allDay", this.allDay);
        map.put("description", this.description);
        map.put("name", this.name);
        map.put("created", this.created);
        return map;
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

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public Calendar getEnd() {
        return end;
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", done=" + done +
                ", start=" + start +
                ", end=" + end +
                ", allDay=" + allDay +
                ", description='" + description + '\'' +
                '}';
    }
}
