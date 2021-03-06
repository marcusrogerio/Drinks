package fr.masciulli.drinks.activity;

import android.os.Bundle;
import fr.masciulli.drinks.R;
import fr.masciulli.drinks.fragment.LiquorDetailFragment;
import fr.masciulli.drinks.model.Liquor;

public class LiquorDetailActivity extends ToolbarActivity {
    public static final String ARG_LIQUOR = "liquor";

    private LiquorDetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            detailFragment = LiquorDetailFragment.newInstance((Liquor) getIntent().getParcelableExtra(ARG_LIQUOR));
            getFragmentManager().beginTransaction()
                    .add(R.id.liquor_detail_container, detailFragment)
                    .commit();
        } else {
            detailFragment = (LiquorDetailFragment) getFragmentManager().findFragmentById(R.id.liquor_detail_container);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_liquor_detail;
    }
}
