package com.episi.recyclens.network

import com.episi.recyclens.model.Reciclaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ReciclajeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val reciclajesRef = firestore.collection("reciclajes")

    fun agregarReciclaje(
        reciclaje: Reciclaje,
        callback: Callback<Void>
    ) {
        val currentUser = auth.currentUser ?: return callback.onFailed(Exception("Usuario no autenticado"))

        val doc = reciclajesRef.document()
        val reciclajeConId = reciclaje.copy(id = doc.id, userId = currentUser.uid)

        doc.set(reciclajeConId)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener { callback.onFailed(it) }
    }

    fun obtenerMisReciclajes(
        onResult: (List<Reciclaje>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val currentUser = auth.currentUser ?: return null

        return reciclajesRef
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    val lista = snapshot?.toObjects(Reciclaje::class.java) ?: emptyList()
                    onResult(lista)
                }
            }
    }

    fun actualizarEstadoReciclaje(
        id: String,
        nuevoEstado: String,
        callback: Callback<Void>
    ) {
        reciclajesRef.document(id)
            .update("estado", nuevoEstado)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener { callback.onFailed(it) }
    }

    fun sumarPuntosAlUsuario(cantidad: Int, callback: Callback<Void>) {
        val user = auth.currentUser ?: return callback.onFailed(Exception("Usuario no autenticado"))
        val userRef = firestore.collection("usuarios").document(user.uid)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val puntosActuales = snapshot.getLong("puntos") ?: 0L
            val nuevosPuntos = puntosActuales + cantidad
            transaction.update(userRef, "puntos", nuevosPuntos)
        }.addOnSuccessListener {
            callback.onSuccess(null)
        }.addOnFailureListener {
            callback.onFailed(it)
        }
    }

    fun obtenerReciclajePorId(
        reciclajeId: String,
        onSuccess: (Reciclaje) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection("reciclajes")
            .document(reciclajeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(Exception("Error en tiempo real: ${error.message}"))
                    return@addSnapshotListener
                }

                snapshot?.toObject(Reciclaje::class.java)?.let {
                    onSuccess(it)
                } ?: onError(Exception("Documento no encontrado"))
            }
    }
}
