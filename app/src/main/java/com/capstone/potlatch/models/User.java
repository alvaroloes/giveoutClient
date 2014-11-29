package com.capstone.potlatch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by alvaro on 13/11/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public long id;
    public String username;
    public int giftCount;
    public int giftTouches;
    public String imageUrlFull;
    public String imageUrlMedium;
}
