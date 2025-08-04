package com.episi.recyclens.network

import com.episi.recyclens.model.Bitacora
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BitacoraRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val bitacoraRef = firestore.collection("bitacora")

    fun registrarOperacion(
        coleccion: String,
        operacion: String,
        documentoId: String,
        datosAntiguos: Map<String, Any?> = emptyMap(),
        datosNuevos: Map<String, Any?> = emptyMap(),
        detalles: String = ""
    ) {
        val currentUser = auth.currentUser ?: return

        val bitacora = Bitacora(
            coleccion = coleccion,
            operacion = operacion,
            usuarioId = currentUser.uid,
            documentoId = documentoId,
            datosAntiguos = datosAntiguos,
            datosNuevos = datosNuevos,
            detalles = detalles
        )

        bitacoraRef.document().set(bitacora)
    }
}