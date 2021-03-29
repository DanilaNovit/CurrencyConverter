package com.novitsky.newcurrencyconverter.managers.networkmanager;

import com.novitsky.newcurrencyconverter.models.CurrenciesInfo;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CurrencyService {
    @GET("daily_json.js")
    Call<CurrenciesInfo> getCurrenciesInfo();
}
