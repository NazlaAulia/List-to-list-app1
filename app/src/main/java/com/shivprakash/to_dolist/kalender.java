package com.shivprakash.to_dolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class kalender extends AppCompatActivity {

    // 🔹 Variabel yang dipakai
    private CalendarView calendarView;
    private LinearLayout containerTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalender);

        // 🔹 Inisialisasi view kalender dan container tugas
        calendarView = findViewById(R.id.calendarView);
        containerTasks = findViewById(R.id.containerTasks);

        // ✅ Tambahkan kode warna fokus (letakkan di sini)
        calendarView.setFocusedMonthDateColor(getResources().getColor(android.R.color.holo_blue_light));
        calendarView.setWeekSeparatorLineColor(getResources().getColor(android.R.color.transparent));

        // 🔹 Saat tanggal diklik oleh user
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadTasksByDate(selectedDate);
        });

        // 🔹 Load otomatis tugas hari ini saat halaman dibuka
        long currentDateMillis = calendarView.getDate();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new java.util.Date(currentDateMillis));
        loadTasksByDate(today);

        // 🔹 Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_calendar:
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
                    startActivity(new Intent(getApplicationContext(), profil.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    // 🔹 Ambil data tugas dari server berdasarkan tanggal
    private void loadTasksByDate(String date) {
        // 🔹 Ambil user ID yang sedang login
        int uid = AuthManager.getUserId(this);

        // 🔹 Tambahkan parameter uid di URL API
        String url = "http://10.205.217.141/todo_api/get_tugas_by_tanggal.php?tanggal=" + date + "&uid=" + uid;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    containerTasks.removeAllViews(); // hapus data lama

                    try {
                        if (response.length() == 0) {
                            TextView tvKosong = new TextView(this);
                            tvKosong.setText("Tidak ada tugas di tanggal ini");
                            tvKosong.setTextSize(14);
                            tvKosong.setPadding(8, 8, 8, 8);
                            containerTasks.addView(tvKosong);
                            return;
                        }

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String task = obj.getString("task");
                            String category = obj.getString("category");
                            String time = obj.getString("due_time");

                            TextView tv = new TextView(this);
                            tv.setText("- " + task + " (" + category + ", " + time + ")");
                            tv.setTextSize(14);
                            tv.setTextColor(android.graphics.Color.WHITE);
                            tv.setPadding(8, 8, 8, 8);
                            containerTasks.addView(tv);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Gagal memuat data dari server", Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
