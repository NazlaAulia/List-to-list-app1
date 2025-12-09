package com.shivprakash.to_dolist.models;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("user_id")
    private int user_id;

    @SerializedName("name")
    private String name;

    @SerializedName("username")
    private String username; // ini sebenarnya password dari database

    @SerializedName("email")
    private String email;

    @SerializedName("profile_image")
    private String profile_image; // 🔹 tambahkan ini biar Glide tetap bisa pakai URL file upload

    @SerializedName("foto")
    private String foto; // 🔹 Base64 string dari kolom BLOB

    // === GETTER ===

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public String getFoto() {
        return foto;
    }
}
