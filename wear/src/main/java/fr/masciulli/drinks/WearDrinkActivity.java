package fr.masciulli.drinks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class WearDrinkActivity extends Activity {

    private TextView mTextView;
    private WearDrink mDrink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_drink);
        mTextView = (TextView)findViewById(R.id.text);
        Intent intent = getIntent();
        if (intent.hasExtra("drink")) {
            mDrink = intent.getParcelableExtra("drink");
            updateViews();
        }
    }

    private void updateViews() {
        if (mDrink != null) {
            mTextView.setText(mDrink.name);
        }
    }
}