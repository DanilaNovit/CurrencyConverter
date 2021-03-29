package com.novitsky.newcurrencyconverter.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.novitsky.newcurrencyconverter.R;
import com.novitsky.newcurrencyconverter.models.Currency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyListAdapter.CurrencyViewHolder>
                                                                    implements Filterable {
    private final List<Currency> currencyListAll;
    private final List<Currency> currencyList;

    private final OnClickItemListListener onClickListener;
    private final LayoutInflater inflater;
    private final Context context;

    private Filter filter;

    public interface OnClickItemListListener {
        void onClick(String charCode);
    }

    public CurrencyListAdapter(Context context, List<Currency> currencyList,
                               OnClickItemListListener onClickListener) {
        this.currencyListAll = currencyList;
        Collections.sort(currencyListAll);

        this.currencyList = new ArrayList<>(currencyListAll);

        this.onClickListener = onClickListener;
        this.context = context;

        inflater = LayoutInflater.from(context);
        initFilter();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.item_currency_list, viewGroup, false);
        return new CurrencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder currencyViewHolder, int i) {
        currencyViewHolder.bind(currencyList.get(i));
    }

    @Override
    public int getItemCount() {
        return currencyList.size();
    }

    private void initFilter() {
        filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence queryText) {
                List<Currency> filteredList = new ArrayList<>();

                if (queryText.toString().isEmpty()) {
                    filteredList.addAll(currencyListAll);
                } else {
                    for (Currency currency : currencyListAll) {
                        boolean containsName = currency.getName().toLowerCase()
                                .contains(queryText.toString().toLowerCase());

                        boolean containsCharCode = currency.getCharCode().toLowerCase()
                                .contains(queryText.toString().toLowerCase());

                        if (containsName || containsCharCode) {
                            filteredList.add(currency);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence queryText, FilterResults filterResults) {
                currencyList.clear();

                @SuppressWarnings("unchecked")
                Collection<? extends Currency> resultsValues =
                        (Collection<? extends Currency>) filterResults.values;

                currencyList.addAll(resultsValues);
                notifyDataSetChanged();
            }
        };
    }

    class CurrencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView flag;
        private final TextView name;
        private final TextView charCode;
        private final TextView value;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);

            flag = itemView.findViewById(R.id.flag);
            name = itemView.findViewById(R.id.name);
            charCode = itemView.findViewById(R.id.charcode);
            value = itemView.findViewById(R.id.value);

            itemView.setOnClickListener(this);
        }

        @SuppressLint("SetTextI18n")
        void bind(Currency currency) {
            Resources res = context.getResources();

            flag.setImageDrawable(res.getDrawable(res.getIdentifier(
                    currency.getCharCode().toLowerCase() + "_flag", "drawable",
                    context.getPackageName())));
            name.setText(currency.getName());
            charCode.setText(currency.getCharCode());
            value.setText(Double.toString(currency.getValue()));
        }

        @Override
        public void onClick(View view) {
            onClickListener.onClick(charCode.getText().toString());
        }
    }
}
