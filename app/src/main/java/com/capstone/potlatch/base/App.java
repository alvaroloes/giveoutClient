package com.capstone.potlatch.base;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.capstone.potlatch.ActivitySettings;
import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.utils.CertUtils;
import com.capstone.potlatch.utils.SyncManager;

/**
 * Created by alvaro on 13/11/14.
 */
public class App extends Application {
    public static final Environment env = Environment.local;

    @Override
    public void onCreate() {

        //TODO: Hacer un progress dialog para evitar interacción del usuario (cuando el login por ejemplo)
        //TODO: Hacer un alert dialog

        //TODO: Crear usuario
        //TODO: Mostrar imágenes con un fade in
        //TODO: Image zoom
        //TODO: Hacer el editar gift (creo que es simple)

        //TODO: Material design
        //TODO: Filtro de gift chains por name no está implementado en el servidor

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
