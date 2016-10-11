package com.wh0_cares.projectstk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {
    /* SETUP
    * 0 = Not signed in
    * 1 = Signed in
    */
    static final String prefAccessToken = "access-token";
    static final String prefSetup = "setup";

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

    public static void setSetup(Context ctx, int setup) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(prefSetup, setup);
        editor.commit();
    }

    public static int getSetup(Context ctx) {
        final int setup = getSharedPreferences(ctx).getInt(prefSetup, 0);
        return setup;
    }

    public static void clearData(Context ctx) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear();
        editor.commit();
    }
}
