package com.wh0_cares.projectstk.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.activities.MainActivity;
import com.wh0_cares.projectstk.database.DatabaseHandler;
import com.wh0_cares.projectstk.database.Stocks;
import com.wh0_cares.projectstk.utils.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlarmService extends Service {

    private final OkHttpClient client = new OkHttpClient();
    int volAvg, realtimeVol;
    String lastDate;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHandler db = new DatabaseHandler(this);
        List<Stocks> stocks = db.getAllStocks();
        ArrayList<String> tempStocks = new ArrayList<>();
        for (Stocks stock : stocks) {
            try {
                Calendar c = Calendar.getInstance();
                Calendar c2 = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                c.setTime(sdf.parse(String.valueOf(stock.getNextUpdate())));
                if (c.after(c2.getTime())) {
                    updateStock(stock.getSymbol());
                }else{
                    volAvg = stock.getVolAvg();
                }
                getStockRealtimeVolume(stock.getSymbol());
                int digitsLength = (int)(Math.log10(volAvg));
                String compare = String.format("%0"+digitsLength+"d", 1);
                if(Integer.parseInt(compare) + volAvg <= realtimeVol){
                    tempStocks.add(stock.getSymbol());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        SaveSharedPreference.setTempStocks(getApplicationContext(), tempStocks.toArray(new String[0]));
//        if(tempStocks.size() != 0){
            sendNotification(tempStocks.size());
//        }
        AlarmReceiver.completeWakefulIntent(intent);
        return START_REDELIVER_INTENT;
    }

    private void updateStock(final String symbol) throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("index", "NASDAQ")
                .add("symbol", symbol)
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.checkDatabase_url).replaceAll(":symbol", symbol))
                .addHeader("Content-Type", "application/json")
                .addHeader("x-access-token", SaveSharedPreference.getToken(getApplicationContext()))
                .put(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response.body());
                } else {
                    try {
                        checkDatabase(symbol);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    response.body().close();
                }
            }
        });
    }

    public void checkDatabase(final String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.checkDatabase_url).replaceAll(":symbol", symbol))
                .addHeader("x-access-token", SaveSharedPreference.getToken(getApplicationContext()))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
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


                        SimpleDateFormat sdfOld = new SimpleDateFormat("MMM dd, yyyy");
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                        Calendar c = Calendar.getInstance();
                        c.setTime(sdfOld.parse(String.valueOf(lastDate)));
                        c.add(Calendar.DATE, 30);
                        String nextUpdate = sdf.format(c.getTime());


                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        db.updateStock(new Stocks(symbol, nextUpdate, volAvg));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendNotification(int count) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        String message = getString(R.string.notification_message).replaceAll("\\{COUNT\\}", String.valueOf(count));

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(0, builder.build());
    }

    private void getStockRealtimeVolume(String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.realtime_volume_url).replaceAll(":symbol", symbol))
                .addHeader("Content-Type", "application/json")
                .addHeader("x-access-token", SaveSharedPreference.getToken(getApplicationContext()))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response.body());
                } else {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        realtimeVol = obj.getInt("volume");
                        response.body().close();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
