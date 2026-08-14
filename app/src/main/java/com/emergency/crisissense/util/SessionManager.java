package com.emergency.crisissense.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.emergency.crisissense.model.User;

public class SessionManager {
    private static final String PREF_NAME = "CrisisSenseSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IMAGE = "profileImage";
    private static final String KEY_ADDRESS = "address";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ROLE, user.getRole());
        editor.putString(KEY_IMAGE, user.getProfileImage());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }
        User user = new User(
            pref.getString(KEY_USER_ID, null),
            pref.getString(KEY_NAME, null),
            pref.getString(KEY_EMAIL, null),
            pref.getString(KEY_PHONE, null),
            pref.getString(KEY_ROLE, null),
            pref.getString(KEY_IMAGE, null),
            pref.getString(KEY_ADDRESS, null)
        );
        return user;
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, "citizen");
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }
}
