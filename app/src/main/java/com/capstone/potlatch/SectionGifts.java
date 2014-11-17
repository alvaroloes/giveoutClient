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
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.capstone.potlatch.adapters.GiftsAdapter;
import com.capstone.potlatch.base.Config;
import com.capstone.potlatch.base.Routes;
import com.capstone.potlatch.models.Gift;
import com.capstone.potlatch.net.JacksonRequest;
import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.utils.EndlessScrollListener;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class SectionGifts extends Fragment {
    private List<Gift> gifts = new ArrayList<Gift>();
    private boolean dataHasBeenLoaded = false;
    private int lastLoadedDataPage = 0;
    private String lastTitleFilter;

    // Non stateful members
    private GiftsAdapter mAdapter;
    private SearchView mSearchView;
    private ScrollListener mScrollListener;

    public static SectionGifts newInstance() {
        SectionGifts fragment = new SectionGifts();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.searchable_options, menu);
        // Setup the search action view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(getActivity().getString(R.string.filter_by_title));

        SearchListener searchListener = new SearchListener();
        mSearchView.setOnQueryTextListener(searchListener);
        MenuItemCompat.setOnActionExpandListener(searchItem, searchListener);

        if (lastTitleFilter != null) {
            // This is very weird. If you call "setQuery" in this layout pass, it doesn't work. It
            // needs to be delayed until the following pass.
            mSearchView.post(new Runnable() {
                @Override
                public void run() {
                    mSearchView.setQuery(lastTitleFilter, false);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView v = (ListView) inflater.inflate(R.layout.fragment_section_gifts, container, false);
        mScrollListener = new ScrollListener();
        v.setOnScrollListener(mScrollListener);
        mAdapter = new GiftsAdapter(getActivity(), gifts);
        v.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!dataHasBeenLoaded) {
            loadPageData(0, null);
        }
    }

    private void loadPageData(int page, String titleFilter) {
        if (page == 0) {
            mScrollListener.reset();
        }

        lastLoadedDataPage = page;
        lastTitleFilter = titleFilter;
        String url = Routes.urlFor(Routes.GIFTS_PATH,
                                   Routes.PAGE_PARAMETER, page,
                                   Routes.LIMIT_PARAMETER, Config.pageSize,
                                   Routes.TITLE_PARAMETER, titleFilter);

        JacksonRequest<List<Gift>> req = new JacksonRequest<List<Gift>>(url,
                                                                        new TypeReference<List<Gift>>() {},
                                                                        new RequestSuccessListener(),
                                                                        new RequestErrorListener());
        Net.addToQueue(req);
        System.out.println("Loaging page nº " + lastLoadedDataPage + " with title filter: " + String.valueOf(lastTitleFilter));
        //TODO: no mostrar los gifts no publicados
    }

    class RequestSuccessListener implements Response.Listener<List<Gift>> {
        @Override
        public void onResponse(List<Gift> response) {
            if (lastLoadedDataPage == 0) {
                gifts.clear();
            }
            gifts.addAll(response);
            dataHasBeenLoaded = true;
            mAdapter.notifyDataSetChanged();
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
        public boolean onQueryTextChange(String newText) {
            return false;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            loadPageData(0, null);
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
