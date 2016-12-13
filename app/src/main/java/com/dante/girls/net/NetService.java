package com.dante.girls.net;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yons on 16/12/8.
 */

public class NetService {
    private static NetService instance;
    private OkHttpClient client;
    private GankApi gankApi;
    private DBApi dbApi;
    private Retrofit retrofit;
    private String baseUrl;

    public static NetService getInstance(String baseUrl) {
        if (instance == null) {
            instance = new NetService();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            instance.client = new OkHttpClient.Builder().addInterceptor(logging).build();
        }
        instance.baseUrl = baseUrl;
        return instance;
    }

    public GankApi getGankApi() {
        if (gankApi == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(Json.gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            return retrofit.create(GankApi.class);
        }
        return gankApi;
    }

    public DBApi getDbApi() {
        if (dbApi == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            return retrofit.create(DBApi.class);
        }
        return dbApi;

    }


}
