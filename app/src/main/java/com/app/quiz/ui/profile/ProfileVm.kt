package com.app.quiz.ui.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.google.firebase.firestore.ListenerRegistration

data class ProfileUiState(
    val loading: Boolean = true,
    val nickname: String? = null,   // sempre do cadastro (users/username/usernames)
    val email: String? = null,
    val uid: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileVm @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private var regUsers: ListenerRegistration? = null
    private var regUsernames: ListenerRegistration? = null

    init { observe() }

    private fun observe() {
        val user = auth.currentUser
        val uid = user?.uid
        val email = user?.email

        if (uid == null) {
            _state.value = ProfileUiState(loading = false, error = "Não autenticado")
            return
        }
        _state.value = _state.value.copy(loading = true, uid = uid, email = email)

        var fromUsers: String? = null
        var fromUsernames: String? = null

        fun emit() {
            val preferred = fromUsers ?: fromUsernames ?: user?.displayName ?: email?.substringBefore('@')
            _state.value = _state.value.copy(
                loading = false,
                nickname = preferred?.removePrefix("@"),
                error = null
            )
        }

        regUsers?.remove()
        regUsernames?.remove()

        regUsers = firestore.collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                fromUsers = snap?.getString("displayName")
                    ?: snap?.getString("username")
                            ?: snap?.getString("nickname")
                emit()
            }

        regUsernames = firestore.collection("usernames")
            .whereEqualTo("uid", uid)
            .limit(1)
            .addSnapshotListener { snap, _ ->
                val doc = snap?.documents?.firstOrNull()
                fromUsernames = doc?.id
                emit()
            }
    }

    fun logout() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        regUsers?.remove()
        regUsernames?.remove()
    }
}
