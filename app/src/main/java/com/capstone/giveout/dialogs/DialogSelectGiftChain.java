package com.capstone.giveout.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.capstone.giveout.base.BaseActivity;
import com.capstone.giveout.base.Config;
import com.capstone.giveout.base.Routes;
import com.capstone.giveout.models.GiftChain;
import com.capstone.giveout.net.Net;
import com.capstone.giveout.net.requests.AuthRequest;
import com.capstone.giveout.utils.EndlessScrollListener;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;


/**
 * Activities that contain this fragment must implement the
 * {@link com.capstone.giveout.dialogs.DialogSelectGiftChain.OnDialogGiftChainSelectedListener} interface
 * to handle interaction events.
 */
public class DialogSelectGiftChain extends BaseRetainedDialog {

    private OnDialogGiftChainSelectedListener mListener;
    private int lastLoadedDataPage;
    private List<GiftChain> giftChains = new ArrayList<>();
    private boolean dataHasBeenLoaded;
    private ScrollListener scrollListener = new ScrollListener();

    // Members that must be cleaned in onDestroyView to avoid potential memory leaks, as this
    // Fragment is retained
    private static class UI {
        ArrayAdapter<GiftChain> adapter;
    }
    private UI ui;

    public static DialogSelectGiftChain open(FragmentManager fragmentManager, String tag) {
        DialogSelectGiftChain dialogConfirm = (DialogSelectGiftChain) fragmentManager.findFragmentByTag(tag);
        if (dialogConfirm == null) {
            dialogConfirm = new DialogSelectGiftChain();
            dialogConfirm.show(fragmentManager, tag);
        }
        return dialogConfirm;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ui = new UI();
        final ListView giftChainList = new ListView(getActivity());
        giftChainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onGiftChainSelected(DialogSelectGiftChain.this, getTag(), giftChains.get(position));
                mListener = null; // To avoid calling it again in onDismiss
                dismiss();
            }
        });

        ui.adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, giftChains);
        giftChainList.setAdapter(ui.adapter);
        giftChainList.setOnScrollListener(scrollListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(giftChainList)
               .setNegativeButton(android.R.string.no, null);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDialogGiftChainSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDialogGiftChainSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onGiftChainSelected(this, getTag(), null);
        }
    }

    @Override
    public void onStop() {
        Net.getQueue().cancelAll(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!dataHasBeenLoaded) {
            loadPageData(0);
        }
    }

    private void loadPageData(int page) {
        if (page == 0) {
            scrollListener.reset();
        }

        lastLoadedDataPage = page;

        String url = Routes.urlFor(Routes.GIFTS_CHAIN_PATH,
                Routes.PAGE_PARAMETER, page,
                Routes.LIMIT_PARAMETER, Config.pageSize);

        AuthRequest<List<GiftChain>> req = new AuthRequest<>(Request.Method.GET, url,
                new TypeReference<List<GiftChain>>() {},
                new RequestLoadPageSuccessListener(),
                ((BaseActivity) getActivity()).getErrorListener(false));
        req.setTag(this);
        Net.addToQueue(req);
    }

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


    class ScrollListener extends EndlessScrollListener {
        @Override
        public void loadNewPage() {
            loadPageData(lastLoadedDataPage + 1);
        }
    }

    public interface OnDialogGiftChainSelectedListener {
        public void onGiftChainSelected(BaseRetainedDialog dialogFragment, String tag, GiftChain giftChain);
    }

}
