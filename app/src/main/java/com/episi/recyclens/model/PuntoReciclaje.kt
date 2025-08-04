package com.episi.recyclens.model

import com.google.firebase.firestore.GeoPoint

data class PuntoReciclaje(
    val id: String = "",
    val coordenadas: GeoPoint,
    val direccion: String,
    val horario: String,
    val nombreLugar: String,
    val tipo: List<String>
) {
    constructor() : this("", GeoPoint(0.0, 0.0), "", "", "", emptyList())
}