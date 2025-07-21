package com.episi.recyclens.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.network.Callback
import com.episi.recyclens.network.ReciclajeRepository
import com.google.firebase.firestore.ListenerRegistration

class ReciclajeViewModel(
    private val repository: ReciclajeRepository = ReciclajeRepository()
) : ViewModel() {

    private val _reciclajes = MutableLiveData<List<Reciclaje>>()
    val reciclajes: LiveData<List<Reciclaje>> get() = _reciclajes

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private var listener: ListenerRegistration? = null

    fun cargarReciclajes() {
        listener?.remove() // evitar duplicar listeners

        listener = repository.obtenerMisReciclajes(
            onResult = { lista ->
                _reciclajes.value = lista
            },
            onError = { e ->
                _error.value = e.message
            }
        )
    }

    fun agregarReciclaje(reciclaje: Reciclaje, onFinish: (Boolean, String?) -> Unit) {
        repository.agregarReciclaje(reciclaje, object : Callback<Void> {
            override fun onSuccess(result: Void?) {
                onFinish(true, null)
            }

            override fun onFailed(exception: Exception) {
                onFinish(false, exception.message)
            }
        })
    }

    fun marcarComoCanjeado(id: String, cantidadKg: Double = 0.0, onFinish: (Boolean, String?) -> Unit) {
        repository.actualizarEstadoReciclaje(id, "canjeado", object : Callback<Void> {
            override fun onSuccess(result: Void?) {
                // Sumar puntos al usuario: por ejemplo, 10 puntos por kilo
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
