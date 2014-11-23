package com.capstone.potlatch;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.capstone.potlatch.adapters.GiftsAdapter;
import com.capstone.potlatch.base.Config;
import com.capstone.potlatch.base.Routes;
import com.capstone.potlatch.base.State;
import com.capstone.potlatch.models.Gift;
import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.net.requests.AuthRequest;
import com.capstone.potlatch.utils.AwareFragment;
import com.capstone.potlatch.utils.EndlessScrollListener;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class SectionGifts extends Fragment implements AwareFragment.OnViewPagerFragmentSelected, AwareFragment.OnUserLogin {
    public static final String ARG_FOR_CURRENT_USER = "FOR_CURRENT_USER";

    private List<Gift> gifts = new ArrayList<Gift>();
    private boolean dataHasBeenLoaded = false;
    private int lastLoadedDataPage = 0;
    private String lastTitleFilter;
    private boolean forCurrentUser = false;
    private ScrollListener scrollListener;

    // Members that must be cleaned in onDestroyView to avoid potential memory leaks, as this
    // Fragment is retained
    private static class UI {
        GiftsAdapter adapter;
        SearchView searchView;
        Button signInButton;
    }
    private UI ui;


//    public static SectionGifts newInstance(boolean forCurrentUser) {
//        SectionGifts fragment = new SectionGifts();
//        Bundle args = new Bundle();
//        args.putBoolean(ARG_FOR_CURRENT_USER, forCurrentUser);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            forCurrentUser = getArguments().getBoolean(ARG_FOR_CURRENT_USER);
        }
        setHasOptionsMenu(true);
        setRetainInstance(true);

        scrollListener = new ScrollListener();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.searchable_options, menu);
        // Setup the search action view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        ui.searchView = (SearchView) searchItem.getActionView();
        ui.searchView.setQueryHint(getActivity().getString(R.string.filter_by_title));

        SearchListener searchListener = new SearchListener();
        ui.searchView.setOnQueryTextListener(searchListener);
        MenuItemCompat.setOnActionExpandListener(searchItem, searchListener);

        if (lastTitleFilter != null) {
            // This is very weird. If you call "setQuery" in this layout pass, it doesn't work. It
            // needs to be delayed until the following pass.
            ui.searchView.post(new Runnable() {
                @Override
                public void run() {
                    ui.searchView.setQuery(lastTitleFilter, false);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ui = new UI();
        View v = inflater.inflate(R.layout.fragment_section_gifts, container, false);
        ListView giftList = (ListView) v.findViewById(R.id.gift_list);
        ui.adapter = new GiftsAdapter(getActivity(), gifts);
        giftList.setAdapter(ui.adapter);
        giftList.setOnScrollListener(scrollListener);

        ui.signInButton = (Button) v.findViewById(R.id.sign_in_button);
        ui.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogLogin.show(getFragmentManager());
            }
        });
        return v;
    }

    @Override
    public void onDestroyView() {
        // As this fragment state is retained, this ensures there is no memory leak
        ui = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkUserAndLoadData();
    }

    @Override
    public void onStop() {
        Net.getQueue().cancelAll(this);
        super.onStop();
    }

    @Override
    public void onSelected() {
        checkUserAndLoadData();
    }

    @Override
    public void onLoginSuccess() {
        checkUserAndLoadData();
    }

    @Override
    public void onLoginCanceled() {
        checkUserAndLoadData();
    }

    private void checkUserAndLoadData() {
        if (forCurrentUser && ! State.get().isUserLoggedIn()) {
            ui.signInButton.setVisibility(View.VISIBLE);
            return;
        }
        ui.signInButton.setVisibility(View.GONE);
        if (!dataHasBeenLoaded) {
            loadPageData(0, null);
        }
    }

    private void loadPageData(int page, String titleFilter) {
        if (page == 0) {
            scrollListener.reset();
        }

        lastLoadedDataPage = page;
        lastTitleFilter = titleFilter;

        String basePath = forCurrentUser ? Routes.MY_GIFTS_PATH
                                         : Routes.GIFTS_PATH;

        String url = Routes.urlFor(basePath,
                                   Routes.PAGE_PARAMETER, page,
                                   Routes.LIMIT_PARAMETER, Config.pageSize,
                                   Routes.TITLE_PARAMETER, titleFilter);

        AuthRequest<List<Gift>> req = new AuthRequest<List<Gift>>(Request.Method.GET, url,
                                                                  new TypeReference<List<Gift>>() {},
                                                                  new RequestSuccessListener(),
                                                                  new RequestErrorListener());
        req.setTag(this);
        Net.addToQueue(req);
        System.out.println("Loaging page nº " + lastLoadedDataPage + " with title filter: " + String.valueOf(lastTitleFilter));
    }

    class RequestSuccessListener implements Response.Listener<List<Gift>> {
        @Override
        public void onResponse(List<Gift> response) {
            if (lastLoadedDataPage == 0) {
                gifts.clear();
            }
            gifts.addAll(response);
            dataHasBeenLoaded = true;
            ui.adapter.notifyDataSetChanged();
        }
    }

    class RequestErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    }

    class SearchListener implements SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            loadPageData(0, query);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            loadPageData(0, null);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return true;
        }

    }

    class ScrollListener extends EndlessScrollListener {
        @Override
        public void loadNewPage() {
            loadPageData(lastLoadedDataPage + 1, lastTitleFilter);
        }
    }
}
