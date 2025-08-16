package com.app.quiz.ui.quiz.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun QuestionCard(

    index: Int,
    total: Int,
    title: String,
    options: List<String>,
    selectedIndex: Int?,
    // comportamento
    onSelect: (Int) -> Unit,
    onPrev: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    // visual/estado
    revealAnswer: Boolean = false,
    correctIndex: Int? = null,
    timeText: String? = null
) {
    val gradient = Brush.horizontalGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth()) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Questão ${index + 1} de $total",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                    Spacer(Modifier.weight(1f))
                    if (timeText != null) {
                        AssistChip(onClick = {}, label = { Text(timeText) })
                    }
                }
            }

            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            Column(Modifier.padding(horizontal = 16.dp)) {
                options.forEachIndexed { i, opt ->
                    val isSelected = selectedIndex == i
                    val isCorrect = revealAnswer && correctIndex == i
                    val isWrongSelected = revealAnswer && isSelected && correctIndex != null && correctIndex != i

                    val bg = when {
                        isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                        isWrongSelected -> MaterialTheme.colorScheme.errorContainer
                        isSelected -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val border = if (isCorrect) MaterialTheme.colorScheme.tertiary
                    else if (isWrongSelected) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outlineVariant

                    Surface(
                        color = bg,
                        shape = RoundedCornerShape(14.dp),
                        tonalElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(i) },
                        border = BorderStroke(1.dp, border)
                    ) {
                        Text(
                            text = opt,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onPrev != null) {
                    OutlinedButton(onClick = onPrev) { Text("Anterior") }
                } else {
                    Spacer(Modifier.width(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onNext != null) {
                        Button(onClick = onNext, enabled = selectedIndex != null) { Text("Próxima") }
                    }
                    if (onSubmit != null) {
                        FilledTonalButton(onClick = onSubmit, enabled = selectedIndex != null) { Text("Finalizar") }
                    }
                }
            }
        }
    }
}
