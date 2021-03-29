package com.novitsky.newcurrencyconverter.managers.networkmanager;

import com.novitsky.newcurrencyconverter.models.Currency;

import java.util.Map;

public interface NetworkManager {
    void getCurrencies(Callback callback);

    interface Callback {
        void onResponse(Map<String, Currency> response, int responseCode);
    }
}
