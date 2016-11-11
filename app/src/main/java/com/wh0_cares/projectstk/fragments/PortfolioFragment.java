package com.wh0_cares.projectstk.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.activities.MainActivity;
import com.wh0_cares.projectstk.adapters.PortfolioAdapter;
import com.wh0_cares.projectstk.data.PortfolioData;
import com.wh0_cares.projectstk.database.DatabaseHandler;
import com.wh0_cares.projectstk.database.Stocks;
import com.wh0_cares.projectstk.utils.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortfolioFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private final OkHttpClient client = new OkHttpClient();
    @Bind(R.id.rv)
    RecyclerView rv;
    @Bind(R.id.refresh)
    SwipeRefreshLayout refresh;
    private ArrayList<PortfolioData> stocks;
    PortfolioAdapter adapter;
    DatabaseHandler db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        toolbar();
        refresh.setOnRefreshListener(this);
        refresh.setColorSchemeResources(R.color.colorPrimary);
        refresh.post(new Runnable() {
                         @Override
                         public void run() {
                             refresh.setRefreshing(true);
                             try {
                                 getPortfolio();
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                         }
                     }
        );
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        stocks = new ArrayList<>();
        adapter = new PortfolioAdapter(getActivity(), stocks);
        rv.setAdapter(adapter);
        rv.addOnItemTouchListener(new PortfolioAdapter.RecyclerTouchListener(getActivity(), rv, new PortfolioAdapter.ClickListener() {
            @Override
            public void onClick(final View view, final int position) {
                PortfolioData stock = stocks.get(position);
                final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment fr = new DetailFragment();
                Bundle args = new Bundle();
                args.putString("symbol", stock.getSymbol());
                args.putString("title", stock.getSymbol() + " - " + stock.getName());
                fr.setArguments(args);
                ft.setCustomAnimations(R.anim.fragment1, R.anim.fragment2);
                ft.replace(R.id.container, fr).addToBackStack(getString(R.string.Details));
                ft.commit();
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        ItemTouchHelper.SimpleCallback touchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int swipePos = viewHolder.getAdapterPosition();
                final PortfolioData swipeStock = stocks.get(swipePos);
                Snackbar snackbar = Snackbar
                        .make(rv, "Stock removed from portfolio", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                stocks.add(swipePos, swipeStock);
                                adapter.notifyItemInserted(swipePos);
                            }
                        }).setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                switch (event) {
                                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                        break;
                                    case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                        try {
                                            removeFromPortfolio(swipeStock.getSymbol(), swipePos);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                }
                            }
                        });
                snackbar.show();
                stocks.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;

                    Paint paint = new Paint();
                    Bitmap bitmap;
                    float translationX;

                    if (dX > 0) { //swiping right
                        translationX = Math.min(dX, viewHolder.itemView.getWidth() / 6);
                        paint.setColor(getResources().getColor(R.color.red));
                        bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_delete);
                        float height = (itemView.getHeight() / 2) - (bitmap.getHeight() / 2);

                        c.drawRect((float) itemView.getLeft() + dX, (float) itemView.getTop(), (float) itemView.getLeft(), (float) itemView.getBottom(), paint);
                        c.drawBitmap(bitmap, 96f, (float) itemView.getTop() + height, null);

                    } else { //swiping left
                        translationX = Math.max(dX, (-1) * viewHolder.itemView.getWidth() / 6);
                        paint.setColor(getResources().getColor(R.color.red));
                        bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_delete);
                        float height = (itemView.getHeight() / 2) - (bitmap.getHeight() / 2);
                        float bitmapWidth = bitmap.getWidth();
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                        c.drawBitmap(bitmap, ((float) itemView.getRight() - bitmapWidth) - 96f, (float) itemView.getTop() + height, null);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
                }
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(rv);
        db = new DatabaseHandler(getActivity());
    }

    public void toolbar() {
        getActivity().setTitle(getString(R.string.Portfolio));
        MainActivity.disableCollapse();
        if (Build.VERSION.SDK_INT >= 21) {
            float scale = getResources().getDisplayMetrics().density;
            int px = (int) (6 * scale + 0.5f);
            View toolbar = getActivity().findViewById(R.id.toolbar);
            toolbar.setElevation(px);
        }
    }

    public void removeFromPortfolio(final String stockSymbol, final int stockPosition) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.portfolio_url) + stockSymbol)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-access-token", SaveSharedPreference.getToken(getActivity()))
                .delete()
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
                db.deleteStock(new Stocks(stockSymbol));
            }
        });
    }

    public void getPortfolio() throws Exception {
        stocks.clear();
        Request request = new Request.Builder()
                .url(getString(R.string.portfolio_url))
                .addHeader("Content-Type", "application/json")
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
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);
                        String firstLetter = String.valueOf(item.getString("name").charAt(0));
                        String name = item.getString("name");
                        String index = item.getString("index");
                        String symbol = item.getString("symbol");
                        JSONArray datesArray = item.getJSONArray("dates");
                        JSONArray volumesArray = item.getJSONArray("volumes");
                        int volAvg = 0;
                        for (int a = 0; a < volumesArray.length(); a++) {
                            volAvg += volumesArray.getInt(a);
                        }
                        volAvg = volAvg / 30;
                        String lastDate = datesArray.getString(0);
                        PortfolioData stock = new PortfolioData();
                        stock.setFirstLetter(firstLetter);
                        stock.setName(name);
                        stock.setIndex(index);
                        stock.setSymbol(symbol);
                        stocks.add(stock);
                        if (!db.getStock(symbol)) {
                            try {
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
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            adapter.notifyDataSetChanged();
                            refresh.setRefreshing(false);
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

    public void error(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                refresh.setRefreshing(false);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRefresh() {
        try {
            getPortfolio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}