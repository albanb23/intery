package com.albaburdallo.intery.model.entities;

public class Calendar extends Base{

    private String name;
    private String description;
    private String color;

    public Calendar(String name, String description, String color) {
        super();
        this.name = name;
        this.description = description;
        this.color = color;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
