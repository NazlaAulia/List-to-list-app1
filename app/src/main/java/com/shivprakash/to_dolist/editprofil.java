package com.shivprakash.to_dolist;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import com.shivprakash.to_dolist.models.UserProfileResponse;
import com.shivprakash.to_dolist.models.GenericResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class editprofil extends AppCompatActivity {

    EditText etName, etEmail;
    Button btnBack; // Tombol Simpan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofil);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setText("Simpan");

        // 🔹 tampilkan data dari sesi dulu (langsung muncul)
        etName.setText(AuthManager.getUserName(this));
        etEmail.setText(AuthManager.getUserEmail(this));

        // 🔹 sambil update dari server di background
        loadUserProfile();

        btnBack.setOnClickListener(v -> updateUserProfile());
    }


    // 🔹 Ambil data profil user dari get_user_profile.php
    private void loadUserProfile() {
        int userId = AuthManager.getUserId(this);
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getUserProfile(userId).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse user = response.body();

                    if ("success".equalsIgnoreCase(user.getStatus())) {
                        etName.setText(user.getName());
                        etEmail.setText(user.getEmail());
                    } else {
                        Toast.makeText(editprofil.this, user.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(editprofil.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Toast.makeText(editprofil.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Kirim perubahan ke update_user_profile.php
    private void updateUserProfile() {
        int userId = AuthManager.getUserId(this);
        String fullName = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.updateUserProfile(userId, fullName, email, "").enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse res = response.body();
                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        Toast.makeText(editprofil.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();

                        // Simpan sesi terbaru
                        AuthManager.saveSession(editprofil.this, userId, fullName, email);
                        finish();
                    } else {
                        Toast.makeText(editprofil.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(editprofil.this, "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(editprofil.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
