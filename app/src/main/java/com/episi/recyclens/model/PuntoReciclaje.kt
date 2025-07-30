package com.episi.recyclens.model

import com.google.firebase.firestore.GeoPoint

data class PuntoReciclaje(
    var nombreLugar: String? = null,
    var coordenadas: GeoPoint? = null,
    var tipo: List<String>? = null,
    var direccion: String? = null,
    var horario: String? = null
)