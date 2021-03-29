package com.novitsky.newcurrencyconverter.modules.convertermodule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.novitsky.newcurrencyconverter.managers.dbmanager.DBManager;
import com.novitsky.newcurrencyconverter.managers.networkmanager.NetworkManager;
import com.novitsky.newcurrencyconverter.models.Currency;
import com.novitsky.newcurrencyconverter.modules.currencylistmodule.CurrencyListViewModel;

import java.util.Date;
import java.util.Map;

public class ConverterViewModel implements ViewModel {
    private static final String TIME_PREFERENCES = "TIME_PREFERENCES";
    private static final String INITIAL_CURRENCY_PREFERENCES = "INITIAL_CURRENCY_PREFERENCES";
    private static final String FINAL_CURRENCY_PREFERENCES = "FINAL_CURRENCY_PREFERENCES";
    private static final String DEFAULT_INITIAL_CURRENCY = "RUB";
    private static final String DEFAULT_FINAL_CURRENCY = "USD";
    private static final int UPDATE_TIME_INTERVAL = 2 * 3600000;
    private static final int INITIAL_REQUEST = 10;
    private static final int FINAL_REQUEST = 20;

    private final MutableLiveData<Double> convertResult = new MutableLiveData<>();
    private final MutableLiveData<Currency> initialCurrency  = new MutableLiveData<>();
    private final MutableLiveData<Currency> finalCurrency  = new MutableLiveData<>();

    private Map<String, Currency> currencyMap;

    private final NetworkManager networkManager;
    private final DBManager dbManager;
    private final Context context;

    ConverterViewModel(NetworkManager networkManager, DBManager dbManager, Context context) {
        this.networkManager = networkManager;
        this.dbManager = dbManager;
        this.context = context;
    }

    @Override
    public void setObserverOnConvertResult(LifecycleOwner owner, Observer<Double> observer) {
        convertResult.observe(owner, observer);
    }

    @Override
    public void setObserverOnInitialCurrencyChanged(LifecycleOwner owner, Observer<Currency> observer) {
        initialCurrency.observe(owner, observer);
    }

    @Override
    public void setObserverOnFinalCurrencyChanged(LifecycleOwner owner, Observer<Currency> observer) {
        finalCurrency.observe(owner, observer);
    }

    @Override
    public void onStartActivity(UpdateCallback updateCallback) {
        updateData(updateCallback);
    }

    @Override
    public void swapCurrency(String initialText) {
        Currency tempCurrency = initialCurrency.getValue();
        initialCurrency.setValue(finalCurrency.getValue());
        finalCurrency.setValue(tempCurrency);
    }

    @Override
    public void convert(String initialText) {
        if (initialCurrency.getValue() == null || finalCurrency.getValue() == null) {
            return;
        }

        double conversionMultiplier;
        double initialNominal = initialCurrency.getValue().getNominal();
        double finalNominal = finalCurrency.getValue().getNominal();

        if (initialNominal > finalNominal) {
            conversionMultiplier = (initialCurrency.getValue().getValue() / finalCurrency.getValue().getValue()) /
                    (initialNominal / finalNominal);
        } else if (initialNominal < finalNominal) {
            conversionMultiplier = (initialCurrency.getValue().getValue() / finalCurrency.getValue().getValue()) *
                    (finalNominal / initialNominal);
        } else {
            conversionMultiplier = (initialCurrency.getValue().getValue() / finalCurrency.getValue().getValue());
        }

        if (initialText.indexOf(".") == 0) {
            initialText = "0" + initialText;
        }

        if (!initialText.equals("")) {
            convertResult.setValue(Double.parseDouble(initialText) * conversionMultiplier);
        } else {
            convertResult.setValue(.0);
        }
    }

