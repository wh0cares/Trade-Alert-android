package com.wh0_cares.tradealert.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.wh0_cares.tradealert.BuildConfig;
import com.wh0_cares.tradealert.R;
import com.wh0_cares.tradealert.database.DatabaseHandler;
import com.wh0_cares.tradealert.utils.SaveSharedPreference;

public class SettingsActivity extends PreferenceActivity implements BillingProcessor.IBillingHandler {

    BillingProcessor bp;
    CheckBoxPreference remove_ads;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_settings);
        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhE2uwwfjeiB+eyvj31NglbNuoN5XYNs+5jcWIMrRdx0F3W4w7Dd3rXZ9p6S/mpUl+X479wwwWUL98u64VKIwC4srqZqRBWtjJ2IAwlol87sNuiXdb/U0/TGU3l8A+39CDb78eg+wCVXzoFm2WlN4CwKOSgW3vI1cZu0txZnwnHprlqUH07JqTtVtt3LpOT2psjYA6438YC0x3eEcMRTjAh4VXu6CKFackvc5RFJrJgFLRkOiYH9Mb4j9yRTSuKFzESYGDhTOaQDIDGTnuZeh9ZunHhtW+teuKTxRYTKkBaV0pLxVwdDdFf35olQJs4m4IZWhmQf22uon/x3x1OceIQIDAQAB", this);
        setupPreferences();
    }

    @SuppressLint("NewApi")
    private void setupPreferences() {
        //Version
        Preference aboutTradeAlert = getPreferenceManager().findPreference("about_trade_alert");
        aboutTradeAlert.setSummary(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        aboutTradeAlert.setIcon(getDrawable(R.mipmap.ic_launcher));

        //Remove ads
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        remove_ads = (CheckBoxPreference) getPreferenceManager().findPreference("remove_ads");
        if (prefs.getBoolean("remove_ads", false)) {
            remove_ads.setChecked(true);
        } else {
            remove_ads.setChecked(false);
        }
        remove_ads.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isChecked = (Boolean) newValue;
                if(isChecked){
                    bp.purchase(SettingsActivity.this, "remove_ads");
                }
                return true;
            }
        });

        //Sign out
        final Preference purgeCache = getPreferenceManager().findPreference("sign_out");
        purgeCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DatabaseHandler db = new DatabaseHandler(SettingsActivity.this);
                SaveSharedPreference.clearData(SettingsActivity.this);
                db.deleteDatabase(SettingsActivity.this);
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    public void onBillingInitialized() {
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        remove_ads.setChecked(true);
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if(errorCode == 7){
            remove_ads.setChecked(true);
        }else{
            remove_ads.setChecked(false);
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}