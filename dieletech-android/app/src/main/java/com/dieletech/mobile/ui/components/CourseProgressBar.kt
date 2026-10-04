package com.dieletech.mobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HU-08: barra de progreso visible del curso.
 * `percent` en [0f, 1f]. Anima suavemente entre valores.
 */
@Composable
fun CourseProgressBar(
    percent: Float,
    completed: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progress"
    )
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Progreso del curso",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$completed / $total lecciones · ${(animated * 100).toInt()}%",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { animated },
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(10.dp)
        )
    }
}
