package com.example.beaconble

import android.hardware.Sensor
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface APIService{
    @POST("addData")
    fun createPost(
      //  @Header("sensor_id") sensor_id:Int,
     //   @Header ("token")token:String,
        @Body body: SensorData): Call<ResponseBody>
}


