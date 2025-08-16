package com.app.quiz.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Logged : AuthState()
    data class Error(val msg: String) : AuthState()
}

@HiltViewModel
class AuthVm @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state = _state.asStateFlow()

    fun clearError() { if (_state.value is AuthState.Error) _state.value = AuthState.Idle }

    fun login(email: String, pass: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        try {
            auth.signInWithEmailAndPassword(email.trim(), pass).await()
            _state.value = AuthState.Logged
        } catch (e: Exception) {
            val msg = when (e) {
                is FirebaseAuthInvalidUserException -> "Usuário não encontrado."
                is FirebaseAuthInvalidCredentialsException -> "Credenciais inválidas."
                else -> e.message ?: "Erro ao entrar."
            }
            _state.value = AuthState.Error(msg)
        }
    }

    fun signup(email: String, pass: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        try {
            auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            _state.value = AuthState.Logged
        } catch (e: Exception) {
            val msg = when (e) {
                is FirebaseAuthUserCollisionException -> "E-mail já cadastrado. Faça login."
                else -> e.message ?: "Erro ao cadastrar."
            }
            _state.value = AuthState.Error(msg)
        }
    }

    fun signupWithNickname(nicknameRaw: String, email: String, pass: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        try {
            val nickname = nicknameRaw.trim()
            val lower = normalizeUsername(nickname)
            validateUsername(lower)

            auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("UID ausente")

            firestore.runTransaction { tx ->
                val usernameRef = firestore.collection("usernames").document(lower)
                val usernameSnap = tx.get(usernameRef)
                if (usernameSnap.exists()) {
                    throw IllegalStateException("Este nickname já está em uso.")
                }
                tx.set(usernameRef, mapOf("uid" to uid, "createdAt" to FieldValue.serverTimestamp()))

                val userRef = firestore.collection("users").document(uid)
                tx.set(
                    userRef,
                    mapOf(
                        "uid" to uid,
                        "displayName" to nickname,
                        "username" to nickname,
                        "usernameLower" to lower,
                        "createdAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }.await()

            val req = UserProfileChangeRequest.Builder()
                .setDisplayName(nickname)
                .build()
            auth.currentUser?.updateProfile(req)?.await()

            _state.value = AuthState.Logged
        } catch (e: Exception) {
            try { auth.currentUser?.delete()?.await() } catch (_: Exception) {}
            _state.value = AuthState.Error(
                when (e) {
                    is IllegalArgumentException, is IllegalStateException -> e.message ?: "Nickname inválido."
                    is FirebaseAuthUserCollisionException -> "E-mail já cadastrado. Faça login."
                    else -> e.message ?: "Erro ao cadastrar."
                }
            )
        }
    }

    private fun normalizeUsername(raw: String): String =
        raw.lowercase().replace("\\s+".toRegex(), "")

    private fun validateUsername(usernameLower: String) {
        val regex = Regex("^[a-z0-9._-]{3,20}$")
        require(usernameLower.matches(regex)) {
            "O nickname deve ter 3–20 caracteres e usar apenas letras, números, '.', '_' ou '-'."
        }
        val reserved = setOf("admin", "root", "support", "moderator", "null", "system")
        require(usernameLower !in reserved) { "Este nickname não está disponível." }
    }
}
