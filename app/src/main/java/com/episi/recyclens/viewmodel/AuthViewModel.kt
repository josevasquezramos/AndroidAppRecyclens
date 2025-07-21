package com.episi.recyclens.viewmodel

import androidx.lifecycle.ViewModel
import com.episi.recyclens.model.User
import com.episi.recyclens.network.AuthenticationRepository
import com.episi.recyclens.network.Callback
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(
    private val repository: AuthenticationRepository = AuthenticationRepository()
) : ViewModel() {

    fun login(email: String, password: String, callback: Callback<User>) {
        repository.login(email, password, object : Callback<FirebaseUser> {
            override fun onSuccess(result: FirebaseUser?) {
                if (result != null) {
                    callback.onSuccess(
                        User(
                            uid = result.uid,
                            email = result.email ?: "",
                            displayName = result.displayName
                        )
                    )
                } else {
                    callback.onFailed(Exception("Usuario no encontrado"))
                }
            }

            override fun onFailed(exception: Exception) {
                callback.onFailed(exception)
            }
        })
    }

    fun register(email: String, password: String, displayName: String?, callback: Callback<User>) {
        repository.register(email, password, displayName, object : Callback<FirebaseUser> {
            override fun onSuccess(result: FirebaseUser?) {
                if (result != null) {
                    callback.onSuccess(
                        User(
                            uid = result.uid,
                            email = result.email ?: "",
                            displayName = result.displayName
                        )
                    )
                } else {
                    callback.onFailed(Exception("Registro fallido"))
                }
            }

            override fun onFailed(exception: Exception) {
                callback.onFailed(exception)
            }
        })
    }

    fun sendResetPassword(email: String, callback: Callback<String>) {
        repository.sendPasswordReset(email, object : Callback<String> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailed(exception: Exception) {
                callback.onFailed(exception)
            }
        })
    }
}
