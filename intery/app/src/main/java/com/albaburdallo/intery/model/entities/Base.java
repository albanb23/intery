package com.albaburdallo.intery.model.entities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
