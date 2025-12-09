package com.shivprakash.to_dolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import java.util.Calendar;

import android.view.View;


public class riwayat extends AppCompatActivity {

    // 🔹 Tambahan variabel
    private RecyclerView recyclerView;
    private RiwayatAdapter adapter;
    private List<RiwayatModel> listRiwayat;
    private static final String URL_RIWAYAT = "http://10.205.217.141/todo_api/get_riwayat.php";
    private TextView tvDariTanggal, tvSampaiTanggal;
    private ImageButton btnDariTanggal, btnSampaiTanggal;
    private Button btnFilter;
    private TextView tvTotalSelesai, tvTotalTerlewat;

    private TextView tvKosong;


    private String dariTanggal = "", sampaiTanggal = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);
        recyclerView = findViewById(R.id.recyclerRiwayat);
        tvKosong = findViewById(R.id.tvKosong);

// Sembunyikan dulu semua tampilan riwayat
        recyclerView.setVisibility(View.GONE);
        tvKosong.setVisibility(View.GONE);



        // 🔹 Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerRiwayat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listRiwayat = new ArrayList<>();
        adapter = new RiwayatAdapter(listRiwayat);
        recyclerView.setAdapter(adapter);

        // Inisialisasi View untuk filter tanggal
        tvDariTanggal = findViewById(R.id.tvDariTanggal);
        tvSampaiTanggal = findViewById(R.id.tvSampaiTanggal);
        btnDariTanggal = findViewById(R.id.btnDariTanggal);
        btnSampaiTanggal = findViewById(R.id.btnSampaiTanggal);
        btnFilter = findViewById(R.id.btnFilter);
        tvTotalSelesai = findViewById(R.id.tvTotalSelesai);
        tvTotalTerlewat = findViewById(R.id.tvTotalTerlewat);


// Calendar untuk pilih tanggal
        Calendar calendar = Calendar.getInstance();

// Dari tanggal picker
        btnDariTanggal.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(riwayat.this, (view, y, m, d) -> {
                dariTanggal = String.format("%04d-%02d-%02d", y, m + 1, d);
                tvDariTanggal.setText(String.format("%02d-%02d-%d", d, m + 1, y));
            }, year, month, day);
            dp.show();
        });

// Sampai tanggal picker
        btnSampaiTanggal.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(riwayat.this, (view, y, m, d) -> {
                sampaiTanggal = String.format("%04d-%02d-%02d", y, m + 1, d);
                tvSampaiTanggal.setText(String.format("%02d-%02d-%d", d, m + 1, y));
            }, year, month, day);
            dp.show();
        });

// Tombol filter ditekan
        btnFilter.setOnClickListener(v -> {
            if (dariTanggal.isEmpty() || sampaiTanggal.isEmpty()) {
                Toast.makeText(this, "Pilih rentang tanggal dulu!", Toast.LENGTH_SHORT).show();
            } else {
                loadRiwayat(); // ✅ panggil di sini, biar muncul setelah pilih tanggal
            }
        });


        // 🔹 Panggil fungsi load data
        //loadRiwayat();

        // --- Bottom Navigation setup ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_history); // biar icon Riwayat nyala

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
                    // lagi di halaman ini, jadi gak ngapa-ngapain
                    return true;

                case R.id.nav_profile:
                    startActivity(new Intent(getApplicationContext(), profil.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    // 🔹 Tambahan fungsi untuk ambil data dari API
    private void loadRiwayat() {
        int userId = AuthManager.getUserId(this);

        StringRequest request = new StringRequest(Request.Method.POST, URL_RIWAYAT,
                response -> {
                    listRiwayat.clear();
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        // ✅ Pastikan field "status" ada dulu
                        if (jsonObject.has("status") && jsonObject.getString("status").equals("success")) {
                            JSONArray dataArray = jsonObject.optJSONArray("data"); // pakai opt biar gak crash

                            if (dataArray != null && dataArray.length() > 0) {
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    listRiwayat.add(new RiwayatModel(
                                            obj.getInt("id"),
                                            obj.getString("task"),
                                            obj.getString("category"),
                                            obj.getString("priority"),
                                            obj.getString("due_date"),
                                            obj.getString("due_time"),
                                            obj.getString("status")
                                    ));
                                }
                                recyclerView.setVisibility(View.VISIBLE);
                                findViewById(R.id.tvKosong).setVisibility(View.GONE);
                            } else {
                                // ✅ Kalau array kosong
                                recyclerView.setVisibility(View.GONE);
                                findViewById(R.id.tvKosong).setVisibility(View.VISIBLE);
                            }
                        } else {
                            // ✅ Kalau status bukan success
                            recyclerView.setVisibility(View.GONE);
                            findViewById(R.id.tvKosong).setVisibility(View.VISIBLE);
                        }

                        adapter.notifyDataSetChanged();

// 🔹 Update total tugas selesai dan terlewat di tampilan
                        if (jsonObject.has("total_selesai") && jsonObject.has("total_terlewat")) {
                            int totalSelesai = jsonObject.optInt("total_selesai", 0);
                            int totalTerlewat = jsonObject.optInt("total_terlewat", 0);

                            tvTotalSelesai.setText(String.valueOf(totalSelesai));
                            tvTotalTerlewat.setText(String.valueOf(totalTerlewat));
                        }


                    } catch (Exception e) {
                        // ✅ Amanin kalau parsing error
                        recyclerView.setVisibility(View.GONE);
                        findViewById(R.id.tvKosong).setVisibility(View.VISIBLE);
                        Toast.makeText(riwayat.this, "Tidak ada data untuk rentang tanggal ini", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(riwayat.this, "Gagal memuat data dari server!", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("dari_tanggal", dariTanggal);
                params.put("sampai_tanggal", sampaiTanggal);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
