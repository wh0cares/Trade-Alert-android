package com.wh0_cares.projectstk.fragments;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.activities.MainActivity;

import butterknife.ButterKnife;

public class PortfolioFragment extends Fragment {

    private ProgressDialog pDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        toolbar();
        pDialog = new ProgressDialog(getActivity());
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
    }

    public void toolbar() {
        getActivity().setTitle(getString(R.string.Portfolio));
        if (Build.VERSION.SDK_INT >= 21) {
            float scale = getResources().getDisplayMetrics().density;
            int px = (int) (6 * scale + 0.5f);
            View toolbar = getActivity().findViewById(R.id.toolbar);
            toolbar.setElevation(px);
        }
        MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
    }
}