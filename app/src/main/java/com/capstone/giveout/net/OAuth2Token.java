package com.capstone.giveout.net;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by alvaro on 13/11/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2Token {
    public String access_token;
    public String refresh_token;
    public String token_type;
    public long expires_in;
    public String scope;
}
