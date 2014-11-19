package com.capstone.potlatch.base;

import com.capstone.potlatch.net.OAuth2Token;
import com.capstone.potlatch.models.User;

/**
 * Created by alvaro on 18/11/14.
 */
public class State {
    private static State mInstance;
    private User user;
    private OAuth2Token oauth2Token;

    private State() {}

    public static State get() {

        if (mInstance == null) {
            mInstance = new State();
        }
        return mInstance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OAuth2Token getOauth2Token() {
        return oauth2Token;
    }

    public void setOauth2Token(OAuth2Token oauth2Token) {
        this.oauth2Token = oauth2Token;
    }

    public boolean isUserLoggedIn() {
        return this.oauth2Token != null;
    }
}
