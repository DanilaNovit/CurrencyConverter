package com.novitsky.newcurrencyconverter.managers.dbmanager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import com.novitsky.newcurrencyconverter.models.Currency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyDBManager implements DBManager {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "DBOfCurrencies";
    private static final String TABLE_NAME = "TableOfCurrencies";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CHARCODE = "CHARCODE";
    private static final String COLUMN_NOMINAL = "NOMINAL";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_VALUE = "VALUE";

    private final SQLiteOpenHelper helper;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public CurrencyDBManager(Context context) {
        helper = new DBHelper(context);
    }

    @Override
    public void getAll(Callback callback) {
        Runnable getAllOnBackground = () -> {
            SQLiteDatabase db;
            final Map<String, Currency> response = new HashMap<>();

            try {
                db = helper.getReadableDatabase();
            } catch (SQLException ex) {
                ex.printStackTrace();
                return;
            }

            @SuppressLint("Recycle")
            Cursor cursor = db.query(TABLE_NAME,
                    new String[] {COLUMN_CHARCODE, COLUMN_NOMINAL, COLUMN_NAME, COLUMN_VALUE},
                    null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Currency tempCurrency = new Currency();

                    tempCurrency.setCharCode(cursor.getString(cursor.getColumnIndex(COLUMN_CHARCODE)));
                    tempCurrency.setNominal(cursor.getInt(cursor.getColumnIndex(COLUMN_NOMINAL)));
                    tempCurrency.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                    tempCurrency.setValue(cursor.getDouble(cursor.getColumnIndex(COLUMN_VALUE)));

                    response.put(tempCurrency.getCharCode(), tempCurrency);
                } while (cursor.moveToNext());
            } else {
                mainHandler.post(() -> callback.onResponse(null));
            }

            db.close();

            mainHandler.post(() -> callback.onResponse(response));
        };

        Thread getThread = new Thread(getAllOnBackground);
        getThread.start();
    }

    @Override
    public void putAll(Map<String, Currency> currencyMap) {
        Runnable putAllOnBackground = () -> {
            SQLiteDatabase db;

            try {
                db = helper.getWritableDatabase();
            } catch (SQLException ex) {
                ex.printStackTrace();
                return;
            }

            @SuppressLint("Recycle")
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

            ContentValues values =  new ContentValues();

            if (cursor.moveToFirst()) {
                List<Currency> currencyList = new ArrayList<>(currencyMap.values());

                for (int i = 0; i < currencyList.size(); ++i) {
                    values.put(COLUMN_VALUE, currencyList.get(i).getValue());
                    db.update(TABLE_NAME, values, COLUMN_ID + "=" + (i + 1), null);
                }
            } else {
                for (Currency currency : currencyMap.values()) {
                    values.put(COLUMN_CHARCODE, currency.getCharCode());
                    values.put(COLUMN_NOMINAL, currency.getNominal());
                    values.put(COLUMN_NAME, currency.getName());
                    values.put(COLUMN_VALUE, currency.getValue());

                    db.insert(TABLE_NAME, null, values);
                }
            }

            db.close();
        };

        Thread putThread = new Thread(putAllOnBackground);
        putThread.start();
    }

    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_CHARCODE + " TEXT," +
                    COLUMN_NOMINAL + " INTEGER," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_VALUE + " REAL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
