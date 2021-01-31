package com.albaburdallo.intery.model.entities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class Base {

    private Calendar created;
    private User user;

    public Base() {
        if (this.created == null) {
            this.created = Calendar.getInstance();
        }
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        String email = u.getEmail();
        String name = u.getDisplayName();
        String photo = u.getPhotoUrl().toString();
        this.user = new User(name, "", email, photo);
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
