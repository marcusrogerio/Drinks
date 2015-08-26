package fr.masciulli.drinks.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.masciulli.drinks.R;
import fr.masciulli.drinks.activity.DrinkDetailActivity;
import fr.masciulli.drinks.activity.MainActivity;
import fr.masciulli.drinks.adapter.DrinksListAdapter;
import fr.masciulli.drinks.data.DrinksProvider;
import fr.masciulli.drinks.model.Drink;
import fr.masciulli.drinks.view.ViewPagerScrollListener;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.List;

public class DrinksListFragment extends Fragment implements Callback<List<Drink>>, ViewPagerScrollListener, SearchView.OnQueryTextListener {
    private static final String STATE_LIST = "drinks_list";
    private static final String PREF_DRINKS_JSON = "drinks_json";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyView;

    private DrinksListAdapter adapter;

    private boolean loadingError = false;

    public static DrinksListFragment newInstance() {
        return new DrinksListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View root = inflater.inflate(R.layout.fragment_drinks_list, container, false);

        recyclerView = (RecyclerView) root.findViewById(R.id.list);
        progressBar = (ProgressBar) root.findViewById(R.id.progressbar);
        emptyView = root.findViewById(android.R.id.empty);

        int columnCount = getResources().getInteger(R.integer.grid_column_count);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));

        adapter = new DrinksListAdapter();
        adapter.setOnItemClickListener(new DrinksListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                openDrinkDetail(position);
            }
        });
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_LIST)) {
                List<Drink> savedDrinks = savedInstanceState.getParcelableArrayList(STATE_LIST);
                updateList(savedDrinks);
            } else {
                refresh();
            }
        } else {
            refresh();
        }

        return root;
    }

    private void updateList(List<Drink> drinks) {
        adapter.update(drinks);
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openDrinkDetail(int position) {
        Drink drink = adapter.getDrinks().get(position);

        Intent intent = new Intent(getActivity(), DrinkDetailActivity.class);
        intent.putExtra(DrinkDetailActivity.ARG_DRINK, drink);
        startActivity(intent);
    }

    @Override
    public void success(List<Drink> drinks, Response response) {
        loadingError = false;

        if (getActivity() == null) {
            return;
        }

        updateList(drinks);
        progressBar.setVisibility(View.GONE);

        Gson gson = new Gson();
        String json = gson.toJson(drinks);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(PREF_DRINKS_JSON, json);
        editor.apply();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        loadingError = true;

        if (getActivity() == null) {
            return;
        }

        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).setRefreshActionVisible(true);

        View view = getView();
        if (view != null) {
            if (retrofitError.getKind() == RetrofitError.Kind.NETWORK) {
                Snackbar.make(view, R.string.network_error, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(view, R.string.list_loading_failed, Snackbar.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onScroll(int position, float positionOffset, int positionOffsetPixels) {
        GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();

        int first = manager.findFirstVisibleItemPosition();
        int last = manager.findLastVisibleItemPosition();

        for (int i = 0; i <= last - first; i++) {
            View itemRoot = recyclerView.getChildAt(i);
            if (itemRoot == null) {
                continue;
            }

            TextView nameView = (TextView) itemRoot.findViewById(R.id.name);

            // TODO get screenWidth somewhere else (always the same)
            int screenWidth = ((ViewGroup) nameView.getParent()).getWidth();
            int textWidth = nameView.getWidth();

            nameView.setRight(Math.round(screenWidth + positionOffset * textWidth));
            nameView.setLeft(Math.round(screenWidth - textWidth + positionOffset * textWidth));
        }

    }

    private void refresh() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        ((MainActivity) getActivity()).setRefreshActionVisible(false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.contains(PREF_DRINKS_JSON)) {
            Gson gson = new Gson();
            //TODO async
            List<Drink> drinks = gson.fromJson(preferences.getString(PREF_DRINKS_JSON, "null"), new TypeToken<List<Drink>>() {
            }.getType());
            updateList(drinks);
            progressBar.setVisibility(View.GONE);
        }
        DrinksProvider.getAllDrinks(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.drinks_list, menu);

        // SearchView configuration
        final MenuItem searchMenuItem = menu.findItem(R.id.search);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                actionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                actionBar.setIcon(getResources().getDrawable(R.drawable.ic_main));
                return true;
            }
        });

        if (loadingError) {
            ((MainActivity) getActivity()).setRefreshActionVisible(true);
        }

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (recyclerView != null) {
            adapter.getFilter().filter(s);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (adapter.getItemCount() > 0) {
            outState.putParcelableArrayList(STATE_LIST, adapter.getDrinks());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.retry:
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
