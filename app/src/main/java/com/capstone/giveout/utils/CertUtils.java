package com.capstone.giveout.utils;

import android.util.Log;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class CertUtils {
    private static TrustManager[] trustManagers;

    private static class _FakeX509TrustManager implements javax.net.ssl.X509TrustManager {
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public boolean isClientTrusted(X509Certificate[] chain) {
            return (true);
        }

        public boolean isServerTrusted(X509Certificate[] chain) {
            return (true);
        }

        public X509Certificate[] getAcceptedIssuers() {
            return (_AcceptedIssuers);
        }
    }

    /**
     * Avoid the host verification on SSL connections. This should not be used in
     * production.
     */
    public static void allowAllSSL() {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        SSLContext context = null;

        if (trustManagers == null) {
            trustManagers = new TrustManager[] { new _FakeX509TrustManager() };
        }

        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("allowAllSSL", e.toString());
        } catch (KeyManagementException e) {
            Log.e("allowAllSSL", e.toString());
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    /**
     * Allow SSL trusted connection to host whose certification file is passed
     */
    public static void useCertificate(InputStream crtFile, String alias, String password){
        KeyStore trustedKeyStore = CertUtils.ConvertCerToBKS(crtFile, alias, password.toCharArray());

        // Pass the keystore to the SSLSocketFactory. The factory is responsible
        // for the verification of the server certificate.
        try {
            String algorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(trustedKeyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

    }
    public static KeyStore ConvertCerToBKS(InputStream cerStream, String alias, char [] password)
    {
        KeyStore keyStore = null;
        try
        {
            keyStore = KeyStore.getInstance("BKS", "BC");
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
            Certificate certificate = factory.generateCertificate(cerStream);
            keyStore.load(null, password);
            keyStore.setCertificateEntry(alias, certificate);
        }
        catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keyStore;
    }

//    class TrustedSocketFactory extends javax.net.ssl.SSLSocketFactory {
//        private org.apache.http.conn.ssl.SSLSocketFactory apacheSSLSocketFactory;
//        TrustedSocketFactory(KeyStore trustedKeyStore) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//            apacheSSLSocketFactory = new SSLSocketFactory(trustedKeyStore);
//        }
//        @Override
//        public String[] getDefaultCipherSuites() {
//            return apacheSSLSocketFactory.
//        }
//
//        @Override
//        public String[] getSupportedCipherSuites() {
//            return new String[0];
//        }
//
//        @Override
//        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
//            return null;
//        }
//
//        @Override
//        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
//            return null;
//        }
//
//        @Override
//        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
//            return null;
//        }
//
//        @Override
//        public Socket createSocket(InetAddress host, int port) throws IOException {
//            return null;
//        }
//
//        @Override
//        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
//            return null;
//        }
//    }
}