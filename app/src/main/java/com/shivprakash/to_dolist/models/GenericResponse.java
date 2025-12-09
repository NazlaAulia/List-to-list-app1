package com.shivprakash.to_dolist.models;

import com.google.gson.annotations.SerializedName;

public class GenericResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    // 🔹 Tambahkan getter (ini yang dibutuhkan editprofil.java)
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
