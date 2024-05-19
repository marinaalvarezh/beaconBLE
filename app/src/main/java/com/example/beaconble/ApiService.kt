package com.example.beaconble

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface APIService{
    @GET("posts")
    fun getPosts(): Call<List<Post>>

    @POST("posts")
    fun createPost(@Body posts: MutableList<Post>): Call<Post>
}


