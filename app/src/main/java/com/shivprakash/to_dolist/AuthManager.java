package com.shivprakash.to_dolist;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREF = "auth_pref";
    private static final String KEY_UID = "user_id";
    private static final String KEY_NAME = "full_name";
    private static final String KEY_EMAIL = "email";

    public static void saveSession(Context c, int uid, String name, String email){
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putInt(KEY_UID, uid)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public static int getUserId(Context c){
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY_UID, 0);
    }

    public static boolean isLoggedIn(Context c){
        return getUserId(c) > 0;
    }

    public static void logout(Context c){
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static String getUserName(Context context) {
        return getPrefs(context).getString(KEY_NAME, "");
    }

    public static String getUserEmail(Context context) {
        return getPrefs(context).getString(KEY_EMAIL, "");
    }

    // 🔹 Fungsi tambahan supaya getPrefs() dikenali
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}
