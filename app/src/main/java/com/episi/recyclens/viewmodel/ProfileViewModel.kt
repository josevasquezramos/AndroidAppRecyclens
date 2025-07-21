package com.episi.recyclens.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.episi.recyclens.network.AuthenticationRepository

class ProfileViewModel(
    private val repository: AuthenticationRepository = AuthenticationRepository()
) : ViewModel() {

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    fun logout() {
        repository.logout()
        _logoutEvent.value = true
    }
}