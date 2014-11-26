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
        //TODO: Get the saved state and set the token header in Net.
        //TODO: Hacer un progress dialog para evitar interacción del usuario (cuando el login por ejemplo)

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

//    private void totallyInsecureMethod_AcceptAllCertificates() {
//        try {
//            TrustManager[] trustAllCerts = new TrustManager[] {
//                new X509TrustManager() {
//                    @Override
//                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    @Override
//                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    public X509Certificate[] getAcceptedIssuers() {
//                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
//                        return myTrustedAnchors;
//                    }
//                }
//            };
//
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustAllCerts, new SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//                @Override
//                public boolean verify(String arg0, SSLSession arg1) {
//                    return true;
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
