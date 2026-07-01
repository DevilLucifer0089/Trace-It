package com.lostandfound.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Firestore document model for a Lost or Found item.
 * Empty constructor is REQUIRED for Firebase deserialization.
 * All fields use primitive wrappers or String to support null-safety.
 */
public class Item {

    @DocumentId
    private String itemId;

    private String userId;
    private String title;
    private String description;

    private String category;

    private String status;

    private String imageUrl;
    private String dateSpotted;
    private String locationName;

    private double latitude;
    private double longitude;

    private boolean isResolved;

    private String posterName;
    private String posterAvatarUrl;

    @ServerTimestamp
    private Timestamp createdAt;

    public Item() {}

    public Item(String userId, String title, String description, String category,
                String status, String imageUrl, String dateSpotted,
                String locationName, double latitude, double longitude,
                String posterName, String posterAvatarUrl) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.imageUrl = imageUrl;
        this.dateSpotted = dateSpotted;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isResolved = false;
        this.posterName = posterName;
        this.posterAvatarUrl = posterAvatarUrl;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDateSpotted() { return dateSpotted; }
    public void setDateSpotted(String dateSpotted) { this.dateSpotted = dateSpotted; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { isResolved = resolved; }

    public String getPosterName() { return posterName; }
    public void setPosterName(String posterName) { this.posterName = posterName; }

    public String getPosterAvatarUrl() { return posterAvatarUrl; }
    public void setPosterAvatarUrl(String posterAvatarUrl) { this.posterAvatarUrl = posterAvatarUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
