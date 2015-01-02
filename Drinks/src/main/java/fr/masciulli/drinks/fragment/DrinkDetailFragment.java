package fr.masciulli.drinks.fragment;

import android.animation.TimeInterpolator;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;
import fr.masciulli.android_quantizer.lib.ColorQuantizer;
import fr.masciulli.drinks.R;
import fr.masciulli.drinks.activity.ToolbarActivity;
import fr.masciulli.drinks.model.Drink;
import fr.masciulli.drinks.util.AnimUtils;
import fr.masciulli.drinks.util.HtmlUtils;
import fr.masciulli.drinks.view.BlurTransformation;
import fr.masciulli.drinks.view.ObservableScrollView;
import fr.masciulli.drinks.view.ScrollViewListener;

import java.util.ArrayList;

public class DrinkDetailFragment extends Fragment implements ScrollViewListener {

    private static final String STATE_DRINK = "drink";
    private static final long ANIM_IMAGE_ENTER_DURATION = 500;
    private static final long ANIM_TEXT_ENTER_DURATION = 500;
    private static final long ANIM_IMAGE_ENTER_STARTDELAY = 300;
    private static final long ANIM_COLORBOX_ENTER_DURATION = 200;

    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();

    private Toolbar mToolbar;

    @InjectView(R.id.image)
    ImageView mImageView;
    @InjectView(R.id.image_blurred)
    ImageView mBlurredImageView;
    @InjectView(R.id.history)
    TextView mHistoryView;
    @InjectView(R.id.scroll)
    ObservableScrollView mScrollView;
    @InjectView(R.id.ingredients)
    TextView mIngredientsView;
    @InjectView(R.id.instructions)
    TextView mInstructionsView;
    @InjectView(R.id.wikipedia)
    Button mWikipediaButton;
    @InjectView(R.id.colorbox)
    View mColorBox;
    @InjectView(R.id.color1)
    View mColorView1;
    @InjectView(R.id.color2)
    View mColorView2;
    @InjectView(R.id.color3)
    View mColorView3;
    @InjectView(R.id.color4)
    View mColorView4;

    private int mImageViewHeight;

    private Transformation mTransformation;

    private Drink mDrink;

