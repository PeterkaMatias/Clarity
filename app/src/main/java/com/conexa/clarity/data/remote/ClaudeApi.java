package com.conexa.clarity.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ClaudeApi {

    @Headers({
            "Content-Type: application/json",
            "anthropic-version: 2023-06-01"
    })

    @POST("v1/messages")
    Call<ClaudeResponse> enviarMensaje(
            @retrofit2.http.Header("x-api-key") String apiKey,
            @Body ClaudeRequest request
    );
}