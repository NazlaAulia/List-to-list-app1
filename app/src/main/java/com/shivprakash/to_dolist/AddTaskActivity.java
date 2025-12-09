package com.shivprakash.to_dolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTaskActivity extends AppCompatActivity {

    private TextView selectedDateTextView;
    private TextView selectedTimeTextView;
    private EditText taskEditText;
    private Spinner categorySpinner;
    private Spinner prioritySpinner;
    private EditText notesEditText;

    private Calendar calendar;
    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_add); // buat highlight menu tambah

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
                    return true; // halaman ini sendiri
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


        selectedDateTextView = findViewById(R.id.selected_date_text_view);
        selectedTimeTextView = findViewById(R.id.selected_time_text_view);
        taskEditText = findViewById(R.id.task_edit_text);
        categorySpinner = findViewById(R.id.category_spinner);
        prioritySpinner = findViewById(R.id.priority_spinner);
        notesEditText = findViewById(R.id.notes_edit_text);
        Button selectDateButton = findViewById(R.id.button_select_due_date);
        Button selectTimeButton = findViewById(R.id.button_select_due_time);
        Button addTaskButton = findViewById(R.id.button_add_task);

        calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories_array,
                R.layout.spinner_text
        );
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        categorySpinner.setAdapter(categoryAdapter);


        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.priorities_array,
                R.layout.spinner_text // GUNAKAN layout custom kamu
        );
        priorityAdapter.setDropDownViewResource(R.layout.spinner_text );
        prioritySpinner.setAdapter(priorityAdapter);

        updateDateAndTimeTextViews();

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask(); // tetap sama, cuma isinya sudah ke MySQL
            }
        });
    }

    // untuk tampil di TextView (UI)
    private void updateDateAndTimeTextViews() {
        String dateStringUI = String.format(Locale.getDefault(), "%02d/%02d/%d", mDay, mMonth + 1, mYear);
        selectedDateTextView.setText(dateStringUI);

        String timeStringUI = String.format(Locale.getDefault(), "%02d:%02d", mHour, mMinute);
        selectedTimeTextView.setText(timeStringUI);
    }

    // untuk dikirim ke API (format SQL)
    private String getDueDateForApi() {
        return String.format(Locale.US, "%04d-%02d-%02d", mYear, mMonth + 1, mDay); // yyyy-MM-dd
    }

    private String getDueTimeForApi() {
        return String.format(Locale.US, "%02d:%02d:00", mHour, mMinute); // HH:mm:ss
    }


    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mYear = year;
                        mMonth = month;
                        mDay = dayOfMonth;
                        updateDateAndTimeTextViews();
                    }
                },
                mYear, mMonth, mDay);
        datePickerDialog.show();
    }
    private void showTimePickerDialog() {
        // 🔹 MaterialTimePicker bisa mulai dari mode digital, tapi user bisa switch ke analog
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(mHour)
                .setMinute(mMinute)
                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD) // ⬅️ mulai dari digital
                .setTitleText("Pilih Waktu")
                .build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            mHour = picker.getHour();
            mMinute = picker.getMinute();
            updateDateAndTimeTextViews();
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }



    // ================== SUDAH KE MYSQL API ====================
    // ================== SUDAH KE MYSQL API ====================
    private void addTask() {
        String task = taskEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String priority = prioritySpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString().trim();
        String dueDate = getDueDateForApi();   // yyyy-MM-dd
        String dueTime = getDueTimeForApi();   // HH:mm:ss

        int uid = AuthManager.getUserId(this);
        if (uid <= 0) uid = 1; // fallback sementara kalau belum login

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.addTask(task, category, priority, notes, dueDate, dueTime, uid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddTaskActivity.this, "Task berhasil ditambahkan ke MySQL", Toast.LENGTH_SHORT).show();
                    scheduleNotification(task, dueDate);
                    startActivity(new Intent(AddTaskActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(AddTaskActivity.this, "Gagal menambahkan task", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AddTaskActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void scheduleNotification(String taskName, String dueDate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date date = sdf.parse(dueDate);

            // Kurangi 1 hari (H-1)
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1);

            // Waktu notifikasi: jam 08:00 pagi H-1
            cal.set(java.util.Calendar.HOUR_OF_DAY, 8);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);

            long triggerTime = cal.getTimeInMillis();

            android.content.Intent intent = new android.content.Intent(this, NotificationReceiver.class);
            intent.putExtra("task_name", taskName);
            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                    this,
                    (int) System.currentTimeMillis(),
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );

            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
