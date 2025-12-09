package com.shivprakash.to_dolist;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.widget.ImageView;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.TextView;



public class login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle; // btnGoogle = tombol Sign Up

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Cek apakah user sudah login (biar gak login ulang)
        if (AuthManager.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // Inisialisasi elemen dari XML
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        ImageView ivTogglePasswordLogin = findViewById(R.id.ivTogglePasswordLogin);

        ivTogglePasswordLogin.setOnClickListener(new View.OnClickListener() {
            boolean isVisible = false;

            @Override
            public void onClick(View v) {
                if (isVisible) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivTogglePasswordLogin.setImageResource(R.drawable.ic_visibility_off);
                    isVisible = false;
                } else {
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivTogglePasswordLogin.setImageResource(R.drawable.ic_visibility);
                    isVisible = true;
                }

                // biar kursor tetap di akhir teks
                etPassword.setSelection(etPassword.getText().length());
            }
        });


        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);

        // Tombol "Forgot Password" → pindah ke halaman ForgotPasswordActivity
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });




        // Tombol Login → panggil API
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(login.this, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🔥 Panggil API Retrofit
                ApiService api = ApiClient.getClient().create(ApiService.class);
                api.login(email, pass).enqueue(new retrofit2.Callback<LoginResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<LoginResponse> call,
                                           retrofit2.Response<LoginResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            LoginResponse r = resp.body();
                            if ("success".equalsIgnoreCase(r.getStatus())) {

                                // ✅ Simpan session pakai AuthManager
                                AuthManager.saveSession(
                                        login.this,
                                        r.getUser_id(),
                                        r.getFull_name(),
                                        r.getEmail()

                                );

                                Toast.makeText(login.this, "Login berhasil", Toast.LENGTH_SHORT).show();

                                // Masuk ke MainActivity
                                startActivity(new Intent(login.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(login.this, r.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String raw = "";
                            try {
                                raw = resp.errorBody() != null ? resp.errorBody().string() : "";
                            } catch (Exception ignored) { }
                            Toast.makeText(login.this, "HTTP " + resp.code() + " " + raw, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(login.this, "Network: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnGoogle = findViewById(R.id.btnGoogle);

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, singup.class);
                startActivity(intent);
            }
        });

    }
}
