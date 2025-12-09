package com.shivprakash.to_dolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.ImageView;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import org.json.JSONObject;

public class singup extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword;
    private Button btnRegister, btnGoogle; // btnGoogle = balik ke login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        ImageView ivTogglePasswordSignup = findViewById(R.id.ivTogglePasswordSignup);

        ivTogglePasswordSignup.setOnClickListener(new View.OnClickListener() {
            boolean isVisible = false;

            @Override
            public void onClick(View v) {
                if (isVisible) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivTogglePasswordSignup.setImageResource(R.drawable.ic_visibility_off);
                    isVisible = false;
                } else {
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivTogglePasswordSignup.setImageResource(R.drawable.ic_visibility);
                    isVisible = true;
                }
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        btnRegister = findViewById(R.id.btnRegister);


        // Tombol Sign Up → kirim ke server
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(singup.this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                ApiService api = ApiClient.getClient().create(ApiService.class);
                api.signup(name, email, pass).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                // ambil isi respon dari server (JSON)
                                String resString = response.body().string();
                                JSONObject json = new JSONObject(resString);

                                String status = json.getString("status");
                                String message = json.getString("message");

                                if (status.equals("success")) {
                                    Toast.makeText(singup.this, message, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(singup.this, login.class));
                                    finish();
                                } else {
                                    Toast.makeText(singup.this, message, Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                Toast.makeText(singup.this, "Response parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(singup.this, "Gagal Sign Up (server error)", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(singup.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


    }
}
