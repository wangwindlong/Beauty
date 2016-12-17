package com.dante.girls.net;

import com.dante.girls.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    private AApi aApi;
    private Retrofit retrofit;
    private String baseUrl;

    public static NetService getInstance(String baseUrl) {
        if (instance == null) {
            instance = new NetService();
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG ?
                    HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);
            instance.client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
                                    .method(original.method(), original.body())
                                    .build();

                            return chain.proceed(request);
                        }
                    })
                    .addInterceptor(logging).build();
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
            return createService(DBApi.class);
        }
        return dbApi;
    }

    public AApi getaApi() {
        if (dbApi == null) {
            return createService(AApi.class);
        }
        return aApi;
    }

    public  <T> T createService(Class<T> className) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(className);
    }


}
