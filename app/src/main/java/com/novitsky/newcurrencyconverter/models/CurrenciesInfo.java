package com.novitsky.newcurrencyconverter.models;

import java.util.HashMap;
import java.util.Map;

public class CurrenciesInfo {
    private final Map<String, Currency> Valute = new HashMap<>();

    public Map<String, Currency> getValute() {
        return Valute;
    }

    public void setValute(String key, Currency value) {
        Valute.put(key, value);
    }
}
