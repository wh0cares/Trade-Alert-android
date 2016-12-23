package com.wh0_cares.tradealert.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.quinny898.library.persistentsearch.SearchResult;
import com.wh0_cares.tradealert.R;
import com.wh0_cares.tradealert.adapters.ViewPagerAdapter;
import com.wh0_cares.tradealert.alarm.AlarmReceiver;
import com.wh0_cares.tradealert.utils.SaveSharedPreference;
import com.wh0_cares.tradealert.utils.SearchBox;
import com.wh0_cares.tradealert.utils.SlidingTabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SearchBox.SearchListener{

    private final OkHttpClient client = new OkHttpClient();
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    SearchBox search;
    boolean searchopened = false;
    AlarmReceiver alarm = new AlarmReceiver();

    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[];
    int Numboftabs = 2;
    MenuItem[] items;
    public static FirebaseAnalytics fa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int setup = SaveSharedPreference.getSetup(MainActivity.this);
        if (setup == 0) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        } else {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            setSupportActionBar(toolbar);
            search = (SearchBox) findViewById(R.id.searchbox);
            setUpSearch();
            Titles = new CharSequence[]{getString(R.string.Portfolio), getString(R.string.Todays_alert)};
            adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs);
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(adapter);
            tabs = (SlidingTabLayout) findViewById(R.id.tabs);
            tabs.setDistributeEvenly(true);
            tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.colorAccent);
                }
            });
            tabs.setViewPager(pager);
            fa = FirebaseAnalytics.getInstance(this);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (!prefs.getBoolean("remove_ads", false)) {
                MobileAds.initialize(this, "ca-app-pub-7704895981554483~9203484256");
                AdView adView = (AdView) findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
            }

            if(!prefs.getBoolean("alarm_set", false)){
                alarm.setAlarm(this);
                prefs.edit().putBoolean("alarm_set", true).apply();
            }
        }
    }

    private void setUpSearch() {
        search.setLogoText("");
        search.setSearchString("");
        search.clearFocus();
        search.setSearchListener(this);
    }

    private void openSearch() {
        search.revealFromMenuItem(R.id.search, this);
        for (MenuItem item : items){
            item.setVisible(false);
        }
    }

    private void closeSearch() {
        for (MenuItem item : items){
            item.setVisible(true);
        }
        search.hideCircularly(MainActivity.this);
        search.setSearchString("");
        search.clearSearchable();
        search.clearResults();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        items = new MenuItem[]{menu.getItem(0), menu.getItem(1)};
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search: {
                openSearch();
                return false;
            }
            case R.id.settnigs: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return false;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (searchopened) {
            search.toggleSearch();
            searchopened = false;
        } else {
            if(pager.getCurrentItem() == 0) {
                finish();
            }else if (pager.getCurrentItem() == 1){
                pager.setCurrentItem(0);
            }
        }
    }

    @Override
    public void onSearchOpened() {
        searchopened = true;
    }

    @Override
    public void onSearchCleared() {
    }

    @Override
    public void onSearchClosed() {
        closeSearch();
        searchopened = false;
    }

    @Override
    public void onSearchTermChanged(String s) {
        search.clearSearchable();
        if (!s.equals("")) {
            try {
                searchSymbol(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            search.clearResults();
        }
    }

    @Override
    public void onSearch(String s) {
//        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        Fragment fr = new DetailFragment();
//        Bundle args = new Bundle();
//        args.putString("symbol", s);
//        fr.setArguments(args);
//        ft.setCustomAnimations(R.anim.fragment1, R.anim.fragment2);
//        ft.replace(R.id.container, fr).addToBackStack(getString(R.string.Details));
//        ft.commit();
//        search.setSearchString("");
//        search.clearSearchable();
//        search.clearResults();
        Toast.makeText(this, R.string.Select_a_stock_from_the_suggestions, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResultClick(SearchResult s) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("symbol", String.valueOf(s).substring(0, String.valueOf(s).indexOf(" - ")));
        intent.putExtra("title", String.valueOf(s));
        search.setSearchString("");
        search.clearSearchable();
        search.clearResults();
        startActivity(intent);
    }

    public void searchSymbol(String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.search_url) + symbol)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response.body());
                }
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONArray matchesArray = obj.getJSONArray("matches");
                    if (matchesArray.length() >= 1) {
                        for (int i = 0; i < matchesArray.length(); i++) {
                            JSONObject stockObject = matchesArray.getJSONObject(i);
                            String stockIndex = stockObject.getString("e");
                            if (stockIndex.equalsIgnoreCase("NASDAQ")) {
                                String stockSymbol = stockObject.getString("t");
                                String stockName = stockObject.getString("n");
                                SearchResult option = new SearchResult(stockSymbol + " - " + stockName);
                                search.addSearchable(option);
                            }
                        }
                    }
                    response.body().close();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}