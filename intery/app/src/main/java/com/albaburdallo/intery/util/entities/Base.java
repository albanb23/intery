package com.albaburdallo.intery.util.entities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public class Base {

    private Date created;
    private User user;

    public Base() {

    }

    public Base(Date calendar) {
        this.created = calendar;
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        String email = u.getEmail();
        String name = u.getDisplayName();
        String photo = "";
        if (u.getPhotoUrl()!=null){
            photo = u.getPhotoUrl().toString();
        }
        this.user = new User(name, "", email, photo);
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
