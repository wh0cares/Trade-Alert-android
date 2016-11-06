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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlarmService extends Service {

    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHandler db = new DatabaseHandler(this);
        List<Stocks> stocks = db.getAllStocks();
        for (Stocks stock : stocks) {
            //TODO compare realtime volume
            try {
                getStockRealtimeVolume(stock.getSymbol());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AlarmReceiver.completeWakefulIntent(intent);
        return START_REDELIVER_INTENT;
    }

    private void sendNotification(String msg) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Test Title")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(0, builder.build());
    }

    private void getStockRealtimeVolume(String symbol) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.realtime_volume_url).replaceAll(":symbol", symbol))
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
                        int volume = obj.getInt("volume");
                        response.body().close();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
