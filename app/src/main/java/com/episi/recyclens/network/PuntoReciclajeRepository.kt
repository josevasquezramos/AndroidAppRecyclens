package com.episi.recyclens.network

import com.episi.recyclens.model.PuntoReciclaje
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.lang.Exception

class PuntoReciclajeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "puntosReciclaje"

    fun obtenerTodosLosPuntos(callback: Callback<List<PuntoReciclaje>>) {
        db.collection(collection)
            .get()
            .addOnSuccessListener { documents ->
                val puntos = documents.map { document ->
                    document.toObject(PuntoReciclaje::class.java).copy(id = document.id)
                }
                callback.onSuccess(puntos)
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun obtenerPuntosPorTipo(tipo: String, callback: Callback<List<PuntoReciclaje>>) {
        db.collection(collection)
            .whereArrayContains("tipo", tipo)
            .get()
            .addOnSuccessListener { documents ->
                val puntos = documents.map { document ->
                    document.toObject(PuntoReciclaje::class.java).copy(id = document.id)
                }
                callback.onSuccess(puntos)
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun obtenerPuntosCercanos(centro: GeoPoint, radio: Double, callback: Callback<List<PuntoReciclaje>>) {
        // Nota: Firestore no soporta consultas geoespaciales nativamente
        // Esta es una implementación básica que filtra en el cliente
        // Para producción, considera usar Geohashes o Firebase con extensión de Algolia

        db.collection(collection)
            .get()
            .addOnSuccessListener { documents ->
                val puntos = documents.map { document ->
                    document.toObject(PuntoReciclaje::class.java).copy(id = document.id)
                }.filter { punto ->
                    distanciaEntre(punto.coordenadas, centro) <= radio
                }
                callback.onSuccess(puntos)
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    private fun distanciaEntre(punto1: GeoPoint, punto2: GeoPoint): Double {
        val radioTierra = 6371 // km
        val lat1 = Math.toRadians(punto1.latitude)
        val lon1 = Math.toRadians(punto1.longitude)
        val lat2 = Math.toRadians(punto2.latitude)
        val lon2 = Math.toRadians(punto2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return radioTierra * c
    }
}