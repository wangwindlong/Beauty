package com.dante.girls.net;

import com.dante.girls.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Net API services.
 */

public class NetService {
    private static NetService instance;
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//            .setExclusionStrategies(new ExclusionStrategy() {
//                @Override
//                public boolean shouldSkipField(FieldAttributes f) {
//                    return f.getDeclaringClass().equals(RealmObject.class);
//                }
//
//                @Override
//                public boolean shouldSkipClass(Class<?> clazz) {
//                    return false;
//                }
//            })
            .create();
    private Retrofit retrofit;
    private String baseUrl;
    private OkHttpClient client;
    private GankApi gankApi;
    private DBApi dbApi;
    private AApi aApi;
    private AppApi appApi;

    public static NetService getInstance(String baseUrl) {
        if (instance == null) {
            instance = new NetService();
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG ?
                    HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);
            instance.client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(logging).build();
        }
        instance.baseUrl = baseUrl;
        return instance;
    }

    public GankApi getGankApi() {
        if (gankApi == null) {
            gankApi = createServiceWithGson(GankApi.class);
        }
        return gankApi;
    }

    public DBApi getDbApi() {
        if (dbApi == null) {
            dbApi = createService(DBApi.class);
        }
        return dbApi;
    }

    public AApi getaApi() {
        if (aApi == null) {
            aApi = createService(AApi.class);
        }
        return aApi;
    }

    public AppApi getAppApi() {
        return createServiceWithGson(AppApi.class);
    }

    public <T> T createService(Class<T> className) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(className);
    }

    public <T> T createServiceWithGson(Class<T> className) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(className);
    }
}
