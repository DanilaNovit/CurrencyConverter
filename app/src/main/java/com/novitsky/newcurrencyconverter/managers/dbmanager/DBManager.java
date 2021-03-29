package com.novitsky.newcurrencyconverter.managers.dbmanager;

import com.novitsky.newcurrencyconverter.models.Currency;

import java.util.Map;

public interface DBManager {
    void getAll(Callback callback);
    void putAll(Map<String, Currency> currencyMap);

    interface Callback {
        void onResponse(Map<String, Currency> response);
    }
}
