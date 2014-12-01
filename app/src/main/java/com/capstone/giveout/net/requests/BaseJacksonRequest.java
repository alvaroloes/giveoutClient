package com.capstone.giveout.net.requests;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alvaro on 13/11/14.
 */
public class BaseJacksonRequest<T> extends Request<T> {
    protected Map<String, String> headers = new HashMap<String, String>();

    private final Class<T> resultClass;
    private final TypeReference<T> resultTypeReference;
    private final JavaType resultJavaType;

    private Response.Listener<T> listener;

    public BaseJacksonRequest(int method, String url, TypeReference<T> resultTypeReference, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.resultTypeReference = resultTypeReference;
        this.resultJavaType = null;
        this.resultClass = null;
        this.listener = listener;
    }

    public BaseJacksonRequest(int method, String url, JavaType resultJavaType, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.resultTypeReference = null;
        this.resultJavaType = resultJavaType;
        this.resultClass = null;
        this.listener = listener;
    }

    public BaseJacksonRequest(int method, String url, Class<T> resultClass, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.resultTypeReference = null;
        this.resultJavaType = null;
        this.resultClass = resultClass;
        this.listener = listener;
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        ObjectMapper om = new ObjectMapper();
        try {
            T res = null;
            if (response.data != null && response.data.length > 0) {
                if (this.resultTypeReference != null) {
                    res = om.readValue(response.data, this.resultTypeReference);
                } else if (this.resultJavaType != null) {
                    res = om.readValue(response.data, this.resultJavaType);
                } else {
                    res = om.readValue(response.data, this.resultClass);
                }
            }
            return Response.success(res, HttpHeaderParser.parseCacheHeaders(response));
        } catch (IOException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError error) {
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String str = new String(error.networkResponse.data, "UTF8");
                Log.d("VOLLEY ERROR STRING", str);
            }
        } catch (UnsupportedEncodingException e) {
            return super.parseNetworkError(error);
        }
        return super.parseNetworkError(error);
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    public void setHeader(String title, String content) {
        headers.put(title, content);
    }
}
