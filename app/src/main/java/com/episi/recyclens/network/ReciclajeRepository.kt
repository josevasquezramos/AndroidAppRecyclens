// com.episi.recyclens.network.ReciclajeRepository
package com.episi.recyclens.network

import com.episi.recyclens.model.Reciclaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ReciclajeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val bitacoraRepository: BitacoraRepository = BitacoraRepository()
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
            .addOnSuccessListener {
                bitacoraRepository.registrarOperacion(
                    coleccion = "reciclajes",
                    operacion = "CREAR",
                    documentoId = doc.id,
                    datosNuevos = reciclajeConId.toMap(),
                    detalles = "Nuevo reciclaje creado"
                )
                callback.onSuccess(null)
            }
            .addOnFailureListener { callback.onFailed(it) }
    }

    fun editarReciclaje(
        reciclaje: Reciclaje,
        callback: Callback<Void>
    ) {
        val currentUser = auth.currentUser ?: return callback.onFailed(Exception("Usuario no autenticado"))

        if (reciclaje.userId != currentUser.uid) {
            return callback.onFailed(Exception("No tienes permiso para editar este reciclaje"))
        }

        // Primero obtenemos los datos antiguos para la bitácora
        reciclajesRef.document(reciclaje.id).get()
            .addOnSuccessListener { snapshot ->
                val datosAntiguos = snapshot.data ?: emptyMap()

                // Luego actualizamos el documento
                reciclajesRef.document(reciclaje.id)
                    .set(reciclaje)
                    .addOnSuccessListener {
                        bitacoraRepository.registrarOperacion(
                            coleccion = "reciclajes",
                            operacion = "EDITAR",
                            documentoId = reciclaje.id,
                            datosAntiguos = datosAntiguos,
                            datosNuevos = reciclaje.toMap(),
                            detalles = "Reciclaje actualizado"
                        )
                        callback.onSuccess(null)
                    }
                    .addOnFailureListener { callback.onFailed(it) }
            }
            .addOnFailureListener { callback.onFailed(it) }
    }

    fun eliminarReciclaje(
        id: String,
        callback: Callback<Void>
    ) {
        // Primero obtenemos los datos para la bitácora
        reciclajesRef.document(id).get()
            .addOnSuccessListener { snapshot ->
                val datosAntiguos = snapshot.data ?: emptyMap()

                // Luego eliminamos el documento
                reciclajesRef.document(id)
                    .delete()
                    .addOnSuccessListener {
                        bitacoraRepository.registrarOperacion(
                            coleccion = "reciclajes",
                            operacion = "ELIMINAR",
                            documentoId = id,
                            datosAntiguos = datosAntiguos,
                            detalles = "Reciclaje eliminado"
                        )
                        callback.onSuccess(null)
                    }
                    .addOnFailureListener { callback.onFailed(it) }
            }
            .addOnFailureListener { callback.onFailed(it) }
    }

    fun actualizarEstadoReciclaje(
        id: String,
        nuevoEstado: String,
        callback: Callback<Void>
    ) {
        // Primero obtenemos los datos actuales
        reciclajesRef.document(id).get()
            .addOnSuccessListener { snapshot ->
                val reciclajeActual = snapshot.toObject(Reciclaje::class.java)
                val datosAntiguos = snapshot.data ?: emptyMap()

                if (reciclajeActual == null) {
                    callback.onFailed(Exception("Reciclaje no encontrado"))
                    return@addOnSuccessListener
                }

                // Actualizamos solo el estado
                reciclajesRef.document(id)
                    .update("estado", nuevoEstado)
                    .addOnSuccessListener {
                        val datosNuevos = datosAntiguos.toMutableMap().apply {
                            put("estado", nuevoEstado)
                        }

                        bitacoraRepository.registrarOperacion(
                            coleccion = "reciclajes",
                            operacion = "CANJEAR",
                            documentoId = id,
                            datosAntiguos = datosAntiguos,
                            datosNuevos = datosNuevos,
                            detalles = "Estado cambiado de ${reciclajeActual.estado} a $nuevoEstado"
                        )
                        callback.onSuccess(null)
                    }
                    .addOnFailureListener { callback.onFailed(it) }
            }
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

// Extensión para convertir Reciclaje a Map
private fun Reciclaje.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "tipo" to tipo,
        "cantidadKg" to cantidadKg,
        "latitud" to latitud,
        "longitud" to longitud,
        "estado" to estado,
        "fotoUrl" to fotoUrl,
        "timestamp" to timestamp
    )
}