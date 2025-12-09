package com.shivprakash.to_dolist;

import java.util.List;

public class RiwayatResponse {
    private String status;
    private String message;
    private List<RiwayatData> data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<RiwayatData> getData() {
        return data;
    }

    // Inner class biar bisa parsing tiap item riwayat
    public static class RiwayatData {
        private String task;
        private String category;
        private String priority;
        private String due_date;
        private String due_time;
        private String status;

        public String getTask() {
            return task;
        }

        public String getCategory() {
            return category;
        }

        public String getPriority() {
            return priority;
        }

        public String getDue_date() {
            return due_date;
        }

        public String getDue_time() {
            return due_time;
        }

        public String getStatus() {
            return status;
        }
    }
}
