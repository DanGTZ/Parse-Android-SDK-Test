package com.perfexpert.parsetest;

import com.parse.Parse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Claude Joseph-Angélique on 5/29/18.
 */

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("parse_app_id")
                .clientKey("parse_client_key")
                .enableLocalDataStore()
                .server("http://10.0.2.2:1337/parse/")
                .clientBuilder(new OkHttpClient.Builder().addNetworkInterceptor(logging))
                .build());
    }
}
