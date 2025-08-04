package com.episi.recyclens.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.episi.recyclens.model.PuntoReciclaje
import com.episi.recyclens.network.PuntoReciclajeRepository
import com.episi.recyclens.network.Callback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.lang.Exception

class MapViewModel(private val repository: PuntoReciclajeRepository) : ViewModel() {

    private val _recyclingMarkers = MutableLiveData<List<MarkerOptions>>()
    val recyclingMarkers: LiveData<List<MarkerOptions>> = _recyclingMarkers

    private val _userMarker = MutableLiveData<MarkerOptions>()
    val userMarker: LiveData<MarkerOptions> = _userMarker

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _mapCenter = MutableLiveData<LatLngBounds>()
    val mapCenter: LiveData<LatLngBounds> = _mapCenter

    fun setUserPosition(position: LatLng) {
        _userMarker.value = MarkerOptions()
            .position(position)
            .title("Tu ubicación")
            .snippet("Estás aquí")
    }

    fun buscarPuntosPorTipo(tipo: String, userPosition: LatLng?) {
        _loading.value = true
        viewModelScope.launch {
            repository.obtenerPuntosPorTipo(tipo, object : Callback<List<PuntoReciclaje>> {
                override fun onSuccess(result: List<PuntoReciclaje>?) {
                    result?.let { puntos ->
                        val markerList = puntos.map { punto ->
                            MarkerOptions()
                                .position(LatLng(punto.coordenadas.latitude, punto.coordenadas.longitude))
                                .title(punto.nombreLugar)
                                .snippet("""
                                Tipo: ${punto.tipo.joinToString()}
                                Dirección: ${punto.direccion}
                                Horario: ${punto.horario}
                            """.trimIndent())
                        }
                        _recyclingMarkers.postValue(markerList)

                        // Calcular bounds para centrar el mapa
                        val builder = LatLngBounds.Builder()
                        userPosition?.let { builder.include(it) }
                        puntos.forEach { punto ->
                            builder.include(LatLng(punto.coordenadas.latitude, punto.coordenadas.longitude))
                        }
                        _mapCenter.postValue(builder.build())
                    }
                    _loading.postValue(false)
                }

                override fun onFailed(exception: Exception) {
                    _errorMessage.postValue("Error al cargar puntos: ${exception.message}")
                    _loading.postValue(false)
                }
            })
        }
    }
}