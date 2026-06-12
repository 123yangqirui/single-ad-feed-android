package com.example.myapplication.dialog

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AiService {

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    fun sendMessage(@Body request: DeepSeekRequest): Call<DeepSeekResponse>
}
