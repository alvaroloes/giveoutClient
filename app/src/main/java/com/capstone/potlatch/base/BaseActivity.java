package com.capstone.potlatch.base;

import android.app.Activity;
import android.app.DialogFragment;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * Created by alvaro on 23/11/14.
 */
public class BaseActivity extends Activity {

    public Response.ErrorListener getErrorListener(final boolean showToUser) {
        return getErrorListener(showToUser, null);
    }

    public Response.ErrorListener getErrorListener(final boolean showToUser, final DialogFragment progressDialog) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (showToUser) {

                }
                Log.d("GLOBAL ERROR LISTENER", "A request returned an error:", error);
            }
        };
    }

}
