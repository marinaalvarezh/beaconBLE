package com.example.beaconble

data class Post(
    val ms:Long, val data:Int
)

data class Respuesta(
    val success: Boolean,
    val mensaje: String
)
