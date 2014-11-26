package com.capstone.potlatch;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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


public class ActivityCreateUpdateGift extends BaseActivity implements DialogLogin.OnLoginListener {
    private static final String TAG_LOGIN = "ActivityCreateGift - TAG_LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gift);
        if (! State.get().isUserLoggedIn()) {
            DialogLogin.open(getFragmentManager(), TAG_LOGIN);
        }
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
}
