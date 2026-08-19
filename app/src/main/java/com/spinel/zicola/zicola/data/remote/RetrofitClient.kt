package com.spinel.zicola.zicola.data.remote

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(CommentsApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val commentsApi: CommentsApi by lazy {
        retrofit.create(CommentsApi::class.java)
    }
}
