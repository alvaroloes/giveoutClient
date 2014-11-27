package com.capstone.potlatch;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.capstone.potlatch.base.BaseActivity;
import com.capstone.potlatch.base.Routes;
import com.capstone.potlatch.base.State;
import com.capstone.potlatch.dialogs.BaseRetainedDialog;
import com.capstone.potlatch.dialogs.DialogLogin;
import com.capstone.potlatch.models.Gift;
import com.capstone.potlatch.models.GiftChain;
import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.net.requests.AuthMultiPartRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;


public class ActivityCreateUpdateGift extends BaseActivity implements DialogLogin.OnLoginListener {
    private static final String TAG_LOGIN = "ActivityCreateGift - TAG_LOGIN";

    private List<GiftChain> giftChains;
    private TextView mGiftChain;
    private Button mSelectGiftChainButton;
    private TextView mGiftTitle;
    private TextView mGiftDescription;
    private NetworkImageView mGiftImage;
    private CheckBox mNoGiftChainCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gift);
        if (! State.get().isUserLoggedIn()) {
            DialogLogin.open(getFragmentManager(), TAG_LOGIN);
        }

        mGiftTitle = (TextView) findViewById(R.id.gift_title);
        mGiftImage = (NetworkImageView) findViewById(R.id.gift_image);
        mGiftDescription = (TextView) findViewById(R.id.gift_description);
        mGiftChain = (TextView) findViewById(R.id.gift_chain);
        mSelectGiftChainButton = (Button) findViewById(R.id.select_gift_chain_button);

        mNoGiftChainCheck = (CheckBox) findViewById(R.id.no_gift_chain_check);
        mNoGiftChainCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGiftChain.setEnabled(!isChecked);
                mSelectGiftChainButton.setEnabled(!isChecked);
            }
        });

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataAndSendGift();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (giftChains == null) {
//            String url = Routes.urlFor(Routes.GIFTS_CHAIN_PATH);
//            AuthRequest<List<GiftChain>> req = new AuthRequest<>(Request.Method.GET, url,
//                    new TypeReference<List<GiftChain>>() {},
//                    new GiftChainRequestListener(),
//                    getErrorListener(true));
//            req.setTag(this);
//            Net.addToQueue(req);
//        }

    }

    @Override
    protected void onStop() {
        Net.getQueue().cancelAll(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_create_gift, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginFinish(BaseRetainedDialog dialogFragment, String tag, boolean success) {
        if (!success) {
            finish();
        }
    }

    private void getDataAndSendGift() {
        boolean error = false;
        String giftTitle = String.valueOf(mGiftTitle.getText());
        String giftDescription = String.valueOf(mGiftDescription.getText());
        String giftChainName = String.valueOf(mGiftChain.getText());
        Long giftChainId = (Long) mGiftChain.getTag();
        boolean thereMustBeAGiftChain = !mNoGiftChainCheck.isChecked();

        // Check for errors

        if (TextUtils.isEmpty(giftTitle)) {
            mGiftTitle.setError("Please insert a Gift title");
            error = true;
        }

        if (TextUtils.isEmpty(giftDescription)) {
            mGiftDescription.setError("Please insert a Gift description");
            error = true;
        }

        if (thereMustBeAGiftChain && TextUtils.isEmpty(giftChainName)) {
            mGiftChain.setError("Please insert or select a Gift chain");
            error = true;
        }

        if (error) {
            return;
        }

        // Create the gift chain

        GiftChain giftChain = null;
        if (thereMustBeAGiftChain) {
            giftChain = new GiftChain();
            if (giftChainId != null) {
                giftChain.id = giftChainId;
            } else {
                giftChain.name = giftChainName;
            }
        }

        // Create the gift

        Gift gift = new Gift();
        gift.giftChain = giftChain;
        gift.title = giftTitle;
        gift.description = giftDescription;

        // Get the image
        //...

        // Send the gift
        File image = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/baobab.jpg");
        sendGift(gift, image);
    }

    void sendGift(Gift gift, File image) {
        String jsonGift = null;
        try {
            jsonGift = new ObjectMapper().writeValueAsString(gift);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }

        String url = Routes.urlFor(Routes.GIFTS_PATH);
        AuthMultiPartRequest<Gift> req = new AuthMultiPartRequest<>(Request.Method.POST, url, Gift.class,
                new Response.Listener<Gift>() {
                    @Override
                    public void onResponse(Gift response) {
                        Log.d("AAAAAAAAAA", "GIFT CREADO GUAY");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("AAAAAAAAAA", "GIFT NO CREADO");
                    }
                }
        );

        req.addStringUpload("gift", jsonGift);
        req.addFileUpload("image", image);
        req.setTag(this);
        Net.addToQueue(req);
    }

    private void testSendRequest() {
        String url = Routes.urlFor(Routes.GIFTS_PATH);
        AuthMultiPartRequest<Gift> req = new AuthMultiPartRequest<>(Request.Method.POST, url, Gift.class,
                new Response.Listener<Gift>() {
                    @Override
                    public void onResponse(Gift response) {
                        int i = 0;
                        i = 0;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int i = 0;
                        i = 0;
                    }
                }
        );

        Gift gift = new Gift();
        gift.title = "Desde android";
        gift.description = "Vamos a ver si se puede crear un gift con imagen desde android";
        gift.giftChain = new GiftChain();
        gift.giftChain.id = 1l;

        String jsonGift = null;
        try {
            jsonGift = new ObjectMapper().writeValueAsString(gift);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        req.addStringUpload("gift", jsonGift);
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/baobab.jpg");
        req.addFileUpload("image", file);


        Net.addToQueue(req);
    }


    class GiftChainRequestListener implements Response.Listener<List<GiftChain>>
    {
        @Override
        public void onResponse(List<GiftChain> response) {

        }
    }
}
