package com.capstone.potlatch.base;

import com.capstone.potlatch.models.Token;
import com.capstone.potlatch.models.User;

/**
 * Created by alvaro on 18/11/14.
 */
public class State {
    private static State mInstance;
    private User user;
    private Token token;

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

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean isUserLoggedIn() {
        return this.token != null;
    }
}
