package com.novitsky.newcurrencyconverter.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Currency implements Parcelable, Comparable<Currency> {
    private String CharCode;
    private int Nominal;
    private String Name;
    private double Value;

    public Currency() {}

    private Currency(Parcel parcel) {
        setCharCode(parcel.readString());
        setNominal(parcel.readInt());
        setName(parcel.readString());
        setValue(parcel.readDouble());
    }

    public String getCharCode() {
        return CharCode;
    }

    public void setCharCode(String charCode) {
        this.CharCode = charCode;
    }

    public int getNominal() {
        return Nominal;
    }

    public void setNominal(int nominal) {
        this.Nominal = nominal;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        this.Value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getCharCode());
        parcel.writeInt(getNominal());
        parcel.writeString(getName());
        parcel.writeDouble(getValue());
    }

    public static final Parcelable.Creator<Currency> CREATOR = new Parcelable.Creator<Currency>() {

        @Override
        public Currency createFromParcel(Parcel parcel) {
            return new Currency(parcel);
        }

        @Override
        public Currency[] newArray(int i) {
            return new Currency[i];
        }
    };

    @Override
    public int compareTo(Currency currency) {
        return getName().compareTo(currency.getName());
    }
}
