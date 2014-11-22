package com.capstone.potlatch.net.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.capstone.potlatch.net.OAuth2Token;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alvaro on 20/11/14.
 */
public class OAuth2TokenRequest extends AuthRequest<OAuth2Token> {
    private final String username;
    private final String password;

    public OAuth2TokenRequest(String url, String username, String password, Response.Listener<OAuth2Token> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, OAuth2Token.class, listener, errorListener);
        this.username = username;
        this.password = password;
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String,String> params = new HashMap<String, String>();
        params.put("grant_type", "password");
        params.put("username", username);
        params.put("password", password);
        return params;
    }
}
