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
import android.widget.TextView;
import android.widget.Toast;

import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.activities.MainActivity;
import com.wh0_cares.projectstk.database.DatabaseHandler;
import com.wh0_cares.projectstk.database.Stocks;
import com.wh0_cares.projectstk.utils.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailFragment extends Fragment {

    private ProgressDialog pDialog;
    String stockSymbol, toolbarTitle;
    private final OkHttpClient client = new OkHttpClient();
    @Bind(R.id.open_value)
    TextView open;
    @Bind(R.id.prevClose_value)
    TextView prevClose;
    @Bind(R.id.volume_value)
    TextView volume;
    @Bind(R.id.volume50avg_value)
    TextView volume50avg;
    @Bind(R.id.marketCap_value)
    TextView marketCap;
    @Bind(R.id.peRatio_value)
    TextView peRatio;
    @Bind(R.id.eps_value)
    TextView eps;
    @Bind(R.id.currentYield_value)
    TextView currentYield;
    DatabaseHandler db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        stockSymbol = getArguments().getString("symbol");
        toolbarTitle = getArguments().getString("title");
        toolbar();
        setHasOptionsMenu(true);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        db = new DatabaseHandler(getActivity());
        if(db.getStock(stockSymbol)){
            MainActivity.fab.setEnabled(false);
        }else{
            MainActivity.fab.setEnabled(true);
        }
        MainActivity.fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    addToPorfolio(stockSymbol);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            getStockRealtime(stockSymbol);
            checkDatabase(stockSymbol);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean contains(String[] a, String b) {
        for (String c : a) {
            if (b.equals(c)) {
                return true;
            }
        }
        return false;
    }

    public void toolbar() {
        MainActivity.collapsingToolbar.setTitle(toolbarTitle);
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

    public void getStockRealtime(String symbol) throws Exception {
        pDialog.setMessage("Getting realtime stock data");
        pDialog.show();
        Request request = new Request.Builder()
                .url(getString(R.string.realtime_url).replaceAll(":symbol", symbol).replaceAll(":index", "NASDAQ"))
                .addHeader("x-access-token", SaveSharedPreference.getToken(getActivity()))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error(getString(R.string.Error_getting_data));
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    error(getString(R.string.Error_getting_data));
                    throw new IOException("Unexpected code " + response.body());
                }
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONArray dataArray = obj.getJSONArray("data");
                    JSONObject dataArrayObj = dataArray.getJSONObject(0);
                    final double open = dataArrayObj.getDouble("open");
                    final double prevClose = dataArrayObj.getDouble("prevClose");
                    final double volume = dataArrayObj.getDouble("volume");
                    final double avgVolume50Day = dataArrayObj.getDouble("avgVolume50Day");
                    final double marketCap = dataArrayObj.getDouble("marketCap");
                    String peRatio = dataArrayObj.getString("peRatio");
                    if(peRatio.equalsIgnoreCase("NE")){
                        peRatio = "0";
                    }
                    final double eps = dataArrayObj.getDouble("eps");
                    final double currentYield = dataArrayObj.getDouble("currentYield");
                    final String finalPeRatio = peRatio;
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pDialog.dismiss();
                            displayData(new double[]{open, prevClose, volume, avgVolume50Day, marketCap, Double.parseDouble(finalPeRatio), eps, currentYield});
                            response.body().close();
                        }
                    });
                } catch (JSONException e) {
                    error(getString(R.string.Invalid_response));
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayData(double[] data) {
        NumberFormat nm = NumberFormat.getCurrencyInstance();
        open.setText(nm.format(data[0]));
        prevClose.setText(nm.format(data[1]));
        volume.setText(nm.format(data[2]));
        volume50avg.setText(nm.format(data[3]));
        marketCap.setText(nm.format(data[4]));
        peRatio.setText(String.valueOf(data[5]));
        eps.setText(nm.format(data[6]));
        currentYield.setText(String.format("%s%%", data[7]));
    }

    public void addToPorfolio(final String symbol) throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.portfolio_url) + symbol)
                .addHeader("x-access-token", SaveSharedPreference.getToken(getActivity()))
                .post(formBody)
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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "Added " + toolbarTitle + " to porfolio", Toast.LENGTH_LONG).show();
                        db.addStock(new Stocks(symbol));
                    }
                });
            }
        });
    }

    public void checkDatabase(final String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.checkDatabase_url).replaceAll(":symbol", symbol))
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
                    if (response.code() == 404) {
                        try {
                            addToDatabase(symbol);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    throw new IOException("Unexpected code " + response.body());
                }
            }
        });
    }

    public void addToDatabase(final String symbol) throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("index", "NASDAQ")
                .add("symbol", symbol)
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.addToDatabase_url))
                .addHeader("Content-Type", "application/json")
                .addHeader("x-access-token", SaveSharedPreference.getToken(getActivity()))
                .post(formBody)
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