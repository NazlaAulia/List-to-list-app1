package com.shivprakash.to_dolist;

import java.util.Collections;
import java.util.Comparator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Toast;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;



public class MainActivity extends AppCompatActivity {
    private ScrollView taskScrollView;
    private ArrayAdapter<String> taskAdapter;
    private List<Data> taskData;

    RecyclerView recyclerView;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        boolean fromRiwayat = getIntent().getBooleanExtra("REFRESH_FROM_RIWAYAT", false);
        if (fromRiwayat) {
            loadTasksFromAPI(); // langsung muat ulang data begitu balik dari Riwayat
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    return true;
                case R.id.nav_calendar:
                    startActivity(new Intent(getApplicationContext(), kalender.class));
                    overridePendingTransition(0, 0);
                    return true;

                case R.id.nav_add:
                    startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
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

        taskData = new ArrayList<>();
        taskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);


        // ✅ INISIALISASI RECYCLER & ADAPTER DULU (penting)
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, taskData);
        recyclerView.setAdapter(adapter);

        // 🔥 ambil data dari API, bukan SQLite
        loadTasksFromAPI(); // ✅ PANGGIL SETELAH adapter siap

        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                MainActivity.Data d = taskData.get(position);
                Intent intent = new Intent(MainActivity.this, editTask.class);
                intent.putExtra("id", d.getId());           // ⬅️ penting
                intent.putExtra("task", d.getName());
                intent.putExtra("date", d.getDate());
                intent.putExtra("time", d.getTime());
                intent.putExtra("category", d.getCategory());
                intent.putExtra("priority", d.getPriority());
                intent.putExtra("notes", d.getNotes());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                int taskId = taskData.get(position).getId();

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Konfirmasi Hapus")
                        .setMessage("Apakah kamu yakin ingin menghapus task ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            // baru hapus kalau user klik YA
                            ApiService api = ApiClient.getClient().create(ApiService.class);
                            api.deleteTask(taskId).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                                    if (res.isSuccessful()) {
                                        taskData.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        Toast.makeText(MainActivity.this, "Task berhasil dihapus", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Delete gagal (" + res.code() + ")", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Batal", (dialog, which) -> {
                            dialog.dismiss(); // kalau batal, tutup dialog
                        })
                        .show();
            }




            @Override
            public void onCheckboxClick(int position) {
                MainActivity.Data d = taskData.get(position);
                ApiService api = ApiClient.getClient().create(ApiService.class);

                api.updateTask(
                        d.getId(),
                        d.getName(),
                        d.getCategory(),
                        d.getPriority(),
                        d.getNotes(),
                        d.getDate(),
                        d.getTime(),
                        1 // completed = 1
                ).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                        if (res.isSuccessful()) {
                            taskData.remove(position);
                            loadTasksFromAPI();
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(MainActivity.this, "Task completed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Update gagal ("+res.code()+")", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });


    }

    // (Opsional) ✅ refresh otomatis saat kembali ke halaman ini
    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromAPI();
        System.out.println("🔄 Home direfresh setelah kembali dari Riwayat");
    }


    // 🔥 ambil data dari API
    private void loadTasksFromAPI() {
        int uid = AuthManager.getUserId(this);   // ⬅️ ambil user yg lagi login
        if (uid <= 0) {
            // kalau belum login, arahkan balik (opsional)
            // startActivity(new Intent(this, login.class));
            // finish();
            // return;
            uid = 1; // fallback sementara kalau kamu belum pakai login
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Task>> call = apiService.getTasks(uid);

        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskData.clear();

                    for (Task task : response.body()) {
                        if (task.isCompleted()) continue; // Skip task yang sudah selesai

                        // 🔹 Cek apakah tugas sudah lewat harinya
                        String dueDate = task.getDueDate(); // format: yyyy-MM-dd
                        if (dueDate != null && !dueDate.isEmpty()) {
                            try {
                                try {
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                                    java.util.Date today = sdf.parse(sdf.format(new java.util.Date())); // tanggal hari ini
                                    java.util.Date taskDate = sdf.parse(dueDate);

                                    // Kalau tugas tanggalnya lebih kecil dari hari ini → skip
                                    // Kalau sudah lewat hari tapi statusnya belum selesai (dibatalkan), tetap tampilkan
                                    if (taskDate.before(today) && task.isCompleted()) {
                                        continue;
                                    }


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d("TASK_DEBUG", task.getTaskName() + " -> completed: " + task.getCompletedInt());

                        // ✅ Kalau belum terlewat, tambahkan ke tampilan
                        taskData.add(new Data(
                                task.getId(),
                                task.getTaskName(),
                                task.getDueDate(),
                                task.getDueTime(),
                                task.getCategory(),
                                task.getPriority(),
                                task.getNotes() == null ? "" : task.getNotes()
                        ));
                    }


                    // === URUTKAN: High dulu, lalu Medium, lalu Low ===
                    // === URUTKAN: Tanggal → Kategori → Prioritas → Waktu ===
                    Collections.sort(taskData, new Comparator<Data>() {
                        @Override
                        public int compare(Data a, Data b) {
                            // 1️⃣ Urut berdasarkan tanggal (naik)
                            int dateCompare = safe(a.getDate()).compareTo(safe(b.getDate()));
                            if (dateCompare != 0) return dateCompare;

                            // 2️⃣ Kalau tanggal sama → urut berdasarkan PRIORITY (High > Medium > Low)
                            int priorityCompare = Integer.compare(priorityRank(b.getPriority()), priorityRank(a.getPriority()));
                            if (priorityCompare != 0) return priorityCompare;

                            // 3️⃣ Kalau priority sama → urut berdasarkan TIME (naik)
                            return normTime(a.getTime()).compareTo(normTime(b.getTime()));
                        }
                    });

                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Gagal ambil data dari API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Map prioritas ke angka (semakin besar = semakin penting)
    private int priorityRank(String p) {
        if (p == null) return 0;
        switch (p.toLowerCase()) {
            case "high":   return 3;
            case "medium": return 2;
            case "low":    return 1;
            default:       return 0;
        }
    }

    // Guard untuk null tanggal (pakai nilai besar supaya jatuh ke bawah)
    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "9999-12-31" : s;
    }

    // Normalisasi jam "HH:mm" -> "HH:mm:ss" agar bisa dibandingkan
    private String normTime(String t) {
        if (t == null || t.isEmpty()) return "23:59:59";
        return (t.length() == 5) ? (t + ":00") : t;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "todo_channel";
            String description = "Channel untuk notifikasi To Do List";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("todo_channel_id", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "todo_channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Halo Zla 👋")
                .setContentText("Ini notifikasi pertamamu berhasil muncul!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }

    public class Data {
        int id;
        String name;
        String date;
        String time;
        String category;
        String priority;
        String notes;

        Data(int id,String name, String date, String time, String category, String priority, String notes) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.time = time;
            this.category = category;
            this.priority = priority;
            this.notes = notes;
        }



        public int getId() { return id; }
        public String getName() { return name; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getCategory() { return category; }
        public String getPriority() { return priority; }
        public String getNotes() { return notes; }
    }
}
