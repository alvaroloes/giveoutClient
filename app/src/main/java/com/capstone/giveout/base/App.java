package com.capstone.giveout.base;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.capstone.giveout.ActivitySettings;
import com.capstone.giveout.net.Net;
import com.capstone.giveout.utils.CertUtils;
import com.capstone.giveout.utils.SyncManager;

/**
 * Created by alvaro on 13/11/14.
 */
public class App extends Application {
    public static final Environment env = Environment.local;

    @Override
    public void onCreate() {
        // Initialize Config class
        Config.initFor(this, env.name());

        // Configure the Routes class
        Routes.baseUrl = Config.baseUrl;

        // Configure network singleton
        Net.setContext(this);

        // Configure app behaviour according to users preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        int syncFreqMinutes = Integer.parseInt(preferences.getString(ActivitySettings.KEY_SYNC_FREQ, "15"));
        SyncManager.setAlarm(this, SyncManager.UPDATE_DATA_ACTION, syncFreqMinutes * 60 * 1000);

        Config.noInappropriateGifts = preferences.getBoolean(ActivitySettings.KEY_NO_INAPPROPRIATE_GIFT, true);

        /**
         * THIS IS A EXTREMELY INSECURE WAY TO ACCEPT HTTPS CERTIFICATES AS ALL OF THEM ARE ACCEPTED,
         * MAKING HTTPS TOTALLY USELESS. THIS IS ONLY INTENDED TO BE USED DURING DEVELOPMENT PROCESS
         *
         * REMOVE THIS FROM PRODUCTION CODE
         */
        CertUtils.allowAllSSL();

    }
}
