package com.capstone.giveout.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.capstone.giveout.R;
import com.capstone.giveout.base.Routes;
import com.capstone.giveout.models.Gift;
import com.capstone.giveout.net.Net;
import com.capstone.giveout.utils.ViewHolder;

import java.util.List;

/**
 * Created by alvaro on 16/11/14.
 */
public class GiftsAdapter extends ArrayAdapter<Gift> {
    public GiftsAdapter(Context context, List<Gift> gifts) {
        super(context, 0, gifts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup v = (ViewGroup) convertView;

        if (v == null) {
            v = (ViewGroup) View.inflate(getContext(), R.layout.partial_gift_row, null);
        }

        Gift g = getItem(position);

        ((TextView) ViewHolder.get(v, R.id.gift_title)).setText(g.title);
        ((TextView) ViewHolder.get(v, R.id.gift_description)).setText(g.description);

        String touchesString = getContext().getString(R.string.gift_touches_number, g.touchedByUserIds.size());
        ((Button) ViewHolder.get(v, R.id.gift_touch_button)).setText(touchesString);

        TextView giftChainTextView = ViewHolder.get(v, R.id.gift_chain_name);
        giftChainTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        String giftChainName = getContext().getString(R.string.gift_unpublished);
        if (g.giftChain != null) {
            giftChainName = g.giftChain.name;
        } else {
            giftChainTextView.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        }
        giftChainTextView.setText(giftChainName);

        NetworkImageView imageView = ViewHolder.get(v, R.id.gift_image);
        imageView.setImageUrl(null, Net.getImgLoader());
        imageView.setErrorImageResId(R.drawable.default_gift_image);

        if (g.imageUrlMedium != null) {
            String imageUrl = Routes.urlFor(g.imageUrlMedium);
            imageView.setImageUrl(imageUrl, Net.getImgLoader());
        }

        return v;
    }
}