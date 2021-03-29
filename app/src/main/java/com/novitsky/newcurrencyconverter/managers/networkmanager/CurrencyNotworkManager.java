package com.novitsky.newcurrencyconverter.managers.networkmanager;

import com.novitsky.newcurrencyconverter.models.CurrenciesInfo;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrencyNotworkManager implements NetworkManager {
    private static final String BASE_URL = "https://www.cbr-xml-daily.ru/";
    private final CurrencyService service;

    public CurrencyNotworkManager() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(CurrencyService.class);
    }

    @Override
    public void getCurrencies(Callback callback) {
        service.getCurrenciesInfo().enqueue(new retrofit2.Callback<CurrenciesInfo>() {
            @Override
            public void onResponse(@NotNull Call<CurrenciesInfo> call,
                                   @NotNull Response<CurrenciesInfo> response) {
                callback.onResponse(response.body().getValute(), response.code());
            }

            @Override
            public void onFailure(@NotNull Call<CurrenciesInfo> call, @NotNull Throwable t) {
                callback.onResponse(null, 0);
            }
        });
    }
}
