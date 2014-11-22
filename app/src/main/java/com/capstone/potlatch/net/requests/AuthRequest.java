package com.capstone.potlatch.net.requests;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.net.OAuth2Token;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alvaro on 13/11/14.
 */
public class AuthRequest<T> extends JacksonRequest<T> implements RequestWithAuth {

    private String oauth2TokenRefreshURL;
    private String oauth2TokenRefreshGrantType = "refresh_token";

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
    public void deliverError(VolleyError error) {
        if (error instanceof AuthFailureError) {
            if (getOAuth2Token() != null) {
                System.out.println("Refreshing token...");
                // Here we put a refresh token request inside the queue. As it have the highest priority,
                // the oauth token will be refreshed before the second attempt of this request is made.
                OAuth2RefreshTokenRequest refreshReq = new OAuth2RefreshTokenRequest(getOauth2TokenRefreshURL(),
                        getOauth2TokenRefreshGrantType(),
                        getOAuth2Token(),
                   new Response.Listener<OAuth2Token>() {
                    @Override
                    public void onResponse(OAuth2Token response) {
                        System.out.println("Token refresh success.");

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Token refresh failed.");
                    }
                });
                Net.addToQueue(refreshReq);
            }
        }

        super.deliverError(error);
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

    public String getOauth2TokenRefreshURL() {
        return oauth2TokenRefreshURL;
    }

    public void setOauth2TokenRefreshURL(String oauth2TokenRefreshURL) {
        this.oauth2TokenRefreshURL = oauth2TokenRefreshURL;
    }

    public String getOauth2TokenRefreshGrantType() {
        return oauth2TokenRefreshGrantType;
    }

    public void setOauth2TokenRefreshGrantType(String oauth2TokenRefreshGrantType) {
        this.oauth2TokenRefreshGrantType = oauth2TokenRefreshGrantType;
    }
}
