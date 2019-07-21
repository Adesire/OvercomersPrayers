package com.overcomersprayer.app.overcomersprayers;

import com.overcomersprayer.app.overcomersprayers.models.RaveResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RaveApi {

    String BASE_URL = "https://api.ravepay.co/flwv3-pug/getpaidx/api/v2/";

    @Headers("content-type: application/json")
    @POST("verify")
    Call<RaveResponse> getRaveResponse(@Body RequestBody params);

}
