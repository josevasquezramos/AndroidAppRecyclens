package com.episi.recyclens.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.episi.recyclens.model.FraseMotivadora
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.network.Callback
import com.episi.recyclens.network.FraseMotivadoraRepository
import com.episi.recyclens.network.ReciclajeRepository

class DetailsViewModel(
    private val reciclajeRepository: ReciclajeRepository = ReciclajeRepository(),
    private val fraseMotivadoraRepository: FraseMotivadoraRepository = FraseMotivadoraRepository()
) : ViewModel() {

    private val _reciclajesCanjeados = MutableLiveData<List<Reciclaje>>()
    val reciclajesCanjeados: LiveData<List<Reciclaje>> = _reciclajesCanjeados

    private val _fraseDelDia = MutableLiveData<FraseMotivadora?>()
    val fraseDelDia: LiveData<FraseMotivadora?> = _fraseDelDia

    fun obtenerReciclajesCanjeados() {
        reciclajeRepository.obtenerMisReciclajes(
            onResult = { reciclajes ->
                val reciclajesCanjeados = reciclajes.filter { it.estado == "canjeado" }
                _reciclajesCanjeados.postValue(reciclajesCanjeados)
            },
            onError = { error ->
                // Manejar el error (puedes mostrar un mensaje o log)
            }
        )
    }

    fun obtenerFraseDelDia() {
        fraseMotivadoraRepository.obtenerUltimaFraseDelDia(object : Callback<FraseMotivadora> {
            override fun onSuccess(result: FraseMotivadora?) {
                _fraseDelDia.postValue(result)
            }

            override fun onFailed(exception: Exception) {
                // Manejar el error (puedes mostrar un mensaje o log)
            }
        })
    }
}
