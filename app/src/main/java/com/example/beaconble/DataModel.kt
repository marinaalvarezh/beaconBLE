package com.example.beaconble

data class SensorData(
    val id_sensor: String,
    val timestamp: String,
    val latitud: Float,
    val longitud: Float,
    val orientacion: Float,
    val inclinacion: Float,
    val tipo_medida: String,
    val valor: Int
)

data class ConfigJSON(
    val sensor_id: String,
    val token: String
    )

data class Respuesta(
    val mensaje: String
)
