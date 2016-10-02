package com.wh0_cares.projectstk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {
    static final String prefAccessToken = "access-token";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setToken(Context ctx, String token) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(prefAccessToken, token);
        editor.commit();
    }

    public static String getToken(Context ctx) {
        final String token = getSharedPreferences(ctx).getString(prefAccessToken, "");
        return token;
    }
}
