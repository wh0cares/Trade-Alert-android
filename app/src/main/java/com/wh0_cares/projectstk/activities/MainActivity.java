package com.wh0_cares.projectstk.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.fragments.PortfolioFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static NavigationView navigationView;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.drawer)
    DrawerLayout drawerLayout;
    TextView title, subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, new PortfolioFragment(), getString(R.string.Portfolio)).addToBackStack(getString(R.string.Portfolio));
        ft.commit();
        initNavigationDrawer();
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