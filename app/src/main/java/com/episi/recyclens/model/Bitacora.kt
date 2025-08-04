package com.episi.recyclens.model

data class Bitacora(
    val id: String = "", // generado por Firestore
    val coleccion: String = "", // nombre de la colección afectada
    val operacion: String = "", // CREAR | EDITAR | ELIMINAR
    val usuarioId: String = "", // uid del usuario que realizó la operación
    val documentoId: String = "", // id del documento afectado
    val datosAntiguos: Map<String, Any?> = emptyMap(), // datos antes de la operación (para editar/eliminar)
    val datosNuevos: Map<String, Any?> = emptyMap(), // datos después de la operación (para crear/editar)
    val timestamp: Long = System.currentTimeMillis(),
    val detalles: String = "" // información adicional si es necesaria
)