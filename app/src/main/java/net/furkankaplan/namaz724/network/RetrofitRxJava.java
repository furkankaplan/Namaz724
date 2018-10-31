package net.furkankaplan.namaz724.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitRxJava {

    private final String BASE_URL = "https://ezanvakti.herokuapp.com";

    private RetrofitRxJavaInterface API;

    public RetrofitRxJava(){

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100,TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.BASE_URL).client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        API = retrofit.create(RetrofitRxJavaInterface.class);

    }

    public RetrofitRxJavaInterface getAPI() {
        return API;
    }


}
