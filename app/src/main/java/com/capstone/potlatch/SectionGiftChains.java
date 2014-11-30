package com.capstone.potlatch;


import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.capstone.potlatch.base.BaseActivity;
import com.capstone.potlatch.base.Config;
import com.capstone.potlatch.base.Routes;
import com.capstone.potlatch.models.Gift;
import com.capstone.potlatch.models.GiftChain;
import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.net.requests.AuthRequest;
import com.capstone.potlatch.utils.AwareFragment;
import com.capstone.potlatch.utils.EndlessScrollListener;
import com.capstone.potlatch.utils.ViewHolder;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class SectionGiftChains extends Fragment implements AwareFragment.OnViewPagerFragmentSelected {
    private List<GiftChain> giftChains = new ArrayList<>();
    private boolean dataHasBeenLoaded = false;
    private int lastLoadedDataPage = 0;
    private String lastNameFilter;
    private ScrollListener scrollListener = new ScrollListener();

    // Members that must be cleaned in onDestroyView to avoid potential memory leaks, as this
    // Fragment is retained
    private static class UI {
        GiftChainAdapter adapter;
        SearchView searchView;
    }
    private UI ui;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.searchable_options, menu);
        // Setup the search action view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        ui.searchView = (SearchView) searchItem.getActionView();
        ui.searchView.setQueryHint(getActivity().getString(R.string.filter_by_name));

        SearchListener searchListener = new SearchListener();
        ui.searchView.setOnQueryTextListener(searchListener);
        MenuItemCompat.setOnActionExpandListener(searchItem, searchListener);

        if (lastNameFilter != null) {
            // This is very weird. If you call "setQuery" in this layout pass, it doesn't work. It
            // needs to be delayed until the following pass.
            ui.searchView.post(new Runnable() {
                @Override
                public void run() {
                    ui.searchView.setQuery(lastNameFilter, false);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ui = new UI();
        View v = inflater.inflate(R.layout.fragment_section_generic_list, container, false);

        ListView giftList = (ListView) v.findViewById(R.id.list);

        ui.adapter = new GiftChainAdapter(getActivity(), giftChains);
        giftList.setAdapter(ui.adapter);
        giftList.setOnScrollListener(scrollListener);

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
        refreshData();
    }

    @Override
    public void onStop() {
        Net.getQueue().cancelAll(this);
        super.onStop();
    }

    @Override
    public void onSelected() {
        refreshData();
    }

    private void refreshData() {
        if (!dataHasBeenLoaded) {
            loadPageData(0, null);
        }
    }

    private void loadPageData(int page, String titleFilter) {
        if (page == 0) {
            scrollListener.reset();
        }

        lastLoadedDataPage = page;
        lastNameFilter = titleFilter;

        String url = Routes.urlFor(Routes.GIFTS_CHAIN_PATH,
                Routes.PAGE_PARAMETER, page,
                Routes.LIMIT_PARAMETER, Config.pageSize,
                Routes.TITLE_PARAMETER, titleFilter);

        AuthRequest<List<GiftChain>> req = new AuthRequest<>(Request.Method.GET, url,
                                                             new TypeReference<List<GiftChain>>() {},
                                                             new RequestLoadPageSuccessListener(),
                                                             ((BaseActivity) getActivity()).getErrorListener(false));
        req.setTag(this);
        Net.addToQueue(req);
    }

    //////////////// Requests listeners ////////////////////

    class RequestLoadPageSuccessListener implements Response.Listener<List<GiftChain>> {
        @Override
        public void onResponse(List<GiftChain> response) {
            if (lastLoadedDataPage == 0) {
                giftChains.clear();
            }
            giftChains.addAll(response);
            dataHasBeenLoaded = true;
            ui.adapter.notifyDataSetChanged();
        }
    }

    //////////////// Other listeners ////////////////////

    class SearchListener implements SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            loadPageData(0, query);
            return false;
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
            loadPageData(lastLoadedDataPage + 1, lastNameFilter);
        }
    }


    //////////////// Adapters ////////////////////

    class GiftChainAdapter extends ArrayAdapter<GiftChain> {
        public GiftChainAdapter(Context context, List<GiftChain> giftChains) {
            super(context, 0, giftChains);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup v = (ViewGroup) convertView;

            if (v == null) {
                v = (ViewGroup) View.inflate(getContext(), R.layout.partial_gift_chain_row, null);
            }

            GiftChain giftChain = getItem(position);

            ((TextView) ViewHolder.get(v, R.id.gift_chain_name)).setText(giftChain.name);

            int maxGiftImages = 3;
            Resources res = getResources();
            String packageName = getContext().getPackageName();

            for (int i = 0; i < maxGiftImages; ++i) {
                try {
                    int giftImageId = res.getIdentifier("gift_image" + (i + 1), "id", packageName);

                    NetworkImageView imageView = ViewHolder.get(v, giftImageId);
                    imageView.setImageUrl(null, Net.getImgLoader());
                    imageView.setErrorImageResId(R.drawable.default_gift_image);

                    Gift g = giftChain.gifts.get(i);
                    if (g.imageUrlSmall != null) {
                        String imageUrl = Routes.urlFor(g.imageUrlSmall);
                        imageView.setImageUrl(imageUrl, Net.getImgLoader());
                    }
                } catch (IndexOutOfBoundsException ignored) {}
            }
            return v;
        }
    }
}
