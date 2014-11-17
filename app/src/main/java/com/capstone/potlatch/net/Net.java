package com.capstone.potlatch.net;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class Net {
    private static Net mInstance;
    private static Context mCtx;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private Net(Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mCtx = context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(mCtx);
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(mCtx));
    }

    public static synchronized Net getInstance(Context context) {
        if (context == null) {
            throw new RuntimeException("A context must be supplied before");
        }

        if (mInstance == null) {
            mInstance = new Net(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addRequestToQueue(Request<T> req) {
        mRequestQueue.add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
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
        return getInstance(mCtx).getRequestQueue();
    }

    public static <T> void addToQueue(Request<T> req) {
        getInstance(mCtx).addRequestToQueue(req);
    }

    public static ImageLoader getImgLoader() {
        return getInstance(mCtx).getImageLoader();
    }
}
