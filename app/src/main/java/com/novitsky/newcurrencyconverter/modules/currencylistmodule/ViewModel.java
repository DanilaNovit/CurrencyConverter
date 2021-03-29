package com.novitsky.newcurrencyconverter.modules.currencylistmodule;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;

public interface ViewModel {
    void onQueryTextChange(String queryText);
    void onCreateActivity(Intent intent, RecyclerView recyclerView, OnReturnResultListener listener);

    interface OnReturnResultListener {
        void onReturnResult(Intent result);
    }
}
