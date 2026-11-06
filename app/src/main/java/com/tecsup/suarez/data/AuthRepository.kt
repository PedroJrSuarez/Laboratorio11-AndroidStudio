package com.tecsup.suarez.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await().user!!
    }.mapError()

    suspend fun register(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email.trim(), password).await().user!!
    }.mapError()

    fun logout() = auth.signOut()

    /** Traduce los errores de Firebase a mensajes legibles. */
    private fun <T> Result<T>.mapError(): Result<T> = recoverCatching { e ->
        val msg = when ((e as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Correo o contraseña incorrectos"
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con ese correo"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya está registrado"
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
            else -> e.localizedMessage ?: "Error desconocido"
        }
        throw Exception(msg)
    }
}
