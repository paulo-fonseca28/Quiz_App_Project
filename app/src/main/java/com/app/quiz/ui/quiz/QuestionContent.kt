package com.app.quiz.ui.quiz.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun QuestionContent(
    index: Int,
    total: Int,
    title: String,
    options: List<String>,
    selectedIndex: Int?,

    onSelect: (Int) -> Unit,
    onPrev: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    timeText: String? = null
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header slim com gradiente e progresso
        val gradient = Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        )
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(gradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(32.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Questão ${index + 1} de $total",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                        )
                        Spacer(Modifier.weight(1f))
                        if (timeText != null) AssistChip(onClick = {}, label = { Text(timeText) })
                    }
                }
                Text(
                    title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { i, opt ->
                val selected = selectedIndex == i
                val bg = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
                val stroke = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant

                Surface(
                    color = bg,
                    shape = RoundedCornerShape(14.dp),
                    tonalElevation = if (selected) 1.dp else 0.dp,
                    border = BorderStroke(1.dp, stroke),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(i) }
                ) {
                    Text(
                        opt,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 4, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onPrev != null) OutlinedButton(onClick = onPrev) { Text("Anterior") } else Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onNext != null) Button(onClick = onNext, enabled = selectedIndex != null) { Text("Próxima") }
                if (onSubmit != null) FilledTonalButton(onClick = onSubmit, enabled = selectedIndex != null) { Text("Finalizar") }
            }
        }
    }
}
