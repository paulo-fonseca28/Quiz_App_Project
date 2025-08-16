package com.app.quiz.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.quiz.data.model.HistoryItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(vm: HistoryVm = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize()) {
        val gradient = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(top = 24.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Histórico",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> HistoryError(message = state.error ?: "Erro ao carregar", onRetry = vm::retry)
            state.items.isEmpty() -> HistoryEmpty(onRetry = vm::retry)
            else -> HistoryList(state.items)
        }
    }
}

@Composable
private fun HistoryList(items: List<HistoryItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { it ->
            HistoryCard(it)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun HistoryCard(item: HistoryItem) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val whenText = item.finishedAt?.toDate()?.let { sdf.format(it) } ?: "—"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(item.quizTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val scoreText = when {
                    item.correct != null && item.total != null -> "${item.correct}/${item.total}"
                    item.score != null -> "${item.score} pts"
                    else -> "—"
                }
                AssistChip(onClick = {}, label = { Text(scoreText) })
                val dur = item.durationSeconds?.takeIf { it > 0 }?.let { "${it / 60}m ${it % 60}s" } ?: ""
                Text(whenText + if (dur.isNotEmpty()) "  •  $dur" else "", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HistoryEmpty(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Você ainda não concluiu nenhum quiz", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Faça um quiz para aparecer aqui!", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Atualizar") }
        }
    }
}

@Composable
private fun HistoryError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Erro ao carregar o histórico", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Tentar novamente") }
        }
    }
}
