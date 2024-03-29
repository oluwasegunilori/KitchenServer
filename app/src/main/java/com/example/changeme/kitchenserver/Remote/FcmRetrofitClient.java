package com.example.changeme.kitchenserver.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by SHEGZ on 1/6/2018.
 */
public class FcmRetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseURL) {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
