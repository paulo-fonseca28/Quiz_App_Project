package com.app.quiz.ui.quiz

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.quiz.data.db.Converters
import com.app.quiz.data.db.entities.SessionEntity
import com.app.quiz.ui.quiz.components.QuestionContent

@Composable
fun QuizScreen(
    quizId: String,
    onFinish: (SessionEntity) -> Unit,
    vm: QuizVm = hiltViewModel()
) {
    val questions by vm.questions.collectAsState(initial = emptyList())

    LaunchedEffect(quizId) { vm.load(quizId) }

    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Este quiz ainda não tem perguntas no Firestore.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.load(quizId) }) { Text("Atualizar") }
            }
        }
        return
    }

    var index by remember { mutableStateOf(0) }
    var correct by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    val start = remember { SystemClock.elapsedRealtime() }

    val q = questions[index]
    val options = remember(q) { Converters().jsonToList(q.optionsJson) }

    QuestionContent(
        index = index,
        total = questions.size,
        title = q.text,
        options = options,
        selectedIndex = selected,
        onSelect = { i -> selected = i },
        onNext = if (index < questions.lastIndex) ({
            if (selected == q.correctIndex) correct++
            index++
            selected = null
        }) else null,
        onSubmit = if (index == questions.lastIndex) ({
            if (selected == q.correctIndex) correct++
            val duration = SystemClock.elapsedRealtime() - start

            val percent = (100 * correct / questions.size)

            val session = SessionEntity(
                uid = "pending",
                quizId = quizId,
                score = percent,            // ← usamos % aqui
                correct = correct,
                total = questions.size,
                durationMs = duration,
                finishedAt = System.currentTimeMillis()
            )
            onFinish(session)
        }) else null,
        timeText = null
    )
}
