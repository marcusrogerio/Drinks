package fr.masciulli.drinks.test;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.widget.Toolbar;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;
import android.widget.TextView;
import fr.masciulli.drinks.R;
import fr.masciulli.drinks.activity.DrinkDetailActivity;
import fr.masciulli.drinks.model.Drink;
import fr.masciulli.drinks.util.HtmlUtils;

import java.util.Arrays;

public class DrinkDetailActivityTest extends ActivityInstrumentationTestCase2<DrinkDetailActivity> {
    private DrinkDetailActivity activity;

    private Drink drink;

    private Toolbar toolBar;
    private TextView historyTextView;
    private Button wikipediaButton;
    private TextView instructionsView;
    private TextView ingredientsView;

    public DrinkDetailActivityTest() {
        super(DrinkDetailActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        Intent intent = new Intent();

        Drink drinkParcelable = new Drink("Name", "http://imageurl.com", "History", "Instructions", "http://en.wikipedia.org/test");
        drinkParcelable.ingredients = Arrays.asList(new String[] {"ingredient1", "ingredient2" });
        intent.putExtra("drink", drinkParcelable);
        setActivityIntent(intent);
        activity = getActivity();
        drink = activity.getIntent().getParcelableExtra("drink");

        toolBar = (Toolbar) activity.findViewById(R.id.toolbar);
        historyTextView = (TextView) activity.findViewById(R.id.history);
        wikipediaButton = (Button) activity.findViewById(R.id.wikipedia);
        instructionsView = (TextView) activity.findViewById(R.id.instructions);
        ingredientsView = (TextView) activity.findViewById(R.id.ingredients);
    }

    public void testPreconditions() {
        assertNotNull(drink);
        assertNotNull(toolBar);
        assertNotNull(historyTextView);
        assertNotNull(ingredientsView);
        assertNotNull(instructionsView);
    }

    public void testViewContentsAreCorrect() throws InterruptedException {
        Thread.sleep(2000);
        assertEquals(drink.name.toLowerCase(), toolBar.getTitle().toString().toLowerCase());
        assertEquals(drink.history, historyTextView.getText().toString());
        assertEquals(drink.instructions, instructionsView.getText().toString());
    }
}
