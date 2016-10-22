package com.wh0_cares.projectstk.fragments;

import android.os.Build;
import android.os.Bundle;
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
import com.wh0_cares.projectstk.utils.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortfolioFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    //private ProgressDialog pDialog;
    private final OkHttpClient client = new OkHttpClient();
    @Bind(R.id.rv)
    RecyclerView rv;
    @Bind(R.id.refresh)
    SwipeRefreshLayout refresh;
    private ArrayList<PortfolioData> stocks;
    PortfolioAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        toolbar();
//        pDialog = new ProgressDialog(getActivity());
//        pDialog.setIndeterminate(true);
//        pDialog.setCanceledOnTouchOutside(false);
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
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                try {
                    deleteUserStock(stocks.get(viewHolder.getAdapterPosition()).getID(), viewHolder.getAdapterPosition());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(rv);
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
        MainActivity.setDrawerEnabled(true);
        MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
    }

    public void deleteUserStock(final int stockID, final int stockPosition) throws Exception {
        Request request = new Request.Builder()
                .url(getString(R.string.portfolio_url) + stockID)
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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        stocks.remove(stockPosition);
                        adapter.notifyItemRemoved(stockPosition);
                    }
                });
            }
        });
    }

    public void getPortfolio() throws Exception {
//        pDialog.setMessage(getString(R.string.Getting_portfolio));
//        pDialog.show();
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
            public void onResponse(Call call, Response response) throws IOException {
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
                        int id = item.getInt("id");
                        PortfolioData stock = new PortfolioData();
                        stock.setFirstLetter(firstLetter);
                        stock.setName(name);
                        stock.setIndex(index);
                        stock.setSymbol(symbol);
                        stock.setID(id);
                        stocks.add(stock);

                    }
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            //pDialog.dismiss();
                            adapter.notifyDataSetChanged();
                            refresh.setRefreshing(false);
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
                //pDialog.dismiss();
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