package com.episi.recyclens.network

import com.episi.recyclens.model.FraseMotivadora
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FraseMotivadoraRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Obtener la última frase motivadora (ordenada por fecha descendente)
     */
    fun obtenerUltimaFraseDelDia(callback: Callback<FraseMotivadora>) {
        firestore.collection("frasesMotivadoras")
            .orderBy("fecha", Query.Direction.DESCENDING) // Ordenar por fecha descendente
            .limit(1) // Obtener solo la última frase
            .get()
            .addOnSuccessListener { querySnapshot ->
                val frase = querySnapshot.documents.firstOrNull()?.toObject(FraseMotivadora::class.java)
                if (frase != null) {
                    callback.onSuccess(frase)
                } else {
                    callback.onFailed(Exception("No se encontró ninguna frase motivadora"))
                }
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }
}