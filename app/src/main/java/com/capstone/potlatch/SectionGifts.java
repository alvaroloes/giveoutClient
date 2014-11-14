package com.capstone.potlatch;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.capstone.potlatch.models.Gift;
import com.capstone.potlatch.net.JacksonRequest;
import com.capstone.potlatch.net.Net;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class SectionGifts extends Fragment {

    public static SectionGifts newInstance() {
        SectionGifts fragment = new SectionGifts();
//        Bundle args = new Bundle();
//        args.putString(ARG_TEXT, text);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_section_gifts, container, false);

        String url = "https://10.0.2.2:8443/gifts"; // This is the equivalent to localhost

        JacksonRequest<List<Gift>> req = new JacksonRequest<List<Gift>>(url, new TypeReference<List<Gift>>() {}, new Response.Listener<List<Gift>>() {
            @Override
            public void onResponse(List<Gift> response) {
                System.out.println("SUCCESS");
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("ERROR");
                error.printStackTrace();
            }
        });

        Net.addToQueue(req);
        return v;
    }


}
