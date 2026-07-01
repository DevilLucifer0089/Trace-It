package com.lostandfound.utils;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Singleton-style accessor for Firebase service instances.
 */
public class FirebaseHelper {

    private static FirebaseHelper instance;

    private final FirebaseAuth        auth;
    private final FirebaseFirestore   db;
    private final FirebaseStorage     storage;

    private FirebaseHelper() {
        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) instance = new FirebaseHelper();
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }

    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    public boolean isLoggedIn() { return auth.getCurrentUser() != null; }

    public FirebaseFirestore getDb() { return db; }

    public FirebaseStorage getStorage() { return storage; }

    public StorageReference getItemImageRef(String userId, String filename) {
        return storage.getReference()
                .child(Constants.STORAGE_ITEMS_PATH)
                .child(userId)
                .child(filename);
    }

    public StorageReference getAvatarRef(String userId) {
        return storage.getReference()
                .child(Constants.STORAGE_AVATARS_PATH)
                .child(userId)
                .child("avatar.jpg");
    }
}
