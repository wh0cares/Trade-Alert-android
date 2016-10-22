package com.wh0_cares.projectstk.fragments;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.activities.MainActivity;
import com.wh0_cares.projectstk.utils.SaveSharedPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailFragment extends Fragment {

    private ProgressDialog pDialog;
    String stockSymbol;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        stockSymbol = getArguments().getString("symbol");
        toolbar();
        setHasOptionsMenu(true);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
    }

    public void toolbar() {
        getActivity().setTitle(stockSymbol);
        MainActivity.enableCollapse();
        if (Build.VERSION.SDK_INT >= 21) {
            float scale = getResources().getDisplayMetrics().density;
            int px = (int) (6 * scale + 0.5f);
            View toolbar = getActivity().findViewById(R.id.toolbar);
            toolbar.setElevation(px);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getStock(String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.getStock_url) + symbol)
                .addHeader("x-access-token", SaveSharedPreference.getToken(getActivity()))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error(getString(R.string.Error_getting_data));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    error(getString(R.string.Error_getting_data));
                    throw new IOException("Unexpected code " + response.body());
                }
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    pDialog.dismiss();
                } catch (JSONException e) {
                    error(getString(R.string.Invalid_response));
                    e.printStackTrace();
                }
            }
        });
    }

    public void error(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                pDialog.dismiss();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                getActivity().getSupportFragmentManager().popBackStack();
                ft.setCustomAnimations(R.anim.back1, R.anim.back2);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}