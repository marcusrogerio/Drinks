package fr.masciulli.drinks.test;

import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;
import fr.masciulli.drinks.R;
import fr.masciulli.drinks.activity.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity activity;
    private boolean dualPane;
    private ViewPager viewPager;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
        dualPane = activity.getResources().getBoolean(R.bool.dualpane);
        if (!dualPane) {
            viewPager = (ViewPager) activity.findViewById(R.id.pager);
        }
    }

    public void testPreconditions() {
        assertNotNull(viewPager);
        assertTrue(viewPager.getAdapter().getCount() == 2);
    }
}
