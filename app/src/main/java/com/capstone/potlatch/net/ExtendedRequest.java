package com.capstone.potlatch.net;

import java.util.Map;

/**
 * Created by alvaro on 19/11/14.
 */
public interface ExtendedRequest {
    public void setBasicAuth(String username, String password);
    public void setOAuth2Token(OAuth2Token token);
    public OAuth2Token getOAuth2Token();
    public void addHeaders(Map<String, String> extraHeaders);
}
