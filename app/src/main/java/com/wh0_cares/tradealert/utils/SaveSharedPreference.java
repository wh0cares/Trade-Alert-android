package com.wh0_cares.tradealert.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.Editor;

public class SaveSharedPreference {
    /* SETUP
    * 0 = Not signed in
    * 1 = Signed in
    */
    static final String prefAccessToken = "access-token";
    static final String prefSetup = "setup";
    static final String prefTempStocks = "temp-stocks";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setToken(Context ctx, String token) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(prefAccessToken, token);
        editor.commit();
    }

    public static String getToken(Context ctx) {
        final String token = getSharedPreferences(ctx).getString(prefAccessToken, "");
        return token;
    }

    public static void setSetup(Context ctx, int setup) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(prefSetup, setup);
        editor.commit();
    }

    public static int getSetup(Context ctx) {
        final int setup = getSharedPreferences(ctx).getInt(prefSetup, 0);
        return setup;
    }

    public static void clearData(Context ctx) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.clear();
        editor.commit();
    }

    public static void setTempStocks(Context ctx, String[] array) {
        Editor editor = getSharedPreferences(ctx).edit();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]).append(",");
        }
        editor.putString(prefTempStocks, sb.toString());
        editor.commit();
    }

    public static String[] getTempStocks(Context ctx){
        final String tempStocks = getSharedPreferences(ctx).getString(prefTempStocks, "");
        String[] tempStocksArray = tempStocks.split(",");
        return tempStocksArray;
    }
}
