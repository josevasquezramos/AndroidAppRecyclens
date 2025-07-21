package com.episi.recyclens.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.episi.recyclens.network.FirebaseAuthentication

class HomeViewModel(
    repository: FirebaseAuthentication = FirebaseAuthentication()
) : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    init {
        val user = repository.getCurrentUser()
        val name = user?.displayName
        val email = user?.email

        _text.value = "¡Bienvenido, $name!\nEmail: $email"
    }
}
