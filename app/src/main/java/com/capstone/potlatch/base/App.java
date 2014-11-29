package com.capstone.potlatch.base;

import android.app.Application;

import com.capstone.potlatch.net.Net;
import com.capstone.potlatch.utils.CertUtils;

/**
 * Created by alvaro on 13/11/14.
 */
public class App extends Application {
    public static final Environment env = Environment.local;

    @Override
    public void onCreate() {
        //TODO: Pantalla gift chains
        //TODO: Pantalla top givers
        //TODO: Update counts in background and notify the activities!! *
        //TODO: Ver de qué forma informar a todos que se han creado nuevos gifts cuando vengo de ActivityCreate... *

        //TODO: Hacer un progress dialog para evitar interacción del usuario (cuando el login por ejemplo)
        //TODO: Hacer un alert dialog
        //TODO: Hacer el editar gift (creo que es simple)
        //TODO: Ordenar por creación o número de touches
        //TODO: No mostrar los obscenos según la preferencia del usuario
        //TODO: Material design
        //TODO: Image zoom

        // Initialize Config class
        Config.initFor(this, env.name());

        // Configure the Routes class
        Routes.baseUrl = Config.baseUrl;

        // Configure network singleton
        Net.setContext(this);

        /**
         * THIS IS A EXTREMELY INSECURE WAY TO ACCEPT HTTPS CERTIFICATES AS ALL OF THEM ARE ACCEPTED,
         * MAKING HTTPS TOTALLY USELESS. THIS IS ONLY INTENDED TO BE USED DURING DEVELOPMENT PROCESS
         *
         * REMOVE THIS FROM PRODUCTION CODE
         */
        CertUtils.allowAllSSL();

    }
}
