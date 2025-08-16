@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.app.quiz.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LibraryBooks
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
import com.app.quiz.data.model.QuizItem

@Composable
fun HomeScreen(
    onOpenQuiz: (id: String, timeLimitSeconds: Int?) -> Unit,
    vm: HomeVm = hiltViewModel()
) {
    val ui by vm.state.collectAsState()

    Scaffold(
        topBar = {
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
                    .padding(vertical = 15.dp, horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LibraryBooks, null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Quizzes",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White, fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { vm.refresh() }) { Text("Atualizar", color = Color.White) }
                }
            }
        }
    ) { p ->
        when {
            ui.loading -> Box(Modifier.padding(p).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.error != null -> ErrorState(ui.error ?: "Erro", onRetry = vm::refresh)
            ui.items.isEmpty() -> EmptyState(onRetry = vm::refresh, modifier = Modifier.padding(p))
            else -> LazyColumn(
                modifier = Modifier.padding(p).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.items) { q ->
                    QuizListTile(q) { onOpenQuiz(q.id, q.timeLimitSeconds) }
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }
    }
}

/** Card compacto, neutro (sem rosa), estilo “list tile” */
@Composable
private fun QuizListTile(
    q: QuizItem,
    onClick: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surfaceVariant
    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = q.isActive) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Título + descrição
            Column(Modifier.weight(1f)) {
                Text(
                    q.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    q.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                // Linha compacta com nível e tempo
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(q.difficulty) })
                    val tl = q.timeLimitSeconds?.takeIf { it > 0 }?.let { "${it / 60} min" } ?: "Sem tempo"
                    AssistChip(onClick = {}, label = { Text(tl) })
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nenhum quiz disponível agora", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) { Text("Atualizar") }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Erro ao carregar quizzes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Tentar novamente") }
        }
    }
}
