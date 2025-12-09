package com.shivprakash.to_dolist;

import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import de.hdodenhof.circleimageview.CircleImageView;

import com.shivprakash.to_dolist.models.GenericResponse;
import com.shivprakash.to_dolist.models.UserProfileResponse;

public class profil extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private CircleImageView imgProfile;
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        // 🔹 Inisialisasi foto profil & nama user
        imgProfile = findViewById(R.id.imgProfile);
        tvName = findViewById(R.id.tvName);

        // 🔹 Tampilkan nama langsung dari sesi
        tvName.setText(AuthManager.getUserName(this));

        // 🔹 Tampilkan foto profil dari server (kalau sudah pernah upload)
        String imageUrl = "http://10.205.217.141/todo_api/uploads/user_"
                + AuthManager.getUserId(this) + ".jpg";
        Glide.with(this)
                .load(imageUrl + "?t=" + System.currentTimeMillis())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(imgProfile);

        // 🔹 Klik foto profil → buka galeri
        imgProfile.setOnClickListener(v -> openImageChooser());

        // 🔹 Menu Edit Profil
        LinearLayout menuEditProfile = findViewById(R.id.menuEditProfile);
        menuEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(profil.this, editprofil.class);
            startActivity(intent);
        });

        // 🔹 Menu Logout
        LinearLayout menuLogout = findViewById(R.id.menuNotification);
        menuLogout.setOnClickListener(v -> {
            AuthManager.logout(profil.this);
            Intent intent = new Intent(profil.this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 🔹 Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_calendar:
                    startActivity(new Intent(getApplicationContext(), kalender.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_add:
                    startActivity(new Intent(getApplicationContext(), AddTaskActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_history:
                    startActivity(new Intent(getApplicationContext(), riwayat.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_profile:
                    return true;
            }
            return false;
        });
    }

    // 🔹 setiap kali balik ke halaman profil, langsung refresh nama & foto
    @Override
    protected void onResume() {
        super.onResume();

        // ✅ tampilkan nama user langsung dari sesi
        tvName.setText(AuthManager.getUserName(this));

        // ✅ refresh foto terbaru (biar gak delay)
        String imageUrl = "http://10.205.217.141/todo_api/uploads/user_"
                + AuthManager.getUserId(this) + ".jpg";
        Glide.with(this)
                .load(imageUrl + "?t=" + System.currentTimeMillis())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(imgProfile);
    }

    // 🔹 Fungsi untuk buka galeri pilih foto
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // 🔹 Hasil pemilihan gambar dari galeri
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
            uploadImageToServer(imageUri);
        }
    }

    // 🔹 Upload foto ke server
    private void uploadImageToServer(Uri uri) {
        try {
            File file = new File(FileUtils.getPath(this, uri));
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("profile_image", file.getName(), requestBody);

            RequestBody userId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(AuthManager.getUserId(this)));
            RequestBody fullName = RequestBody.create(MediaType.parse("text/plain"), AuthManager.getUserName(this));
            RequestBody email = RequestBody.create(MediaType.parse("text/plain"), AuthManager.getUserEmail(this));
            RequestBody password = RequestBody.create(MediaType.parse("text/plain"), "");

            ApiService api = ApiClient.getClient().create(ApiService.class);
            api.updateUserProfileWithImage(userId, fullName, email, password, part)
                    .enqueue(new retrofit2.Callback<GenericResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<GenericResponse> call, retrofit2.Response<GenericResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(profil.this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();

                                // reload foto baru
                                String imageUrl = "http://10.205.217.141/todo_api/uploads/user_"
                                        + AuthManager.getUserId(profil.this) + ".jpg";
                                Glide.with(profil.this)
                                        .load(imageUrl + "?t=" + System.currentTimeMillis())
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(imgProfile);
                            } else {
                                Toast.makeText(profil.this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<GenericResponse> call, Throwable t) {
                            Toast.makeText(profil.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Gagal memproses gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
