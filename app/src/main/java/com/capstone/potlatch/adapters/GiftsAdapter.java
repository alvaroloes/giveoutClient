package com.capstone.potlatch.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.capstone.potlatch.R;
import com.capstone.potlatch.models.Gift;
import com.capstone.potlatch.utils.ViewHolder;

import org.apache.commons.lang3.StringUtils;

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
        String giftChainName = g.giftChainName;
        if (StringUtils.isBlank(giftChainName)) {
            giftChainName = getContext().getString(R.string.gift_unpublished);
        }

        ((TextView) ViewHolder.get(v, R.id.gift_title)).setText(g.title);
        ((TextView) ViewHolder.get(v, R.id.gift_chain_name)).setText(giftChainName);
        ((TextView) ViewHolder.get(v, R.id.gift_description)).setText(g.description);
        ((TextView) ViewHolder.get(v, R.id.gift_touches_count)).setText(String.valueOf(g.touchedByUserIds.size()));

        return v;
    }
}