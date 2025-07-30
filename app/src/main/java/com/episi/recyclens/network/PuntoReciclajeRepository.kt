package com.episi.recyclens.network

import com.episi.recyclens.model.PuntoReciclaje
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PuntoReciclajeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val puntosRef = firestore.collection("puntosReciclaje")

    // Agregar un nuevo punto de reciclaje
    fun agregarPunto(
        punto: PuntoReciclaje,
        callback: Callback<Void>
    ) {
        val doc = puntosRef.document()
        val puntoConId = punto.copy() // puedes extender el modelo para incluir un campo id si lo necesitas
        doc.set(puntoConId)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener { callback.onFailed(it) }
    }

    // Obtener todos los puntos
    fun obtenerPuntos(
        onResult: (List<PuntoReciclaje>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return puntosRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    val lista = snapshot?.toObjects(PuntoReciclaje::class.java) ?: emptyList()
                    onResult(lista)
                }
            }
    }

    // Filtrar por tipo de reciclaje (por ejemplo: "papel")
    fun obtenerPuntosPorTipo(
        tipo: String,
        onResult: (List<PuntoReciclaje>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return puntosRef
            .whereArrayContains("tipo", tipo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    val lista = snapshot?.toObjects(PuntoReciclaje::class.java) ?: emptyList()
                    onResult(lista)
                }
            }
    }

    // Eliminar punto si necesitas (opcional)
    fun eliminarPunto(
        idDocumento: String,
        callback: Callback<Void>
    ) {
        puntosRef.document(idDocumento)
            .delete()
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener { callback.onFailed(it) }
    }
}