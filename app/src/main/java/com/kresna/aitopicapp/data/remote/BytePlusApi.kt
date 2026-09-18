package com.kresna.aitopicapp.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BytePlusApi {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Body request: BytePlusRequest
    ): Response<BytePlusResponse>
}
