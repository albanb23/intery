package com.albaburdallo.intery.model;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class Base {

    private Calendar created;
    private User user;

    public Base(Calendar created, User user) {
        this.created = created;
        this.user = user;
    }

    public Base() {
        this.created = Calendar.getInstance();
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
