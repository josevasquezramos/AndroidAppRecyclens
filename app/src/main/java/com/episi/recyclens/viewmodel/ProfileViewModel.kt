package com.episi.recyclens.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.model.User
import com.episi.recyclens.network.AuthenticationRepository
import com.episi.recyclens.network.ReciclajeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthenticationRepository = AuthenticationRepository(),
    private val reciclajeRepository: ReciclajeRepository = ReciclajeRepository()
) : ViewModel() {

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData

    private val _reciclajeStats = MutableLiveData<Map<String, Double>>()
    val reciclajeStats: LiveData<Map<String, Double>> = _reciclajeStats

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadUserData()
        loadReciclajeStats()
    }

    private fun loadUserData() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.let { user ->
            _userData.value = User(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: "Usuario"
            )
        }
    }

    private fun loadReciclajeStats() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.let { user ->
                    reciclajeRepository.obtenerMisReciclajes(
                        onResult = { reciclajes ->
                            val stats = calculateStats(reciclajes)
                            _reciclajeStats.postValue(stats)
                            _loading.postValue(false)
                        },
                        onError = { exception ->
                            _error.postValue(exception.message ?: "Error al cargar estadísticas")
                            _loading.postValue(false)
                        }
                    )
                } ?: run {
                    _error.postValue("Usuario no autenticado")
                    _loading.postValue(false)
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error desconocido")
                _loading.postValue(false)
            }
        }
    }

    private fun calculateStats(reciclajes: List<Reciclaje>): Map<String, Double> {
        val stats = mutableMapOf(
            "papel" to 0.0,
            "plastico" to 0.0,
            "metal" to 0.0,
            "organico" to 0.0
        )

        reciclajes
            .filter { it.estado == "canjeado" }
            .forEach { reciclaje ->
                when (reciclaje.tipo.toLowerCase()) {
                    "papel" -> stats["papel"] = stats["papel"]!! + reciclaje.cantidadKg
                    "plástico", "plastico" -> stats["plastico"] = stats["plastico"]!! + reciclaje.cantidadKg
                    "metal" -> stats["metal"] = stats["metal"]!! + reciclaje.cantidadKg
                    "orgánico", "organico" -> stats["organico"] = stats["organico"]!! + reciclaje.cantidadKg
                }
            }

        return stats
    }

    fun logout() {
        authRepository.logout()
        _logoutEvent.value = true
    }
}