    @Override
    public void updateDataFromNetwork(UpdateCallback callback) {
        networkManager.getCurrencies((responseNet, responseCode) -> {
            if (responseNet == null) {
                if (responseCode == 0) {
                    Toast toast = Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT);
                    toast.show();
                }

                if (currencyMap == null) {
                    dbManager.getAll(responseDB -> {
                        if (responseDB != null) {
                            currencyMap = responseDB;
                        }
                    });
                }
            } else {
                currencyMap = responseNet;
                putRub();
                saveData();
            }

            if (callback != null) {
                callback.onResponse(currencyMap != null);
            }

            initInitialAndFinalCurrency();
        });
    }

    @Override
    public void initialCurrencyChanged(Activity activityForResult) {
        startCurrencyListActivity(INITIAL_REQUEST, activityForResult);
    }

    @Override
    public void finalCurrencyChanged(Activity activityForResult) {
        startCurrencyListActivity(FINAL_REQUEST, activityForResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, int RESULT_CANCELED) {
        if (data == null || requestCode == RESULT_CANCELED) {
            return;
        }

        String resultKey = CurrencyListViewModel.getResultFromIntent(data);

        switch (requestCode) {
            case (INITIAL_REQUEST):
                initialCurrency.setValue(currencyMap.get(resultKey));
                setCurrencyPreferences(INITIAL_CURRENCY_PREFERENCES, initialCurrency.getValue());
                break;

            case (FINAL_REQUEST):
                finalCurrency.setValue(currencyMap.get(resultKey));
                setCurrencyPreferences(FINAL_CURRENCY_PREFERENCES, finalCurrency.getValue());
            break;
        }
    }

    private void setCurrencyPreferences(String key, Currency currency) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        preferencesEditor.putString(key, currency.getCharCode());
        preferencesEditor.apply();
    }

    private void startCurrencyListActivity(int requestCode, Activity activity) {
        Intent intent = CurrencyListViewModel.createIntent(context, currencyMap.values());
        activity.startActivityForResult(intent, requestCode);
    }

    private void initInitialAndFinalCurrency() {
        if (currencyMap == null || initialCurrency.getValue() != null) {
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String initialCharCode = preferences.getString(INITIAL_CURRENCY_PREFERENCES, null);
        String finalCharCode = preferences.getString(FINAL_CURRENCY_PREFERENCES, null);

        if (initialCharCode == null || finalCharCode == null) {
            initialCurrency.setValue(currencyMap.get(DEFAULT_INITIAL_CURRENCY));
            finalCurrency.setValue(currencyMap.get(DEFAULT_FINAL_CURRENCY));
            return;
        }

        initialCurrency.setValue(currencyMap.get(initialCharCode));
        finalCurrency.setValue(currencyMap.get(finalCharCode));
    }

    void updateData(UpdateCallback updateCallback) {
        if (checkExpired()) {
            updateDataFromNetwork(updateCallback);
        } else {
            getDataFromDB();
        }
    }

    private boolean checkExpired() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long oldTime = preferences.getLong(TIME_PREFERENCES, -1);

        if (oldTime == -1) {
            return true;
        }

        long newTime = new Date().getTime();

        return oldTime <= (newTime - UPDATE_TIME_INTERVAL);
    }

    private void getDataFromDB() {
        dbManager.getAll(responseDB -> {
            if (responseDB == null) {
                networkManager.getCurrencies((responseNet, responseCode) -> {
                    if (responseCode == 0) {
                        Toast toast = Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    if (responseNet != null) {
                        currencyMap = responseNet;
                        putRub();
                        saveData();
                    }
                });
            } else {
                currencyMap = responseDB;
            }

            initInitialAndFinalCurrency();
        });
    }

    private void putRub() {
        Currency rub = new Currency();
        rub.setName("Российский рубль");
        rub.setNominal(1);
        rub.setCharCode("RUB");
        rub.setValue(1);

        currencyMap.put("RUB", rub);
    }

    private void saveData() {
        if (currencyMap == null) {
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        preferencesEditor.putLong(TIME_PREFERENCES, new Date().getTime());
        preferencesEditor.apply();

        dbManager.putAll(currencyMap);
    }
}
