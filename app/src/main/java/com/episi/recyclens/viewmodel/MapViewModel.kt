package com.episi.recyclens.viewmodel

import androidx.lifecycle.ViewModel
import com.episi.recyclens.model.PuntoReciclaje
import com.episi.recyclens.network.PuntoReciclajeRepository

class MapViewModel(
    private val repository: PuntoReciclajeRepository = PuntoReciclajeRepository()
) : ViewModel() {

    fun obtenerPuntosPorTipo(
        tipo: String,
        onResult: (List<PuntoReciclaje>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        repository.obtenerPuntosPorTipo(
            tipo = tipo,
            onResult = onResult,
            onError = onError
        )
    }
}
