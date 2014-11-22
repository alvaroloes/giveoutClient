package com.capstone.potlatch.net;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.capstone.potlatch.net.requests.RequestWithAuth;

import java.util.HashMap;
import java.util.Map;

public class Net {
    private static Net mInstance;
    private static Context mCtx;

    private OAuth2Token oauth2Token;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private Map<String,String> globalHeaders;
    private String oauth2TokenRefreshURL;
    private String oauth2TokenRefreshGrantType = "refresh_token";

    private Net(Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mCtx = context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(mCtx);
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(mCtx));
        globalHeaders = new HashMap<String, String>();
    }

    public static synchronized Net get(Context context) {
        if (context == null) {
            throw new RuntimeException("A context must be supplied before");
        }

        if (mInstance == null) {
            mInstance = new Net(context);
        }
        return mInstance;
    }

    /**
     * This method is useful for one time initialization and then use the static accessors, like
     * queue or addToQueue
     * @param context
     */
    public static void setContext(Context context) {
        mCtx = context.getApplicationContext();
    }

    // Convenience static methods. These only works if a context has been already supplied,
    // for example through setContext. If not, they will throw a RuntimeException

    public static RequestQueue getQueue() {
        return get(mCtx).mRequestQueue;
    }

    public static <T> void addToQueue(Request<T> req) {
        if (req instanceof RequestWithAuth) {
            RequestWithAuth exReq = (RequestWithAuth) req;

            exReq.addHeaders(getGlobalHeaders());

            // Only set the global values if the request doesn't have them already specified

            if (exReq.getOAuth2Token() == null) {
                exReq.setOAuth2Token(getGlobalOAuth2Token());
            }

            if (exReq.getOauth2TokenRefreshURL() == null) {
                exReq.setOauth2TokenRefreshURL(getOauth2TokenRefreshURL());
            }

            if (exReq.getOauth2TokenRefreshGrantType() == null) {
                exReq.setOauth2TokenRefreshGrantType(getOauth2TokenRefreshGrantType());
            }
        }
        get(mCtx).mRequestQueue.add(req);
    }

    public static ImageLoader getImgLoader() {
        return get(mCtx).mImageLoader;
    }

    public static Map<String, String> getGlobalHeaders() {
        return get(mCtx).globalHeaders;
    }

    public static void setGlobalHeaders(Map<String, String> globalHeaders) {
        get(mCtx).globalHeaders = globalHeaders;
    }

    public static void setGlobalOAuth2Token(OAuth2Token oauth2Token) {
        get(mCtx).oauth2Token = oauth2Token;
    }

    public static OAuth2Token getGlobalOAuth2Token() {
        return get(mCtx).oauth2Token;
    }

    public static String getOauth2TokenRefreshURL() {
        return get(mCtx).oauth2TokenRefreshURL;
    }

    public static void setOauth2TokenRefreshURL(String oauth2TokenRefreshURL) {
        get(mCtx).oauth2TokenRefreshURL = oauth2TokenRefreshURL;
    }

    public static String getOauth2TokenRefreshGrantType() {
        return get(mCtx).oauth2TokenRefreshGrantType;
    }

    public static void setOauth2TokenRefreshGrantType(String oauth2TokenRefreshGrantType) {
        get(mCtx).oauth2TokenRefreshGrantType = oauth2TokenRefreshGrantType;
    }
}
