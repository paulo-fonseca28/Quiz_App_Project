package com.app.quiz.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val nickname: String? = null,   // <<< novo
    val email: String? = null,
    val photoUrl: String? = null
)

class ProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val fs: FirebaseFirestore,
    private val prefs: DataStore<Preferences>
) {
    private val K_UID = stringPreferencesKey("uid")
    private val K_DISPLAY = stringPreferencesKey("displayName")
    private val K_NICK = stringPreferencesKey("nickname")
    private val K_EMAIL = stringPreferencesKey("email")
    private val K_PHOTO = stringPreferencesKey("photoUrl")

    fun observeLocal(): Flow<UserProfile?> =
        prefs.data.map { p ->
            val uid = p[K_UID] ?: return@map null
            UserProfile(
                uid = uid,
                displayName = p[K_DISPLAY] ?: "",
                nickname = p[K_NICK],
                email = p[K_EMAIL],
                photoUrl = p[K_PHOTO]
            )
        }

    suspend fun onLoginEnsureAndSync() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email
        val doc = fs.collection("users").document(uid).collection("profile").document("main")
        val snap = doc.get().await()
        if (!snap.exists()) {
            doc.set(
                mapOf(
                    "uid" to uid,
                    "displayName" to (email ?: "Usuário"),
                    "email" to email,
                    "photoUrl" to null
                ),
                SetOptions.merge()
            ).await()
        }
        val d = doc.get().await()
        saveLocal(
            UserProfile(
                uid = uid,
                displayName = d.getString("displayName") ?: (email ?: "Usuário"),
                nickname = d.getString("nickname"),
                email = d.getString("email") ?: email,
                photoUrl = d.getString("photoUrl")
            )
        )
    }

    fun sanitizeNickname(raw: String): String {
        val s = raw.trim().lowercase()
        require(s.length in 3..20) { "Apelido deve ter entre 3 e 20 caracteres." }
        require(s.all { it.isLetterOrDigit() || it in "._-" }) { "Use apenas letras, números, ., _ ou -." }
        return s
    }

    suspend fun isNicknameAvailable(nick: String): Boolean {
        val id = sanitizeNickname(nick)
        val ref = fs.collection("usernames").document(id).get().await()
        return !ref.exists()
    }

    suspend fun getPreferredName(): String {
        // nickname > displayName > email > uid
        val p = prefs.data.first()
        return p[K_NICK] ?: p[K_DISPLAY] ?: p[K_EMAIL] ?: (auth.currentUser?.uid ?: "usuario")
    }

    suspend fun claimNicknameAndCreateProfile(uid: String, email: String?, nickname: String, displayName: String?) {
        val id = sanitizeNickname(nickname)
        fs.runTransaction { tr ->
            val unameRef = fs.collection("usernames").document(id)
            if (tr.get(unameRef).exists()) throw IllegalStateException("Apelido já está em uso.")

            tr.set(unameRef, mapOf("uid" to uid, "nickname" to id))

            val profRef = fs.collection("users").document(uid).collection("profile").document("main")
            tr.set(
                profRef,
                mapOf(
                    "uid" to uid,
                    "displayName" to (displayName ?: email ?: "Usuário"),
                    "nickname" to id,
                    "email" to email,
                    "photoUrl" to null
                ),
                SetOptions.merge()
            )
            null
        }.await()

        saveLocal(
            UserProfile(
                uid = uid,
                displayName = displayName ?: email ?: "Usuário",
                nickname = id,
                email = email,
                photoUrl = null
            )
        )
    }

    suspend fun save(profile: UserProfile) {
        val uid = auth.currentUser?.uid ?: return
        val doc = fs.collection("users").document(uid).collection("profile").document("main")
        doc.set(
            mapOf(
                "uid" to uid,
                "displayName" to profile.displayName,
                "nickname" to profile.nickname,
                "email" to profile.email,
                "photoUrl" to profile.photoUrl
            ),
            SetOptions.merge()
        ).await()
        saveLocal(profile.copy(uid = uid))
    }

    private suspend fun saveLocal(p: UserProfile) {
        prefs.edit { e ->
            e[K_UID] = p.uid
            e[K_DISPLAY] = p.displayName
            if (p.nickname != null) e[K_NICK] = p.nickname else e.remove(K_NICK)
            if (p.email != null) e[K_EMAIL] = p.email else e.remove(K_EMAIL)
            if (p.photoUrl != null) e[K_PHOTO] = p.photoUrl else e.remove(K_PHOTO)
        }
    }
}
