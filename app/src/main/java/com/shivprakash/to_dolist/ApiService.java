package com.shivprakash.to_dolist;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import com.shivprakash.to_dolist.models.UserProfileResponse;
import com.shivprakash.to_dolist.models.GenericResponse;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ApiService {

    // --- TASKS ---

    // Ambil task milik user tertentu
    @GET("get_tasks.php")
    Call<List<Task>> getTasks(@Query("user_id") int userId);

    // Ambil tugas berdasarkan tanggal + user login (untuk fitur kalender)
    @GET("get_tugas_by_tanggal.php")
    Call<List<Task>> getTugasByTanggal(
            @Query("tanggal") String tanggal,
            @Query("uid") int uid
    );


    // Tambah task (WAJIB kirim user_id)
    @FormUrlEncoded
    @POST("add_task.php")
    Call<ResponseBody> addTask(
            @Field("task") String task,
            @Field("category") String category,
            @Field("priority") String priority,
            @Field("notes") String notes,
            @Field("due_date") String dueDate,   // YYYY-MM-DD
            @Field("due_time") String dueTime,   // HH:mm:ss
            @Field("user_id") int userId         // ⬅️ tambahan penting

    );

    // (Kalau kamu pakai) Update / Delete biarkan seperti punyamu
    @FormUrlEncoded
    @POST("update_task.php")
    Call<ResponseBody> updateTask(
            @Field("id") int id,
            @Field("task") String task,
            @Field("category") String category,
            @Field("priority") String priority,
            @Field("notes") String notes,
            @Field("due_date") String dueDate,
            @Field("due_time") String dueTime,
            @Field("completed") int completed
    );

    @FormUrlEncoded
    @POST("delete_task.php")
    Call<ResponseBody> deleteTask(@Field("id") int id);

    @FormUrlEncoded
    @POST("batalkan_tugas.php")
    Call<ResponseBody> batalkanTask(
            @Field("id") int id
    );





    // --- AUTH (kalau sudah kamu buat) ---
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("register.php")
    Call<ResponseBody> signup(
            @Field("full_name") String fullName,
            @Field("email") String email,
            @Field("password") String password
    );

    // --- FORGOT PASSWORD ---
    @FormUrlEncoded
    @POST("forgot_password.php")
    Call<ResponseBody> forgotPassword(
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("get_user_profile.php")
    Call<UserProfileResponse> getUserProfile(
            @Field("user_id") int userId
    );

    // 🔹 Tambahan untuk ambil foto profil (kolom 'foto' / BLOB)
    @FormUrlEncoded
    @POST("get_user_profile.php")
    Call<UserProfileResponse> getUserProfileWithFoto(
            @Field("user_id") int userId
    );




    @FormUrlEncoded
    @POST("get_riwayat.php")
    Call<RiwayatResponse> getRiwayat(
            @Field("user_id") int userId
    );


    @FormUrlEncoded
    @POST("update_user_profile.php")
    Call<GenericResponse> updateUserProfile(
            @Field("user_id") int userId,
            @Field("full_name") String fullName,
            @Field("email") String email,
            @Field("password") String password

    );

    // --- Upload Profil dengan Gambar ---
    @Multipart
    @POST("update_user_profile.php")
    Call<GenericResponse> updateUserProfileWithImage(
            @Part("user_id") RequestBody userId,
            @Part("full_name") RequestBody fullName,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part MultipartBody.Part profile_image
    );

}



