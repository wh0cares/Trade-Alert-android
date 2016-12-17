package com.wh0_cares.tradealert.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wh0_cares.tradealert.R;
import com.wh0_cares.tradealert.database.DatabaseHandler;
import com.wh0_cares.tradealert.database.Stocks;
import com.wh0_cares.tradealert.utils.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity  extends AppCompatActivity {

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
    String lastDate;
    int volAvg;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbar;
    FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        stockSymbol = getIntent().getStringExtra("symbol");
        toolbarTitle = getIntent().getStringExtra("title");

        setSupportActionBar(toolbar);
        setTitle(toolbarTitle);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        db = new DatabaseHandler(this);
        if (db.hasStock(stockSymbol)) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
        try {
            getStockRealtime(stockSymbol);
            checkDatabase(stockSymbol);
        } catch (Exception e) {
            e.printStackTrace();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    addToPorfolio(stockSymbol);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void getStockRealtime(String symbol) throws Exception {
        pDialog.setMessage("Getting realtime stock data");
        pDialog.show();
        Request request = new Request.Builder()
                .url(getString(R.string.realtime_url).replaceAll(":symbol", symbol).replaceAll(":index", "NASDAQ"))
                .addHeader("x-access-token", SaveSharedPreference.getToken(this))
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
                    if (peRatio.equalsIgnoreCase("NE")) {
                        peRatio = "0";
                    }
                    final double eps = dataArrayObj.getDouble("eps");
                    final double currentYield = dataArrayObj.getDouble("currentYield");
                    final String finalPeRatio = peRatio;
                    runOnUiThread(new Runnable() {
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
                .addHeader("x-access-token", SaveSharedPreference.getToken(this))
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
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            Toast.makeText(DetailActivity.this, "Added " + toolbarTitle + " to porfolio", Toast.LENGTH_LONG).show();
                            SimpleDateFormat sdfOld = new SimpleDateFormat("MMM dd, yyyy");
                            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                            Calendar c = Calendar.getInstance();
                            c.setTime(sdfOld.parse(String.valueOf(lastDate)));
                            c.add(Calendar.DATE, 30);
                            String nextUpdate = sdf.format(c.getTime());
                            db.addStock(new Stocks(symbol, nextUpdate, volAvg));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void checkDatabase(final String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.checkDatabase_url).replaceAll(":symbol", symbol))
                .addHeader("x-access-token", SaveSharedPreference.getToken(this))
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
                } else {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        JSONObject dataObj = obj.getJSONObject("data");
                        JSONArray datesArray = dataObj.getJSONArray("dates");
                        JSONArray volumesArray = dataObj.getJSONArray("volumes");
                        volAvg = 0;
                        for (int a = 0; a < volumesArray.length(); a++) {
                            volAvg += volumesArray.getInt(a);
                        }
                        volAvg = volAvg / 30;
                        lastDate = datesArray.getString(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                .addHeader("x-access-token", SaveSharedPreference.getToken(this))
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
        runOnUiThread(new Runnable() {
            public void run() {
                pDialog.dismiss();
                Toast.makeText(DetailActivity.this, message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
