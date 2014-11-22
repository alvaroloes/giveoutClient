package com.capstone.potlatch.net.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.capstone.potlatch.net.OAuth2Token;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alvaro on 20/11/14.
 */
public class OAuth2RefreshTokenRequest extends AuthRequest<OAuth2Token> {
    private final String url;
    private final String grantType;
    private final OAuth2Token token;

    public OAuth2RefreshTokenRequest(String url, String grantType, OAuth2Token token, Response.Listener<OAuth2Token> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, OAuth2Token.class, listener, errorListener);
        this.url = url;
        this.grantType = grantType;
        this.token = token;
    }

    @Override
    public Priority getPriority() {
        return Priority.IMMEDIATE;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String,String> params = new HashMap<String, String>();
        params.put("grant_type", grantType);
        params.put("refresh_token", token.refresh_token);
        return params;
    }
}
