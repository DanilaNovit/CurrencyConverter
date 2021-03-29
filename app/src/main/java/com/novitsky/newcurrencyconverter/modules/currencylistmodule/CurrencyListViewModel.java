package com.novitsky.newcurrencyconverter.modules.currencylistmodule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.novitsky.newcurrencyconverter.adapters.CurrencyListAdapter;
import com.novitsky.newcurrencyconverter.models.Currency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CurrencyListViewModel implements ViewModel {
    public static final String CURRENCY_LIST_EXTRA = "CURRENCY_LIST_EXTRA";
    public static final String RESULT_EXTRA = "RESULT_EXTRA";

    private final Context context;
    private List<Currency> currencyList;

    private CurrencyListAdapter adapter;

    CurrencyListViewModel(Context context) {
        this.context = context;
    }

    @Override
    public void onQueryTextChange(String queryText) {
        adapter.getFilter().filter(queryText);
    }

    @Override
    public void onCreateActivity(Intent intent, RecyclerView recyclerView,
                                 OnReturnResultListener listener) {
        try {
            currencyList = intent.getBundleExtra(CURRENCY_LIST_EXTRA)
                    .getParcelableArrayList(CURRENCY_LIST_EXTRA);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        if (currencyList == null) {
            return;
        }

        adapter = new CurrencyListAdapter(context, currencyList, resultCharCode -> {
            Intent result = new Intent();
            result.putExtra(RESULT_EXTRA, resultCharCode);
            listener.onReturnResult(result);
        });

        recyclerView.setAdapter(adapter);
    }

    public static Intent createIntent(Context context, Collection<Currency> currencyCollection) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(CURRENCY_LIST_EXTRA, new ArrayList<>(currencyCollection));
        Intent intent = new Intent(context, CurrencyListActivity.class);
        intent.putExtra(CURRENCY_LIST_EXTRA, bundle);
        return intent;
    }

    public static String getResultFromIntent(Intent intent) {
        return intent.getStringExtra(RESULT_EXTRA);
    }
}
