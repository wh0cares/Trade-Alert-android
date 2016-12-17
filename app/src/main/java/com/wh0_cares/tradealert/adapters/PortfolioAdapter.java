package com.wh0_cares.tradealert.adapters;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.wh0_cares.tradealert.R;
import com.wh0_cares.tradealert.data.PortfolioData;

import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.MyViewHolder> {

    private List<PortfolioData> stocks;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView cv;
        public TextView stockImage;
        public TextView stockName;
        public TextView stockIndexSymbol;

        public MyViewHolder(View view) {
            super(view);
            cv = (CardView)view.findViewById(R.id.CardView_stock);
            stockImage = (TextView)view.findViewById(R.id.stock_image);
            stockName = (TextView)view.findViewById(R.id.stock_name);
            stockIndexSymbol = (TextView)view.findViewById(R.id.stock_index_symnol);
        }
    }


    public PortfolioAdapter(Context context, List<PortfolioData> stocks) {
        mContext = context;
        this.stocks = stocks;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_portfolio, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final PortfolioData stock = stocks.get(position);
        holder.stockImage.setText(stock.getFirstLetter());
        holder.stockName.setText(stock.getName());
        holder.stockIndexSymbol.setText(stock.getIndex() + " - " + stock.getSymbol());

    }
    @Override
    public int getItemCount() {
        return stocks.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private PortfolioAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final PortfolioAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}