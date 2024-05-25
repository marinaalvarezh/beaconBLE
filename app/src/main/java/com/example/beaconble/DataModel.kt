package com.example.beaconble

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class SensorData(
    @SerializedName("id_sensor") val id_sensor: Int,
    @SerializedName("token")val token: String,
    @SerializedName("timestamp")val timestamp: String,
    @SerializedName("latitud")val latitud: String,
    @SerializedName("longitud")val longitud: String,
    @SerializedName("orientacion")val orientacion: Int,
    @SerializedName("inclinacion")val inclinacion: Int,
    @SerializedName("tipo_medida")val tipo_medida: String,
    @SerializedName("valor_medida")val valor_medida: Double
)

data class ConfigJSON(
    val sensor_id : Int,
    val token: String
)

data class Respuesta(
    val mensaje: String
)
