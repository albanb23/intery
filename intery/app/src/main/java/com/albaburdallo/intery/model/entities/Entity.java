package com.albaburdallo.intery.model.entities;

public class Entity extends Base {

    private String id;
    private String name;

    public Entity() {

    }

    public Entity(String id, String name) {
        super(java.util.Calendar.getInstance().getTime());
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
