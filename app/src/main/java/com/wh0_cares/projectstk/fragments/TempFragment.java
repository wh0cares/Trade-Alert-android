package com.wh0_cares.projectstk.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wh0_cares.projectstk.R;

import butterknife.ButterKnife;

public class TempFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
    }
}
