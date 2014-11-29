package com.capstone.potlatch;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.capstone.potlatch.base.BaseActivity;
import com.capstone.potlatch.base.Config;
import com.capstone.potlatch.base.Routes;
import com.capstone.potlatch.models.User;
import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.net.requests.AuthRequest;
import com.capstone.potlatch.utils.AwareFragment;
import com.capstone.potlatch.utils.EndlessScrollListener;
import com.capstone.potlatch.utils.ViewHolder;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class SectionTopGivers extends Fragment implements AwareFragment.OnViewPagerFragmentSelected {
    private List<User> users = new ArrayList<>();
    private boolean dataHasBeenLoaded = false;
    private int lastLoadedDataPage = 0;
    private String lastOrderKind;
    private ScrollListener scrollListener = new ScrollListener();

    // Members that must be cleaned in onDestroyView to avoid potential memory leaks, as this
    // Fragment is retained
    private static class UI {
        UserAdapter adapter;
    }
    private UI ui;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_section_top_givers, menu);

        MenuItem orderByCount = menu.findItem(R.id.action_top_by_gift_count);
        MenuItem orderByTouches = menu.findItem(R.id.action_top_by_gift_touches);

        orderByCount.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                loadPageData(0, Routes.TOP_KIND_GIFT_COUNT);
                return true;
            }
        });

        orderByTouches.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                loadPageData(0, Routes.TOP_KIND_GIFT_TOUCHES);
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ui = new UI();
        View v = inflater.inflate(R.layout.fragment_section_generic_list, container, false);

        ListView giftList = (ListView) v.findViewById(R.id.list);

        ui.adapter = new UserAdapter(getActivity(), users);
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

    private void loadPageData(int page, String orderKind) {
        if (page == 0) {
            scrollListener.reset();
        }

        lastLoadedDataPage = page;
        lastOrderKind = orderKind;

        String url = Routes.urlFor(Routes.TOP_GIVERS_PATH,
                Routes.PAGE_PARAMETER, page,
                Routes.LIMIT_PARAMETER, Config.pageSize,
                Routes.TOP_KIND_PARAMETER, orderKind);

        AuthRequest<List<User>> req = new AuthRequest<>(Request.Method.GET, url,
                                                             new TypeReference<List<User>>() {},
                                                             new RequestLoadPageSuccessListener(),
                                                             ((BaseActivity) getActivity()).getErrorListener(false));
        req.setTag(this);
        Net.addToQueue(req);
    }

    //////////////// Requests listeners ////////////////////

    class RequestLoadPageSuccessListener implements Response.Listener<List<User>> {
        @Override
        public void onResponse(List<User> response) {
            if (lastLoadedDataPage == 0) {
                users.clear();
            }
            users.addAll(response);
            dataHasBeenLoaded = true;
            ui.adapter.notifyDataSetChanged();
        }
    }

    //////////////// Listeners ////////////////////

    class ScrollListener extends EndlessScrollListener {
        @Override
        public void loadNewPage() {
            loadPageData(lastLoadedDataPage + 1, lastOrderKind);
        }
    }


    //////////////// Adapters ////////////////////

    class UserAdapter extends ArrayAdapter<User> {
        public UserAdapter(Context context, List<User> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup v = (ViewGroup) convertView;

            if (v == null) {
                v = (ViewGroup) View.inflate(getContext(), R.layout.partial_top_user_row, null);
            }

            User user = getItem(position);

            ((TextView) ViewHolder.get(v, R.id.user_name)).setText(user.username);

            String countString = getResources().getString(R.string.total_touches, user.giftTouches);
            if (Routes.TOP_KIND_GIFT_COUNT.equals(lastOrderKind)) {
                countString = getResources().getString(R.string.total_gift_count, user.giftCount);
            }
            ((TextView) ViewHolder.get(v, R.id.user_kind_count)).setText(countString);

            NetworkImageView imageView = ViewHolder.get(v, R.id.user_image);
            imageView.setImageUrl(null, Net.getImgLoader());
            imageView.setDefaultImageResId(R.drawable.default_user_image);
            if (user.imageUrlMedium != null) {
                String imageUrl = Routes.urlFor(user.imageUrlMedium);
                imageView.setImageUrl(imageUrl, Net.getImgLoader());
            }

            return v;
        }
    }
}
