package com.shivprakash.to_dolist;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;





import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.ViewHolder> {

    private List<RiwayatModel> listRiwayat;

    public RiwayatAdapter(List<RiwayatModel> listRiwayat) {
        this.listRiwayat = listRiwayat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_riwayat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RiwayatModel data = listRiwayat.get(position);

        holder.tvTask.setText(data.getTask());
        holder.tvCategory.setText("Kategori: " + data.getCategory());
        holder.tvPriority.setText("Prioritas: " + data.getPriority());
        holder.tvDeadline.setText("Deadline: " + data.getDue_date() + " " + data.getDue_time());
        holder.tvStatus.setText("Status: " + data.getStatus());

        // 🔹 Warna status (hijau untuk selesai, merah untuk terlewat)
        if (data.getStatus().equalsIgnoreCase("selesai")) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if (data.getStatus().equalsIgnoreCase("terlewat")) {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#000000"));
        }


        // ✅ Tambahkan bagian INI di SINI (setelah pengaturan warna)
        holder.btnBatalkan.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            RiwayatModel task = listRiwayat.get(pos);
            int taskId = task.getId(); // pastikan kamu sudah tambahkan getId() di model
            android.util.Log.d("BATALKAN_TASK", "Mengirim id: " + taskId);
            ApiService api = ApiClient.getClient().create(ApiService.class);
            Call<ResponseBody> call = api.batalkanTask(task.getId());

            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String resp = response.body().string();
                            org.json.JSONObject json = new org.json.JSONObject(resp);

                            boolean success = json.optBoolean("success", false);
                            String message = json.optString("message", "Gagal memproses permintaan");

                            android.widget.Toast.makeText(v.getContext(), message, android.widget.Toast.LENGTH_SHORT).show();

                            if (success) {
                                // kalau benar-benar berhasil → hapus dari tampilan & refresh home
                                listRiwayat.remove(pos);
                                notifyItemRemoved(pos);

                                android.content.Intent intent = new android.content.Intent(v.getContext(), MainActivity.class);
                                intent.putExtra("REFRESH_FROM_RIWAYAT", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                v.getContext().startActivity(intent);

                                if (v.getContext() instanceof android.app.Activity) {
                                    ((android.app.Activity) v.getContext()).finish();
                                }
                            }

                        } catch (Exception e) {
                            android.widget.Toast.makeText(v.getContext(), "Gagal membaca respons", android.widget.Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        android.widget.Toast.makeText(v.getContext(), "Gagal membatalkan tugas (server error)", android.widget.Toast.LENGTH_SHORT).show();
                    }

                }


                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    android.widget.Toast.makeText(v.getContext(), "Error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });


    }

    @Override
    public int getItemCount() {
        return listRiwayat.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTask, tvCategory, tvPriority, tvDeadline, tvStatus;
        Button btnBatalkan; // ✅ tambahkan ini di dalam ViewHolder


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTask = itemView.findViewById(R.id.tvTask);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnBatalkan = itemView.findViewById(R.id.btnBatalkan); // ✅ hubungkan ke XML

        }
    }
}
