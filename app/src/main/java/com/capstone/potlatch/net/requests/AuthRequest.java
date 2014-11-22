package com.capstone.potlatch.net.requests;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.capstone.potlatch.net.OAuth2Token;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alvaro on 13/11/14.
 */
public class AuthRequest<T> extends JacksonRequest<T> implements RequestWithAuth {
    private String basicAuthName;
    private String basicAuthPass;
    private OAuth2Token oauth2Token;
    private Map<String,String> headers = new HashMap<String, String>();

    public AuthRequest(int method, String url, TypeReference<T> resultTypeReference, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, resultTypeReference, listener, errorListener);
    }

    public AuthRequest(int method, String url, JavaType resultJavaType, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, resultJavaType, listener, errorListener);
    }

    public AuthRequest(int method, String url, Class<T> resultClass, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, resultClass, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        String authHeaderVal = null;

        if (oauth2Token != null) {
            authHeaderVal = "Bearer " + oauth2Token.access_token;
        } else if (basicAuthName != null && basicAuthPass != null) {
            byte[] token = (basicAuthName + ":" + basicAuthPass).getBytes();
            authHeaderVal = "Basic " + Base64.encodeToString(token, Base64.DEFAULT);
        }

        if (authHeaderVal != null) {
            headers.put("Authorization", authHeaderVal);
        }

        return headers;
    }

    @Override
    public void setBasicAuth(String username, String password) {
        basicAuthName = username;
        basicAuthPass = password;
    }

    @Override
    public void setOAuth2Token(OAuth2Token token) {
        oauth2Token = token;
    }

    @Override
    public OAuth2Token getOAuth2Token() {
        return oauth2Token;
    }

    @Override
    public void addHeaders(Map<String, String> extraHeaders) {
        headers.putAll(extraHeaders);
    }
}
