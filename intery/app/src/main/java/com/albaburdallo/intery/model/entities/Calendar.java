package com.albaburdallo.intery.model.entities;

public class Calendar extends Base{

    private String id;
    private String name;
    private String description;
    private String color;
    private Boolean def;

    public Calendar() {

    }

    public Calendar(String id, String name, String description, String color) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.def = false;
    }

    public Calendar(String id, String name, String description, String color, Boolean def) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.def = def;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getDef() {
        return def;
    }

    public void setDef(Boolean def) {
        this.def = def;
    }

    @Override
    public String toString() {
        return "Calendar{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
