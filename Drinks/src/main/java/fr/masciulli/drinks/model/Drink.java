package fr.masciulli.drinks.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Drink implements Parcelable {
    public String name;
    public String imageUrl;
    public String history;
    public String instructions;
    public List<String> ingredients = new ArrayList<String>();
    public String wikipedia;

    public Drink() {
    }

    public Drink(String name, String imageUrl, String history, String instructions, String wikipedia) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.history = history;
        this.instructions = instructions;
        this.wikipedia = wikipedia;
    }

    private Drink(Parcel parcel) {
        name = parcel.readString();
        imageUrl = parcel.readString();
        history = parcel.readString();
        instructions = parcel.readString();
        parcel.readStringList(ingredients);
        wikipedia = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(imageUrl);
        parcel.writeString(history);
        parcel.writeString(instructions);
        parcel.writeStringList(ingredients);
        parcel.writeString(wikipedia);
    }

    public static final Parcelable.Creator<Drink> CREATOR = new Parcelable.Creator<Drink>() {
        public Drink createFromParcel(Parcel in) {
            return new Drink(in);
        }

        public Drink[] newArray(int size) {
            return new Drink[size];
        }
    };
}
