package com.albaburdallo.intery.util.entities;


public class User {

    private String name;
    private String surname;
    private String email;
    private String photo;

    public User() {

    }

    public User(String name, String surname, String email, String photo) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
