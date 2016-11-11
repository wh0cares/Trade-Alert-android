package com.wh0_cares.projectstk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "stocks-database";
    private static final String TABLE_STOCKS = "stocks";

    private static final String KEY_ID = "id";
    private static final String KEY_SYMBOL = "symbol";
    private static final String KEY_NEXTUPDATE = "next_update";
    private static final String KEY_VOLAVG = "volume_adverage";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_STOCKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SYMBOL + " TEXT,"
                + KEY_VOLAVG + " INTEGER,"
                + KEY_NEXTUPDATE + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS);
        onCreate(db);
    }

    public void addStock(Stocks stock) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SYMBOL, stock.getSymbol());
        values.put(KEY_VOLAVG, stock.getVolAvg());
        values.put(KEY_NEXTUPDATE, stock.getNextUpdate());

        db.insert(TABLE_STOCKS, null, values);
        db.close();
    }

    public boolean getStock(String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_STOCKS, new String[]{
                        KEY_ID,
                        KEY_SYMBOL
                }, KEY_SYMBOL + "=?",
                new String[]{symbol}, null, null, null, null);
        return cursor.moveToFirst();
    }

    public List<Stocks> getAllStocks() {
        List<Stocks> stockList = new ArrayList<Stocks>();

        String selectQuery = "SELECT  * FROM " + TABLE_STOCKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Stocks stock = new Stocks();
                stock.setID(Integer.parseInt(cursor.getString(0)));
                stock.setSymbol(cursor.getString(1));
                stockList.add(stock);
            } while (cursor.moveToNext());
        }
        return stockList;
    }

    public int updateStock(Stocks stock) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SYMBOL, stock.getSymbol());
        values.put(KEY_VOLAVG, stock.getVolAvg());
        values.put(KEY_NEXTUPDATE, stock.getNextUpdate());

        return db.update(TABLE_STOCKS, values, KEY_SYMBOL + " = ?", new String[]{String.valueOf(stock.getSymbol())});
    }

    public void deleteStock(Stocks stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS, KEY_SYMBOL + " = ?", new String[]{String.valueOf(stock.getSymbol())});
        db.close();
    }

    public int getStocksCount() {
        String countQuery = "SELECT  * FROM " + TABLE_STOCKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }
}