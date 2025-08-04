package com.episi.recyclens.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.network.Callback
import com.episi.recyclens.network.FotoRepository
import com.episi.recyclens.network.ReciclajeRepository
import com.google.firebase.firestore.ListenerRegistration

class ReciclajeViewModel(
    private val repository: ReciclajeRepository = ReciclajeRepository(),
    private val fotoRepository: FotoRepository = FotoRepository()
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
                // Ordenar la lista de reciclajes por timestamp en orden descendente
                val reciclajesOrdenados = lista.sortedByDescending { it.timestamp }
                _reciclajes.value = reciclajesOrdenados
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

    fun agregarReciclajeConFoto(context: Context, reciclaje: Reciclaje, fotoUri: Uri, callback: (Boolean, String?) -> Unit) {
        fotoRepository.subirFoto(context, fotoUri) { url ->
            if (url != null) {
                val reciclajeConFoto = reciclaje.copy(fotoUrl = url)
                repository.agregarReciclaje(reciclajeConFoto, object : Callback<Void> {
                    override fun onSuccess(result: Void?) {
                        callback(true, null)
                    }
                    override fun onFailed(exception: Exception) {
                        callback(false, exception.message)
                    }
                })
            } else {
                callback(false, "Error al subir la foto")
            }
        }
    }

    fun marcarComoCanjeado(id: String, onFinish: (Boolean, String?) -> Unit) {
        repository.actualizarEstadoReciclaje(id, "canjeado", object : Callback<Void> {
            override fun onSuccess(result: Void?) {
                onFinish(true, null) // Solo éxito al actualizar estado
            }
            override fun onFailed(exception: Exception) {
                onFinish(false, "Error al actualizar estado: ${exception.message}")
            }
        })
    }

    fun obtenerReciclajePorId(
        reciclajeId: String,
        onSuccess: (Reciclaje) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return repository.obtenerReciclajePorId(
            reciclajeId,
            onSuccess = { reciclaje ->
                onSuccess(reciclaje)
            },
            onError = { exception ->
                onError(exception.message ?: "Error al cargar reciclaje")
            }
        )
    }

    fun editarReciclaje(
        reciclaje: Reciclaje,
        onFinish: (Boolean, String?) -> Unit
    ) {
        if (reciclaje.estado != "pendiente") {
            onFinish(false, "Solo se pueden editar reciclajes en estado pendiente")
            return
        }

        repository.editarReciclaje(reciclaje, object : Callback<Void> {
            override fun onSuccess(result: Void?) {
                onFinish(true, null)
            }

            override fun onFailed(exception: Exception) {
                onFinish(false, exception.message)
            }
        })
    }

    fun eliminarReciclaje(id: String, estado: String, onFinish: (Boolean, String?) -> Unit) {
        if (estado != "pendiente") {
            onFinish(false, "Solo se pueden eliminar reciclajes en estado pendiente")
            return
        }

        repository.eliminarReciclaje(id, object : Callback<Void> {
            override fun onSuccess(result: Void?) {
                onFinish(true, null)
            }

            override fun onFailed(exception: Exception) {
                onFinish(false, exception.message)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}