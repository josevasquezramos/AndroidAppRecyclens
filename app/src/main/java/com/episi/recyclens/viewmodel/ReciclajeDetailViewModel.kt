package com.episi.recyclens.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.network.Callback
import com.episi.recyclens.network.ReciclajeRepository
import com.google.firebase.firestore.ListenerRegistration

class ReciclajeDetailViewModel(
    private val repository: ReciclajeRepository = ReciclajeRepository()
) : ViewModel() {

    private val _reciclaje = MutableLiveData<Reciclaje>()
    val reciclaje: LiveData<Reciclaje> = _reciclaje

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var listener: ListenerRegistration? = null

    fun observarReciclaje(reciclajeId: String) {
        listener = repository.obtenerReciclajePorId(
            reciclajeId,
            onSuccess = { reciclaje ->
                _reciclaje.value = reciclaje
            },
            onError = { e ->
                _error.value = e.message
            }
        )
    }

    fun marcarComoCanjeado(id: String, cantidadKg: Double, onFinish: (Boolean, String?) -> Unit) {
        repository.actualizarEstadoReciclaje(id, "canjeado", object : Callback<Void> {
            override fun onSuccess(result: Void?) {
                // Sumar puntos al usuario (ejemplo: 10 puntos por kilo)
                val puntosASumar = (cantidadKg * 10).toInt()
                repository.sumarPuntosAlUsuario(puntosASumar, object : Callback<Void> {
                    override fun onSuccess(result: Void?) {
                        onFinish(true, null)
                    }
                    override fun onFailed(exception: Exception) {
                        onFinish(false, "Error al sumar puntos: ${exception.message}")
                    }
                })
            }
            override fun onFailed(exception: Exception) {
                onFinish(false, "Error al actualizar estado: ${exception.message}")
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}