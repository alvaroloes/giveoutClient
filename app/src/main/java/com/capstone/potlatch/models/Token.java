package com.capstone.potlatch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by alvaro on 13/11/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {
    public String access_token;
    public String token_type;
    public long expires_in;
    public String scope;
}
