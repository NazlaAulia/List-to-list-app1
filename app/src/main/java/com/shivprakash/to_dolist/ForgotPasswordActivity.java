package com.shivprakash.to_dolist;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmailForgot;
    Button btnSendResetLink;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmailForgot = findViewById(R.id.etEmailForgot);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        progressDialog = new ProgressDialog(this);

        btnSendResetLink.setOnClickListener(v -> {
            String email = etEmailForgot.getText().toString().trim();
            if (email.isEmpty()) {
                etEmailForgot.setError("Email is required");
                return;
            }

            // 🔒 Matikan tombol biar gak diklik dua kali
            btnSendResetLink.setEnabled(false);
            btnSendResetLink.setText("Processing...");

            sendResetLink(email);
        });

    }

    private void sendResetLink(String email) {
        progressDialog.setMessage("Sending reset link...");
        progressDialog.show();

        String url = "http://10.205.217.141/todo_api/forgot_password.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    btnSendResetLink.setEnabled(true);
                    btnSendResetLink.setText("Send Reset Link");


                    try {
                        JSONObject json = new JSONObject(response);
                        String status = json.getString("status");

                        if (status.equals("success")) {
                            String link = json.optString("link", "");

                            Toast.makeText(this, "Link reset berhasil dibuat!", Toast.LENGTH_SHORT).show();

                            // 🔥 Jika link ada, buka di browser
                            if (!link.isEmpty()) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                startActivity(browserIntent);
                            } else {
                                Toast.makeText(this, "Link tidak ditemukan di respons server.", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Response error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}