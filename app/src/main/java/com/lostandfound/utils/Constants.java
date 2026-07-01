package com.lostandfound.utils;

/**
 * Central place for all string keys used across Firestore, Intents, and SharedPreferences.
 */
public class Constants {

    public static final String COLLECTION_USERS    = "users";
    public static final String COLLECTION_ITEMS    = "items";
    public static final String COLLECTION_CHATS    = "chats";
    public static final String COLLECTION_MESSAGES = "messages";

    public static final String FIELD_USER_ID      = "userId";
    public static final String FIELD_STATUS       = "status";
    public static final String FIELD_CATEGORY     = "category";
    public static final String FIELD_IS_RESOLVED  = "isResolved";
    public static final String FIELD_CREATED_AT   = "createdAt";
    public static final String FIELD_TIMESTAMP    = "timestamp";

    public static final String STATUS_LOST  = "Lost";
    public static final String STATUS_FOUND = "Found";

    public static final String[] CATEGORIES = {
            "Electronics", "Documents", "Keys", "Clothing", "Pets", "Others"
    };

    public static final String STORAGE_ITEMS_PATH   = "item_images/";
    public static final String STORAGE_AVATARS_PATH = "profile_avatars/";

    public static final String EXTRA_ITEM_ID   = "extra_item_id";
    public static final String EXTRA_CHAT_ID   = "extra_chat_id";
    public static final String EXTRA_USER_ID   = "extra_user_id";
    public static final String EXTRA_USER_NAME = "extra_user_name";

    public static final int REQUEST_IMAGE_GALLERY  = 1001;
    public static final int REQUEST_IMAGE_CAMERA   = 1002;
    public static final int REQUEST_LOCATION_PERM  = 1003;

    public static final String PREF_NAME        = "LostFoundPrefs";
    public static final String PREF_USER_NAME   = "pref_user_name";
    public static final String PREF_USER_AVATAR = "pref_user_avatar";

    private Constants() {}
}
