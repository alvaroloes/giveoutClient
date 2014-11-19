package com.capstone.potlatch.net;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Net {
    private static Net mInstance;
    private static Context mCtx;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private Map<String,String> globalHeaders;

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
        if (req instanceof ExtendedRequest) {
            ((ExtendedRequest) req).addHeaders(get(mCtx).globalHeaders);
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
}
