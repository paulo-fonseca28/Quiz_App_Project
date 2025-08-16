package com.app.quiz.ui.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.quiz.data.model.RankingEntry

@Composable
fun RankingScreen(vm: RankingVm = hiltViewModel()) {
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
                Icon(Icons.Default.EmojiEvents, contentDescription = "Ranking", tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Ranking",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> ErrorState(state.error ?: "Erro", onRetry = vm::retry)
            state.items.isEmpty() -> EmptyState(onRetry = vm::retry)
            else -> RankingList(state.items)
        }
    }
}

@Composable
private fun RankingList(items: List<RankingEntry>) {
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(items) { index, item -> RankingRow(item, highlight = index < 3) }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun RankingRow(entry: RankingEntry, highlight: Boolean) {
    val cardColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("#${entry.rank}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.width(52.dp))

            val initials = remember(entry.displayName) {
                entry.displayName.trim().split("\\s+".toRegex()).take(2)
                    .map { it.first().uppercase() }.joinToString("").ifEmpty { "U" }
            }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) { Text(initials, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(entry.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                val subtitle = if (entry.quizzes > 0) "${entry.quizzes} quizzes" else "—"
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.width(8.dp))
            AssistChip(onClick = {}, label = { Text("${entry.score} pts") })
        }
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sem jogadores no ranking ainda", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Jogue e acumule pontos para aparecer aqui!", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Atualizar") }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Erro ao carregar o ranking", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Tentar novamente") }
        }
    }
}
