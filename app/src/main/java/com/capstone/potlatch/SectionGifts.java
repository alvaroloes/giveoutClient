package com.capstone.potlatch;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.capstone.potlatch.adapters.GiftsAdapter;
import com.capstone.potlatch.base.Routes;
import com.capstone.potlatch.models.Gift;
import com.capstone.potlatch.net.JacksonRequest;
import com.capstone.potlatch.net.Net;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class SectionGifts extends Fragment {
    private List<Gift> gifts = new ArrayList<Gift>();
    private boolean dataHasBeenLoaded = false;

    // UI members
    private GiftsAdapter mAdapter;

    public static SectionGifts newInstance() {
        SectionGifts fragment = new SectionGifts();
/*        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView v = (ListView) inflater.inflate(R.layout.fragment_section_gifts, container, false);
        mAdapter = new GiftsAdapter(getActivity(), gifts);
        v.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!dataHasBeenLoaded) {
            loadPageData();
        }
    }

    private void loadPageData() {
        String url = Routes.urlFor(Routes.GIFTS_PATH);
        JacksonRequest<List<Gift>> req = new JacksonRequest<List<Gift>>(url,
                                                                        new TypeReference<List<Gift>>() {},
                                                                        new RequestSuccessListener(),
                                                                        new RequestErrorListener());
        Net.addToQueue(req);
    }

    class RequestSuccessListener implements Response.Listener<List<Gift>> {
        @Override
        public void onResponse(List<Gift> response) {
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
}
