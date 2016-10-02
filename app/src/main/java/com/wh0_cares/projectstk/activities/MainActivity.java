package com.wh0_cares.projectstk.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.quinny898.library.persistentsearch.SearchResult;
import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.fragments.DetailFragment;
import com.wh0_cares.projectstk.fragments.PortfolioFragment;
import com.wh0_cares.projectstk.utils.SearchBox;

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

public class MainActivity extends AppCompatActivity implements SearchBox.SearchListener {

    public static NavigationView navigationView;
    private final OkHttpClient client = new OkHttpClient();
    public static ActionBarDrawerToggle actionBarDrawerToggle;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    static DrawerLayout drawerLayout;
    TextView title, subtitle;
    SearchBox search;
    boolean searchopened = false;
    static ImageView imageView;
    static CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, new PortfolioFragment(), getString(R.string.Portfolio)).addToBackStack(getString(R.string.Portfolio));
        ft.commit();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        initNavigationDrawer();
        search = (SearchBox) findViewById(R.id.searchbox);
        setUpSearch();
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        imageView = (ImageView) findViewById(R.id.backdrop);
        disableCollapse();
    }

    private void setUpSearch() {
        search.setLogoText("");
        search.setSearchString("");
        search.clearFocus();
        search.setSearchListener(this);
    }

    private void openSearch() {
        search.revealFromMenuItem(R.id.search, this);
        toolbar.setVisibility(View.GONE);
        setDrawerEnabled(false);
    }

    private void closeSearch() {
        toolbar.setVisibility(View.VISIBLE);
        setDrawerEnabled(true);
        search.hideCircularly(MainActivity.this);
        search.setSearchString("");
        search.clearSearchable();
        search.clearResults();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.search: {
                openSearch();
                return false;
            }
            case R.id.signout: {
                return false;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void initNavigationDrawer() {

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                final int id = menuItem.getItemId();
                drawerLayout.closeDrawers();
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fragment1, R.anim.fragment2);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switch (id) {
                            case R.id.portfolio:
                                ft.replace(R.id.container, new PortfolioFragment(), getString(R.string.Portfolio)).addToBackStack(getString(R.string.Portfolio));
                                break;
                        }
                        ft.commit();
                    }
                }, 250);
                return true;
            }
        });
        View header = navigationView.getHeaderView(0);
        title = (TextView) header.findViewById(R.id.title);
        title.setText(R.string.Title);
        subtitle = (TextView) header.findViewById(R.id.subtitle);
        subtitle.setText(R.string.Subtitle);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            if (searchopened) {
                search.toggleSearch();
                searchopened = false;
            } else {
                PortfolioFragment pf = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.Portfolio));
                if (pf != null && pf.isVisible()) {
                    finish();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                getSupportFragmentManager().popBackStack();
                ft.setCustomAnimations(R.anim.back1, R.anim.back2);
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

    public static void disableCollapse() {
        imageView.setVisibility(View.GONE);
        collapsingToolbar.setTitleEnabled(false);
    }

    public static void enableCollapse() {
        imageView.setVisibility(View.VISIBLE);
        collapsingToolbar.setTitleEnabled(true);
    }

    @Override
    public void onSearch(String s) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fr = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("symbol", s);
        fr.setArguments(args);
        ft.setCustomAnimations(R.anim.fragment1, R.anim.fragment2);
        ft.replace(R.id.container, fr).addToBackStack(getString(R.string.Details));
        ft.commit();
        search.setSearchString("");
        search.clearSearchable();
        search.clearResults();
    }

    @Override
    public void onResultClick(SearchResult s) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fr = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("symbol", String.valueOf(s).substring(0, String.valueOf(s).indexOf(" - ")));
        fr.setArguments(args);
        ft.setCustomAnimations(R.anim.fragment1, R.anim.fragment2);
        ft.replace(R.id.container, fr).addToBackStack(getString(R.string.Details));
        ft.commit();
        search.setSearchString("");
        search.clearSearchable();
        search.clearResults();
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
                                SearchResult option = new SearchResult(stockSymbol + " - " + stockName, getResources().getDrawable(R.drawable.ic_signout));
                                search.addSearchable(option);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void setDrawerEnabled(boolean enabled) {
        if (enabled) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
            actionBarDrawerToggle.syncState();
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
            actionBarDrawerToggle.syncState();
        }
    }
}