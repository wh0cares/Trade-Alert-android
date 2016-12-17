package com.wh0_cares.tradealert.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wh0_cares.tradealert.R;
import com.wh0_cares.tradealert.adapters.TempAdapter;
import com.wh0_cares.tradealert.data.TempData;
import com.wh0_cares.tradealert.database.DatabaseHandler;
import com.wh0_cares.tradealert.database.Stocks;
import com.wh0_cares.tradealert.utils.SaveSharedPreference;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TempFragment extends Fragment {

    @Bind(R.id.rv)
    RecyclerView rv;
    private ArrayList<TempData> stocks;
    TempAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temp, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        stocks = new ArrayList<>();
        adapter = new TempAdapter(getActivity(), stocks);
        rv.setAdapter(adapter);

        DatabaseHandler db = new DatabaseHandler(getActivity());
        TempData stock = new TempData();
        if(!SaveSharedPreference.getTempStocks(getActivity())[0].equals("")) {
            for (String tempSymbol : SaveSharedPreference.getTempStocks(getActivity())) {
                Stocks tempStock = db.getStock(tempSymbol);
                stock.setFirstLetter(String.valueOf(tempStock.getName().charAt(0)));
                stock.setName(tempStock.getName());
                stock.setIndex(tempStock.getIndex());
                stock.setSymbol(tempStock.getSymbol());
                stocks.add(stock);
            }
            adapter.notifyDataSetChanged();
        }
    }
}
