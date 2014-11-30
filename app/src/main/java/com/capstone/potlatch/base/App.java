package com.capstone.potlatch.base;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
        //TODO: Hacer el editar gift (creo que es simple)
        //TODO: Ordenar por creación o número de touches
        //TODO: No mostrar los obscenos según la preferencia del usuario
        //TODO: Material design
        //TODO: Image zoom
        //TODO: Filtro de gift chains por name no está implementado en el servidor

        // Initialize Config class
        Config.initFor(this, env.name());

        // Configure the Routes class
        Routes.baseUrl = Config.baseUrl;

        // Configure network singleton
        Net.setContext(this);

        // Sets the alarm for periodic updates
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int syncFreqMinutes = Integer.parseInt(sharedPref.getString("sync_freq", "15"));
        SyncManager.setAlarm(this, SyncManager.UPDATE_DATA_ACTION, syncFreqMinutes * 60 * 1000);

        /**
         * THIS IS A EXTREMELY INSECURE WAY TO ACCEPT HTTPS CERTIFICATES AS ALL OF THEM ARE ACCEPTED,
         * MAKING HTTPS TOTALLY USELESS. THIS IS ONLY INTENDED TO BE USED DURING DEVELOPMENT PROCESS
         *
         * REMOVE THIS FROM PRODUCTION CODE
         */
        CertUtils.allowAllSSL();

    }
}
