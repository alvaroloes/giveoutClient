package com.capstone.potlatch.net;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by alvaro on 13/11/14.
 */
public class JacksonRequest<T> extends Request<T> {
    private final Class<T> resultClass;
    private final Response.Listener<T> listener;
    private final TypeReference<T> resultTypeReference;
    private final JavaType resultJavaType;

    public JacksonRequest(String url, TypeReference<T> resultTypeReference, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.resultTypeReference = resultTypeReference;
        this.resultJavaType = null;
        this.resultClass = null;
        this.listener = listener;
    }

    public JacksonRequest(String url, JavaType resultJavaType, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.resultTypeReference = null;
        this.resultJavaType = resultJavaType;
        this.resultClass = null;
        this.listener = listener;
    }

    public JacksonRequest(String url, Class<T> resultClass, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.resultTypeReference = null;
        this.resultJavaType = null;
        this.resultClass = resultClass;
        this.listener = listener;
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        ObjectMapper om = new ObjectMapper();
        try {
            T res;
            if (this.resultTypeReference != null) {
                res = om.readValue(response.data, this.resultTypeReference);
            } else if (this.resultJavaType != null) {
                res = om.readValue(response.data, this.resultJavaType);
            } else {
                res = om.readValue(response.data, this.resultClass);
            }
            return Response.success(res, HttpHeaderParser.parseCacheHeaders(response));
        } catch (IOException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }
}