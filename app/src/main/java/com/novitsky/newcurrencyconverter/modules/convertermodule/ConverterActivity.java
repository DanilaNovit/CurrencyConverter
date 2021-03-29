package com.novitsky.newcurrencyconverter.modules.convertermodule;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.novitsky.newcurrencyconverter.R;
import com.novitsky.newcurrencyconverter.managers.dbmanager.CurrencyDBManager;
import com.novitsky.newcurrencyconverter.managers.networkmanager.CurrencyNotworkManager;
import com.novitsky.newcurrencyconverter.models.Currency;

import java.text.DecimalFormat;

public class ConverterActivity extends AppCompatActivity {
    private static final DecimalFormat resultFormat = new DecimalFormat("#.##");

    private ViewModel viewModel;

    private LinearLayout initialCurrencyView;
    private LinearLayout finalCurrencyView;
    private ImageView initialFlagView;
    private ImageView finalFlagView;
    private ImageView swapView;
    private ImageView updateView;
    private EditText initialTextView;
    private TextView resultView;
    private TextView initialCharCodeView;
    private TextView finalCharCodeView;

    private ObjectAnimator updateAnimation;

    private ViewModel.UpdateCallback updateCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        init();
        setClickableInterface(false);
        setObservers();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.onStartActivity(updateCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewModel.onActivityResult(requestCode, resultCode, data, RESULT_CANCELED);
    }

    private void init() {
        initialCurrencyView = findViewById(R.id.initial_currency);
        finalCurrencyView = findViewById(R.id.final_currency);
        initialFlagView = findViewById(R.id.initial_flag);
        finalFlagView = findViewById(R.id.final_flag);
        swapView = findViewById(R.id.swap);
        updateView = findViewById(R.id.update);
        initialTextView = findViewById(R.id.initial_text);
        resultView = findViewById(R.id.result);
        initialCharCodeView = findViewById(R.id.initial_charcode);
        finalCharCodeView = findViewById(R.id.final_charcode);

        viewModel = new ConverterViewModel(new CurrencyNotworkManager(),
                new CurrencyDBManager(this), this);

        updateAnimation = ObjectAnimator.ofFloat(updateView, "rotation", 0.0f, 360f);
        updateAnimation.setDuration(1600);
        updateAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        updateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        updateAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        updateAnimation.start();

        updateCallback = isSuccessful -> {
            updateAnimation.pause();
            returnUpdateViewToPosition();

            if (isSuccessful) {
                setClickableInterface(true);
            }

            viewModel.convert(initialTextView.getText().toString());
        };
    }

    @SuppressLint("SetTextI18n")
    private void setObservers() {
        viewModel.setObserverOnConvertResult(this, resultValue -> {
            if (resultValue == 0) {
                resultView.setText(getString(R.string.default_data));
            } else {
                resultView.setText(resultFormat.format(resultValue));
            }
        });

        viewModel.setObserverOnInitialCurrencyChanged(this,
                createObserver(initialFlagView, initialCharCodeView));

        viewModel.setObserverOnFinalCurrencyChanged(this,
                createObserver(finalFlagView, finalCharCodeView));
    }

    private Observer<Currency> createObserver(ImageView flagView, TextView charCodeView) {
        return currency -> {
            if (currency == null) {
                return;
            }

            Resources res = getResources();
            flagView.setImageDrawable(res.getDrawable(res.getIdentifier(
                    currency.getCharCode().toLowerCase() + "_flag", "drawable",
                    ConverterActivity.this.getPackageName())));

            charCodeView.setText(currency.getCharCode());

            updateCallback.onResponse(true);
        };
    }

    private void setListeners() {
        swapView.setOnClickListener(view ->
                viewModel.swapCurrency(initialTextView.getText().toString()));

        updateView.setOnClickListener(view -> {
            viewModel.updateDataFromNetwork(updateCallback);

            setClickableInterface(false);
            updateAnimation.start();
        });

        initialTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence initialText, int i, int i1, int i2) {
                viewModel.convert(initialText.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        initialCurrencyView.setOnClickListener(view -> viewModel.initialCurrencyChanged(this));
        finalCurrencyView.setOnClickListener(view -> viewModel.finalCurrencyChanged(this));
    }

    private void setClickableInterface(boolean clickable) {
        initialCurrencyView.setClickable(clickable);
        finalCurrencyView.setClickable(clickable);
        swapView.setClickable(clickable);
        initialTextView.setEnabled(clickable);
    }

    private void returnUpdateViewToPosition() {
        updateAnimation.setDuration(0);
        updateAnimation.start();
        updateAnimation.pause();
        updateAnimation.setDuration(1600);
    }
}
