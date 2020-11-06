package com.example.govDiary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Api {
    public static Response sendRequest(String url, RequestBody formBody, Context context, Boolean ifLogin){
        //ONLY FOR TESTING PURPOSES
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLSocketFactory sslSocketFactory = null;
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        // Create an ssl socket factory with our all-trusting manager


        //END
        OkHttpClient client =  new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]).hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        })
                .build();
        Request request;
        if(formBody == null){
            request = new Request.Builder()
                    .url(url)
                    .build();
        }
        else{
            request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
        }
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if(response.code() == 400 && !ifLogin){
                SharedPreferences pref = context.getSharedPreferences("LogData",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("loginStatus", "fail");
                editor.apply();
                Intent intent = new Intent(context,JournalActivity.class);
                context.startActivity(intent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
