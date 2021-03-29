package com.novitsky.newcurrencyconverter.modules.convertermodule;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Intent;

import com.novitsky.newcurrencyconverter.models.Currency;

public interface ViewModel {
    void setObserverOnConvertResult(LifecycleOwner owner, Observer<Double> observer);
    void setObserverOnInitialCurrencyChanged(LifecycleOwner owner, Observer<Currency> observer);
    void setObserverOnFinalCurrencyChanged(LifecycleOwner owner, Observer<Currency> observer);

    void onStartActivity(UpdateCallback updateCallback);
    void convert(String initialText);

    void swapCurrency(String initialText);
    void updateDataFromNetwork(UpdateCallback callback);

    void initialCurrencyChanged(Activity activityForResult);
    void finalCurrencyChanged(Activity activityForResult);

    void onActivityResult(int requestCode, int resultCode, Intent data, int RESULT_CANCELED);

    interface UpdateCallback {
        void onResponse(boolean isSuccessful);
    }
}
