package com.capstone.potlatch.base;

import android.app.Application;

import com.capstone.potlatch.net.Net;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by alvaro on 13/11/14.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        //Configure network singleton
        Net.setContext(this);

        // ONLY WHILE DEVELOPING. REMOVE THIS FROM PRODUCTION CODE
        totallyInsecureMethod_AcceptAllCertificates();
    }

    /**
     * THIS IS A EXTREMELY INSECURE WAY TO ACCEPT HTTPS CERTIFICATES ASS ALL OF THEM ARE ACCEPTED,
     * MAKING HTTPS TOTALLY USELESS. THIS IS ONLY INTENDED TO BE USED DURING DEVELOPMENT PROCESS
     *
     * REMOVE THIS FROM PRODUCTION CODE
     */
    private void totallyInsecureMethod_AcceptAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                        return myTrustedAnchors;
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
