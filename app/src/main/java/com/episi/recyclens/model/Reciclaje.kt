package com.episi.recyclens.model

data class Reciclaje(
    val id: String = "", // generado por Firestore
    val userId: String = "", // uid del usuario que lo creó
    val tipo: String = "", // papel, plástico, metal, orgánico
    val cantidadKg: Double = 0.0, // cantidad en kilos
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val estado: String = "pendiente", // pendiente | canjeable | canjeado
    val timestamp: Long = System.currentTimeMillis()
)