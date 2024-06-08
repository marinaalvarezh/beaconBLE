package com.example.beaconble

import okhttp3.Interceptor
import okhttp3.Response

class MyInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("sensor_id","7001")
            .addHeader("token","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmcmVzaCI6ZmFsc2UsImlhdCI6MTcxNjIyOTc0MCwianRpIjoiN2ZhMzRhN2UtNWFlYi00Y2QyLWE4ZjAtNWNmNDViMWU0NGNhIiwidHlwZSI6ImFjY2VzcyIsInN1YiI6NzAwMSwibmJmIjoxNzE2MjI5NzQwLCJleHAiOjE3MTYyMzA2NDB9.VW1Om0dNadi341-T0XZS3exOfG1WRnGGQtZjMd6uPVA")
            .build()

        return chain.proceed(request)
    }
}