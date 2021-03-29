package com.novitsky.newcurrencyconverter.modules.currencylistmodule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.novitsky.newcurrencyconverter.R;

public class CurrencyListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        init();
        toolbarSetting();
        viewModel.onCreateActivity(getIntent(), recyclerView, result -> {
            setResult(RESULT_OK, result);
            finish();
        });
    }

    private void init() {
        viewModel = new CurrencyListViewModel(this);

        recyclerView = findViewById(R.id.currency_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void toolbarSetting() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setTitle(R.string.currency_list);
        toolbar.setNavigationOnClickListener(view -> CurrencyListActivity.this.finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_currency_list, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                viewModel.onQueryTextChange(s);
                return false;
            }
        });

        return true;
    }
}