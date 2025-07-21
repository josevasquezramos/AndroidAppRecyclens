package com.episi.recyclens.network

import com.episi.recyclens.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthenticationRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun login(email: String, password: String, callback: Callback<FirebaseUser>) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                callback.onSuccess(result.user)
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun register(email: String, password: String, displayName: String?, callback: Callback<FirebaseUser>) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    user.updateProfile(profileUpdates)
                        .addOnSuccessListener {
                            // Crear documento en Firestore
                            val newUser = User(
                                uid = user.uid,
                                email = user.email ?: "",
                                displayName = displayName
                            )
                            firestore.collection("usuarios")
                                .document(user.uid)
                                .set(newUser)
                                .addOnSuccessListener {
                                    callback.onSuccess(user)
                                }
                                .addOnFailureListener { firestoreException ->
                                    callback.onFailed(firestoreException)
                                }
                        }
                        .addOnFailureListener { exception ->
                            callback.onFailed(exception)
                        }
                } else {
                    callback.onFailed(Exception("Error al crear usuario"))
                }
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let {
            User(
                uid = it.uid,
                email = it.email ?: "",
                displayName = it.displayName
            )
        }
    }

    fun sendPasswordReset(email: String, callback: Callback<String>) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                callback.onSuccess("Correo enviado")
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
