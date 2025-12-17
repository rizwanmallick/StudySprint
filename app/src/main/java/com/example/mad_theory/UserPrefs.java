package com.example.mad_theory;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class UserPrefs {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id"; // Firebase UID stored as string

    private final SharedPreferences prefs;

    public UserPrefs(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(String userUid, String name, String email) {
        prefs.edit()
                .putString(KEY_USER_ID, userUid)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    // Backwards-compatible helper (legacy callers)
    public void saveUser(String name, String email) {
        saveUser("", name, email);
    }

    // Overload for previous code that used numeric ids
    public void saveUser(long userId, String name, String email) {
        saveUser(String.valueOf(userId), name, email);
    }

    public String getUserUid() {
        String uid = prefs.getString(KEY_USER_ID, "");
        // Migrate legacy long-based storage if present
        if (TextUtils.isEmpty(uid)) {
            long legacy = prefs.getLong(KEY_USER_ID, -1);
            if (legacy > 0) {
                uid = String.valueOf(legacy);
                prefs.edit().putString(KEY_USER_ID, uid).apply();
            }
        }
        return uid;
    }

    public String getName() {
        return prefs.getString(KEY_NAME, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(getUserUid()) && !TextUtils.isEmpty(getEmail());
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}