    private Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mImageView.setImageBitmap(bitmap);
            new QuantizeBitmapTask().execute(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mToolbar = ((ToolbarActivity) getActivity()).getToolbar();
        mToolbar.getBackground().setAlpha(0);

        View root = inflater.inflate(R.layout.fragment_drink_detail, container, false);
        ButterKnife.inject(this, root);

        setHasOptionsMenu(true);

        Intent intent = getActivity().getIntent();
        mDrink = intent.getParcelableExtra("drink");

        getActivity().setTitle(mDrink.name);
        Picasso.with(getActivity()).load(mDrink.imageUrl).into(mTarget);

        mTransformation = new BlurTransformation(getActivity(), getResources().getInteger(R.integer.blur_radius));
        Picasso.with(getActivity()).load(mDrink.imageUrl).transform(mTransformation).into(mBlurredImageView);

        mImageViewHeight = (int) getResources().getDimension(R.dimen.drink_detail_recipe_margin);
        mScrollView.setScrollViewListener(this);

        if (Build.VERSION.SDK_INT >= 21) {
            mColorBox.setAlpha(1);
            if (mDrink != null) {
                refreshUI(mDrink);
            }
        } else if (savedInstanceState != null) {
            mColorBox.setAlpha(1);
            Drink drink = savedInstanceState.getParcelable(STATE_DRINK);
            if (drink != null) {
                refreshUI(drink);
            }
        } else {
            ViewTreeObserver observer = mImageView.getViewTreeObserver();
            if (observer != null) {
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                        runEnterAnimation();
                        return true;
                    }
                });
            }
        }

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.drink_detail, menu);
    }

    @OnClick(R.id.wikipedia)
    void goToWikipedia() {
        if (mDrink == null) {
            return;
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mDrink.wikipedia)));
    }

    private void runEnterAnimation() {

        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshUI(mDrink);
            }
        };
        mImageView.setTranslationY(-mImageView.getHeight());

        ViewPropertyAnimator animator = mImageView.animate().setDuration(ANIM_IMAGE_ENTER_DURATION).
                setStartDelay(ANIM_IMAGE_ENTER_STARTDELAY).
                translationY(0).
                setInterpolator(sDecelerator);

        Runnable animateColorBoxRunnable = new Runnable() {
            @Override
            public void run() {
                mColorBox.animate()
                        .alpha(1)
                        .setDuration(ANIM_COLORBOX_ENTER_DURATION)
                        .setInterpolator(sDecelerator);
            }
        };

        AnimUtils.scheduleStartAction(animator, refreshRunnable, ANIM_IMAGE_ENTER_STARTDELAY);
        AnimUtils.scheduleEndAction(animator, animateColorBoxRunnable, ANIM_IMAGE_ENTER_STARTDELAY, ANIM_IMAGE_ENTER_DURATION);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mDrink != null) {
            outState.putParcelable(STATE_DRINK, mDrink);
        }
    }

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        float alpha = 2 * (float) y / (float) mImageViewHeight;
        if (alpha > 1) {
            alpha = 1;
        } else if (alpha < 0) {
            alpha = 0;
        }
        mBlurredImageView.setAlpha(alpha);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
        params.setMargins(params.leftMargin, -y / 2, params.rightMargin, params.bottomMargin);
        mImageView.setLayoutParams(params);

        params = (RelativeLayout.LayoutParams) mBlurredImageView.getLayoutParams();
        params.setMargins(params.leftMargin, -y / 2, params.rightMargin, params.bottomMargin);
        mBlurredImageView.setLayoutParams(params);

        mToolbar.getBackground().setAlpha((int) (alpha * 255));
    }

    public void refreshUI(Drink drink) {
        mDrink = drink;

        if (getActivity() == null) {
            return;
        }

        mHistoryView.setText(drink.history);

        mIngredientsView.setText(Html.fromHtml(HtmlUtils.getIngredientsHtml(mDrink)));

        mInstructionsView.setText(drink.instructions);
        mWikipediaButton.setText(String.format(getString(R.string.drink_detail_wikipedia), drink.name));

        ViewTreeObserver observer = mScrollView.getViewTreeObserver();
        if (observer != null) {
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    mScrollView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mScrollView.setAlpha(0);
                    mScrollView.animate().setDuration(ANIM_TEXT_ENTER_DURATION).
                            alpha(1).
                            setInterpolator(sDecelerator);
                    // Fake a onScrollChangedCall to apply changes to mBlurredImageView and mImageView.
                    fakeOnScrollChanged();
                    return true;
                }
            });
        }
        mScrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityCompat.finishAfterTransition(getActivity());
                return true;
            case R.id.menu_item_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mDrink.name);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(HtmlUtils.getIngredientsHtml(mDrink)));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void fakeOnScrollChanged() {
        onScrollChanged(mScrollView, mScrollView.getScrollX(),
                mScrollView.getScrollY(), mScrollView.getScrollX(), mScrollView.getScrollY());
    }

    private class QuantizeBitmapTask extends AsyncTask<Bitmap, Void, ArrayList<Integer>> {

        @Override
        protected ArrayList<Integer> doInBackground(Bitmap... bitmaps) {

            Bitmap originalBitmap = bitmaps[0];

            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();

            Bitmap bitmap = Bitmap.createScaledBitmap(originalBitmap, originalWidth / 16, originalHeight / 16, true);

            ArrayList<Integer> quantizedColors = new ColorQuantizer().load(bitmap).quantize().getQuantizedColors();

            return quantizedColors;
        }

        @Override
        protected void onPostExecute(ArrayList<Integer> colors) {
            View[] colorViews = new View[]{mColorView1, mColorView2, mColorView3, mColorView4};
            for (int i = 0; i < colors.size() && i < 4; i++) {
                colorViews[i].setBackgroundColor(colors.get(i));
            }
        }
    }
}
