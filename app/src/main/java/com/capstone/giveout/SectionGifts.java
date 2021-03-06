package com.capstone.giveout;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.capstone.giveout.adapters.GiftsAdapter;
import com.capstone.giveout.base.BaseActivity;
import com.capstone.giveout.base.Config;
import com.capstone.giveout.base.Routes;
import com.capstone.giveout.base.State;
import com.capstone.giveout.dialogs.BaseRetainedDialog;
import com.capstone.giveout.dialogs.DialogConfirm;
import com.capstone.giveout.dialogs.DialogLogin;
import com.capstone.giveout.models.Gift;
import com.capstone.giveout.models.User;
import com.capstone.giveout.net.Net;
import com.capstone.giveout.net.requests.AuthRequest;
import com.capstone.giveout.utils.AwareFragment;
import com.capstone.giveout.utils.Copier;
import com.capstone.giveout.utils.EndlessScrollListener;
import com.capstone.giveout.utils.SyncManager;
import com.capstone.giveout.utils.ViewHolder;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class SectionGifts extends Fragment implements AwareFragment.OnViewPagerFragmentSelected,
                                                      AwareFragment.OnUserLogin,
                                                      AwareFragment.OnDialogConfirmation {
    public static final String ARG_FOR_CURRENT_USER = "FOR_CURRENT_USER";

    private static final String TAG_ACTION_LOGIN = "SectionGifts - TAG_ACTION_LOGIN";
    private static final String TAG_ACTION_INAPPROPRIATE = "SectionGifts - TAG_ACTION_INAPPROPRIATE";
    private static final String TAG_ACTION_DELETE = "SectionGifts - TAG_ACTION_DELETE";
    private static final String TAG_ACTION_TOUCH = "SectionGifts - TAG_ACTION_TOUCH";

    private List<Gift> gifts = new ArrayList<Gift>();
    private boolean dataHasBeenLoaded = false;
    private boolean dataIsReloading = false;
    private int lastLoadedDataPage = 0;
    private String lastTitleFilter;
    private boolean forCurrentUser = false;
    private ScrollListener scrollListener = new ScrollListener();
    private Copier copier = new Copier();
    private BroadcastReceiver updateDataReceiver;
    private BroadcastReceiver reloadDataReceiver;

    // Members that must be cleaned in onDestroyView to avoid potential memory leaks, as this
    // Fragment is retained
    private static class UI {
        ListView giftList;
        GiftsAdapter adapter;
        SearchView searchView;
        Button signInButton;
    }
    private UI ui;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            forCurrentUser = getArguments().getBoolean(ARG_FOR_CURRENT_USER);
        }
        setHasOptionsMenu(true);
        setRetainInstance(true);

        scrollListener = new ScrollListener();

        reloadDataReceiver = new ReloadDataBroadcastReceiver();

        getActivity().registerReceiver(reloadDataReceiver, new IntentFilter(SyncManager.RELOAD_DATA_ACTION));
    }

    @Override
    public void onDestroy() {
        if (reloadDataReceiver != null) {
            getActivity().unregisterReceiver(reloadDataReceiver);
        }
        super.onDestroy();
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

        ui.giftList = (ListView) v.findViewById(R.id.gift_list);

        ui.adapter = forCurrentUser ? new UserGiftsAdapter(getActivity(), gifts)
                                    : new PublicGiftsAdapter(getActivity(), gifts);
        ui.giftList.setAdapter(ui.adapter);
        ui.giftList.setOnScrollListener(scrollListener);

        ui.signInButton = (Button) v.findViewById(R.id.sign_in_button);
        ui.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogLogin.open(getFragmentManager(), TAG_ACTION_LOGIN);
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
        reloadData();
    }

    @Override
    public void onStart() {
        super.onStart();

        updateDataReceiver = new UpdateDataBroadcastReceiver();

        getActivity().registerReceiver(updateDataReceiver, new IntentFilter(SyncManager.UPDATE_DATA_ACTION));
    }

    @Override
    public void onStop() {
        Net.getQueue().cancelAll(this);
        if (updateDataReceiver != null) {
            getActivity().unregisterReceiver(updateDataReceiver);
        }
        super.onStop();
    }

    @Override
    public void onSelected() {
        reloadData();
    }

    @Override
    public void onLogin(BaseRetainedDialog dialogFragment, String tag, boolean success) {
        ui.adapter.notifyDataSetChanged();
        if (!success)  {
            return;
        }

        switch(tag) {
            case TAG_ACTION_LOGIN:
                SyncManager.sendBroadcast(getActivity(), SyncManager.RELOAD_DATA_ACTION);
                break;
            case TAG_ACTION_INAPPROPRIATE:
            case TAG_ACTION_DELETE:
            case TAG_ACTION_TOUCH:
                doAction(dialogFragment.<Gift>getData(), tag);
                break;

        }
    }

    @Override
    public void onConfirmation(BaseRetainedDialog dialogFragment, String tag, boolean confirmed) {
        if (! confirmed) {
            return;
        }

        switch (tag) {
            case TAG_ACTION_INAPPROPRIATE:
            case TAG_ACTION_DELETE:
                if (State.get().isUserLoggedIn()) {
                    doAction(dialogFragment.<Gift>getData(), tag);
                    return;
                }
                DialogLogin.open(getFragmentManager(), tag)
                           .setData(dialogFragment.getData());
                break;
        }
    }

    private void doAction(Gift gift, String tag) {
        switch (tag) {
            case TAG_ACTION_INAPPROPRIATE:
            case TAG_ACTION_TOUCH:
                flagOrTouch(gift, false);
                break;
            case TAG_ACTION_DELETE:
                delete(gift);
                break;
        }
    }

    private void reloadData() {
        if (forCurrentUser && ! State.get().isUserLoggedIn()) {
            ui.signInButton.setVisibility(View.VISIBLE);
            return;
        }
        ui.signInButton.setVisibility(View.GONE);
        if (!dataHasBeenLoaded) {
            loadPageData(0, lastTitleFilter);
        }
        ui.adapter.notifyDataSetChanged();
    }

    private void loadPageData(int page, String titleFilter) {
        loadData(page, titleFilter, false);
    }

    private void refreshCurrentData() {
        loadData(0, lastTitleFilter, true);
    }

    private void loadData(int page, String titleFilter, boolean refreshCurrentDataOnly) {
        if (page == 0) {
            scrollListener.reset();
        }

        int pageSize = Config.pageSize;
        if (refreshCurrentDataOnly) {
            dataIsReloading = true;
            pageSize = gifts.size();
            ui.giftList.setOnScrollListener(null);
        } else {
            lastLoadedDataPage = page;
            lastTitleFilter = titleFilter;
        }

        String basePath = forCurrentUser ? Routes.MY_GIFTS_PATH
                                         : Routes.GIFTS_PATH;
        Long userId = State.get().isUserLoggedIn() ? State.get().getUser().id
                                                   : null;

        String url = Routes.urlFor(basePath,
                                   Routes.PAGE_PARAMETER, page,
                                   Routes.LIMIT_PARAMETER, pageSize,
                                   Routes.TITLE_PARAMETER, titleFilter,
                                   Routes.NOT_FLAGGED_BY_USER_ID_PARAMETER, Config.noInappropriateGifts ? userId : null);

        AuthRequest<List<Gift>> req = new AuthRequest<>(Request.Method.GET, url,
                                                                  new TypeReference<List<Gift>>() {},
                                                                  new RequestLoadPageSuccessListener(),
                                                                  ((BaseActivity) getActivity()).getErrorListener(false));
        req.setTag(this);
        Net.addToQueue(req);
        System.out.println("Loaging page nº " + page + " with title filter: " + String.valueOf(titleFilter));
    }



    private void flagOrTouch(Gift gift, boolean touch) {
        User user = State.get().getUser();
        String basePath = touch ? Routes.GIFTS_TOUCH_PATH
                                : Routes.GIFTS_INAPPROPRIATE_PATH;

        String url;
        if ((touch && gift.touchedBy(user)) ||
            (!touch && gift.inappropriateBy(user))) {
            url = Routes.urlFor(basePath,
                                "id", gift.id,
                                Routes.REGRET_PARAMETER, true);
        } else {
            url = Routes.urlFor(basePath, "id", gift.id);
        }

        AuthRequest<Gift> req = new AuthRequest<>(Request.Method.PUT, url, Gift.class,
                                                      new RequestUpdateOrDeleteSuccessListener(gift),
                                                      ((BaseActivity) getActivity()).getErrorListener(true));
        req.setTag(this);
        Net.addToQueue(req);
    }

    private void delete(Gift gift) {
        String url = Routes.urlFor(Routes.GIFTS_ID_PATH, "id", gift.id);
        AuthRequest<Gift> req = new AuthRequest<>(Request.Method.DELETE, url, Gift.class,
                new RequestUpdateOrDeleteSuccessListener(gift),
                ((BaseActivity) getActivity()).getErrorListener(true));
        req.setTag(this);
        Net.addToQueue(req);
    }

    //////////////// Requests listeners ////////////////////

    class RequestLoadPageSuccessListener implements Response.Listener<List<Gift>> {
        @Override
        public void onResponse(List<Gift> response) {
            if (lastLoadedDataPage == 0 || dataIsReloading) {
                gifts.clear();
                dataIsReloading = false;
                ui.giftList.setOnScrollListener(scrollListener);
            }
            gifts.addAll(response);
            dataHasBeenLoaded = true;
            ui.adapter.notifyDataSetChanged();
        }
    }

    class RequestUpdateOrDeleteSuccessListener implements Response.Listener<Gift> {
        private Gift originalGift;

        RequestUpdateOrDeleteSuccessListener(Gift originalGift) {
            this.originalGift = originalGift;
        }

        @Override
        public void onResponse(Gift response) {
            if (response == null) { // The gift was deleted
                ui.adapter.remove(originalGift);
            } else {
                copier.copyProperties(originalGift, response);
            }
            SyncManager.sendBroadcast(getActivity(), SyncManager.RELOAD_DATA_ACTION);
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
            loadPageData(lastLoadedDataPage + 1, lastTitleFilter);
        }
    }

    class OnGiftTouchedMeListener implements View.OnClickListener {
        private Gift gift;

        OnGiftTouchedMeListener(Gift gift) {
            this.gift = gift;
        }

        @Override
        public void onClick(View v) {
            if (State.get().isUserLoggedIn()) {
                flagOrTouch(gift, true);
                return;
            }
            DialogLogin.open(getFragmentManager(), TAG_ACTION_TOUCH)
                       .setData(gift);
        }
    }

    class OnGiftEditListener implements View.OnClickListener {
        private Gift gift;

        OnGiftEditListener(Gift gift) {
            this.gift = gift;
        }

        @Override
        public void onClick(View v) {
            //todo
        }
    }

    class OnGiftInappropriateListener implements View.OnClickListener {
        private Gift gift;

        OnGiftInappropriateListener(Gift gift) {
            this.gift = gift;
        }

        @Override
        public void onClick(View v) {
            String text = getResources().getString(R.string.confirm_flat_inappropriate);
            if (gift.inappropriateBy(State.get().getUser())) {
                text = getResources().getString(R.string.confirm_unflag_inappropriate);
            }
            DialogConfirm.open(getFragmentManager(), TAG_ACTION_INAPPROPRIATE, text)
                         .setData(gift);
        }
    }

    class OnGiftDeleteListener implements View.OnClickListener {
        private Gift gift;

        OnGiftDeleteListener(Gift gift) {
            this.gift = gift;
        }

        @Override
        public void onClick(View v) {
            DialogConfirm.open(getFragmentManager(), TAG_ACTION_DELETE,
                               getResources().getString(R.string.confirm_delete_gift))
                         .setData(gift);
        }
    }

    //////////////// Adapters ////////////////////

    class PublicGiftsAdapter extends GiftsAdapter {
        public PublicGiftsAdapter(Context context, List<Gift> gifts) {
            super(context, gifts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            Gift gift = getItem(position);

            ViewHolder.get(v, R.id.gift_edit).setVisibility(View.GONE);
            ViewHolder.get(v, R.id.gift_delete).setVisibility(View.GONE);

            View touchButton = ViewHolder.get(v, R.id.gift_touch_button);
            View inappButton = ViewHolder.get(v, R.id.gift_inappropriate_button);

            touchButton.setOnClickListener(new OnGiftTouchedMeListener(gift));
            inappButton.setOnClickListener(new OnGiftInappropriateListener(gift));

            User user = State.get().getUser();
            touchButton.setSelected(gift.touchedBy(user));
            inappButton.setSelected(gift.inappropriateBy(user));

            return v;
        }
    }

    class UserGiftsAdapter extends GiftsAdapter {
        public UserGiftsAdapter(Context context, List<Gift> gifts) {
            super(context, gifts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            ViewHolder.get(v, R.id.gift_touch_button).setVisibility(View.GONE);
            ViewHolder.get(v, R.id.gift_inappropriate_button).setVisibility(View.GONE);

            ViewHolder.get(v, R.id.gift_edit).setOnClickListener(new OnGiftEditListener(getItem(position)));
            ViewHolder.get(v, R.id.gift_delete).setOnClickListener(new OnGiftDeleteListener(getItem(position)));

            return v;
        }
    }

    public class UpdateDataBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshCurrentData();
        }
    }

    public class ReloadDataBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            dataHasBeenLoaded = false;
            if (isResumed()) {
                reloadData();
            }
        }
    }
}
