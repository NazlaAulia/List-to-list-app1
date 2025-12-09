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

import java.util.Calendar;
import java.util.Locale;

// Retrofit / OkHttp
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;


public class editTask extends AppCompatActivity {

    private TextView selectedDateTextView;
    private TextView selectedTimeTextView;
    private Spinner categorySpinner;
    private Spinner prioritySpinner;
    private EditText notesEditText;
    private TextView text_view_task;

    private Calendar calendar;
    private String task;   // nama task (judul)
    private int mYear, mMonth, mDay, mHour, mMinute;
    private int taskId;    // ID task dari intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Intent intent = getIntent();
        taskId = intent.getIntExtra("id", -1);
        task   = intent.getStringExtra("task");

        text_view_task        = findViewById(R.id.text_view_task);
        selectedDateTextView  = findViewById(R.id.selected_date_text_view);
        selectedTimeTextView  = findViewById(R.id.selected_time_text_view);
        categorySpinner       = findViewById(R.id.category_spinner);
        prioritySpinner       = findViewById(R.id.priority_spinner);
        notesEditText         = findViewById(R.id.notes_edit_text);
        Button selectDateBtn  = findViewById(R.id.button_select_due_date);
        Button selectTimeBtn  = findViewById(R.id.button_select_due_time);
        Button saveBtn        = findViewById(R.id.button_add_task); // tombol “Save”

        text_view_task.setText(task != null ? task : "");

        calendar = Calendar.getInstance();
        mYear  = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay   = calendar.get(Calendar.DAY_OF_MONTH);
        mHour  = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute= calendar.get(Calendar.MINUTE);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories_array,
                R.layout.spinner_text // layout custom
        );
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.priorities_array,
                R.layout.spinner_text
        );
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        prioritySpinner.setAdapter(priorityAdapter);


        // === AMBIL DATA LAMA DARI INTENT ===
        String dateSql = intent.getStringExtra("date");      // "yyyy-MM-dd"
        String timeSql = intent.getStringExtra("time");      // "HH:mm:ss"
        String cat     = intent.getStringExtra("category");
        String prio    = intent.getStringExtra("priority");
        String notes   = intent.getStringExtra("notes");

// Isi Notes
        if (notes != null) notesEditText.setText(notes);

// Isi Tanggal & Jam (konversi dulu supaya enak dibaca di UI)
        if (dateSql != null && !dateSql.isEmpty()) {
            selectedDateTextView.setText(convertSqlDateToUi(dateSql));   // -> dd/MM/yyyy
        }
        if (timeSql != null && !timeSql.isEmpty()) {
            selectedTimeTextView.setText(timeSql.length() >= 5 ? timeSql.substring(0,5) : timeSql); // HH:mm
        }

// Pilih item Spinner sesuai value lama
        setSpinnerSelection(categorySpinner, cat);
        setSpinnerSelection(prioritySpinner, prio);

        // Hanya set default kalau data lama dari Intent KOSONG
        if ((dateSql == null || dateSql.isEmpty()) && (timeSql == null || timeSql.isEmpty())) {
            updateDateAndTimeTextViews();
        }


        selectDateBtn.setOnClickListener(v -> showDatePickerDialog());
        selectTimeBtn.setOnClickListener(v -> showTimePickerDialog());

        saveBtn.setOnClickListener(v -> {
            if (taskId <= 0) {
                Toast.makeText(this, "Task ID tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }
            editTask(task);
        });
    }

    private void updateDateAndTimeTextViews() {
        String dateString = String.format(Locale.getDefault(), "%02d/%02d/%d", mDay, mMonth + 1, mYear);
        selectedDateTextView.setText(dateString);

        String timeString = String.format(Locale.getDefault(), "%02d:%02d", mHour, mMinute);
        selectedTimeTextView.setText(timeString);
    }

    private void showDatePickerDialog() {
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    mYear = year;
                    mMonth = month;
                    mDay = dayOfMonth;
                    updateDateAndTimeTextViews();
                },
                mYear, mMonth, mDay
        );
        dlg.show();
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



    private void editTask(String taskTitle) {
        String category = categorySpinner.getSelectedItem().toString();
        String priority = prioritySpinner.getSelectedItem().toString();
        String notes    = notesEditText.getText().toString().trim();
        String dueDate  = selectedDateTextView.getText().toString().trim(); // dd/MM/yyyy
        String dueTime  = selectedTimeTextView.getText().toString().trim(); // HH:mm

        // ubah ke format SQL
        dueDate = convertUiDateToSql(dueDate); // yyyy-MM-dd
        dueTime = convertUiTimeToSql(dueTime); // HH:mm:ss

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = api.updateTask(
                taskId,
                taskTitle,
                category,
                priority,
                notes,
                dueDate,
                dueTime,
                0 // completed
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(editTask.this, "Task berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(editTask.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(editTask.this, "Gagal update task", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(editTask.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertUiDateToSql(String uiDate) {
        try {
            java.text.DateFormat in  = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US);
            java.text.DateFormat out = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return out.format(in.parse(uiDate));
        } catch (Exception e) {
            return uiDate; // fallback
        }
    }

    private String convertUiTimeToSql(String uiTime) {
        if (uiTime != null && uiTime.length() == 5) return uiTime + ":00";
        return uiTime;
    }


    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            String item = String.valueOf(spinner.getItemAtPosition(i));
            if (value.equalsIgnoreCase(item)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // "yyyy-MM-dd" -> "dd/MM/yyyy"
    private String convertSqlDateToUi(String sqlDate) {
        try {
            java.text.DateFormat in  = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.text.DateFormat out = new java.text.SimpleDateFormat("dd/MM/yyyy",  java.util.Locale.getDefault());
            return out.format(in.parse(sqlDate));
        } catch (Exception e) {
            return sqlDate;
        }
    }

}
