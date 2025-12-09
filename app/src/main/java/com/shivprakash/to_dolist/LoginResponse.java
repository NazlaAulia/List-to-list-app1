package com.shivprakash.to_dolist;

public class LoginResponse {
    private String status;
    private int user_id;
    private String full_name;
    private String email;
    private String message;

    public String getStatus() { return status; }
    public int getUser_id() { return user_id; }
    public String getFull_name() { return full_name; }
    public String getEmail() { return email; }
    public String getMessage() { return message; }
}
