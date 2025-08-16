package com.app.quiz.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun QuizTimerBar(
    totalSeconds: Int,
    onTimeUp: () -> Unit
) {
    var left by remember(totalSeconds) { mutableStateOf(totalSeconds) }

    LaunchedEffect(totalSeconds) {
        while (left > 0) {
            delay(1000)
            left--
        }
        onTimeUp()
    }

    val minutes = left / 60
    val seconds = left % 60
    LinearProgressIndicator(progress = { (left.toFloat() / totalSeconds.coerceAtLeast(1)) },
        modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))
    Text(String.format("Tempo restante: %02d:%02d", minutes, seconds), style = MaterialTheme.typography.bodySmall)
}

@Composable
fun TimeUpBlockingOverlay(
    visible: Boolean,
    message: String = "Tempo esgotado",
    onAcknowledge: () -> Unit
) {
    if (!visible) return
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = true, onClick = {})
    ) {
        Card(modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(message, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Revise rapidamente e finalize o quiz.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAcknowledge) { Text("Ok") }
            }
        }
    }
}
