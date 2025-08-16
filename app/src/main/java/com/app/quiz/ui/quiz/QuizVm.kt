package com.app.quiz.ui.quiz

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import javax.inject.Inject

data class QuestionUi(
    val id: String,
    val text: String,
    val optionsJson: String,
    val correctIndex: Int
)

@HiltViewModel
class QuizVm @Inject constructor(
    private val fs: FirebaseFirestore
) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuestionUi>>(emptyList())
    val questions: StateFlow<List<QuestionUi>> = _questions.asStateFlow()

    private var reg: ListenerRegistration? = null

    fun load(quizId: String) {
        reg?.remove()
        _questions.value = emptyList()

        val ref = fs.collection("quizzes")
            .document(quizId)
            .collection("questions")

        // ⚠️ Sem orderBy no servidor (evita falta de índice/campo); ordena localmente
        reg = ref.addSnapshotListener { snap, err ->
            if (err != null) {
                _questions.value = emptyList()
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()

            val sorted = docs.sortedWith(
                compareBy(
                    { it.getLong("order") ?: Long.MAX_VALUE },  // usa se existir
                    { it.id }                                    // fallback por id do doc (q1, q2…)
                )
            )

            val list = sorted.mapNotNull { d ->
                val text = d.getString("text") ?: return@mapNotNull null
                val options = (d.get("options") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val correctIndex = (d.getLong("correctIndex") ?: 0L).toInt()

                val optionsJson = try { JSONArray(options).toString() } catch (_: Exception) { "[]" }

                QuestionUi(
                    id = d.id,
                    text = text,
                    optionsJson = optionsJson,
                    correctIndex = correctIndex
                )
            }

            _questions.value = list
        }
    }

    override fun onCleared() {
        super.onCleared()
        reg?.remove()
    }
}
