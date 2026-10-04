package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dieletech.mobile.data.model.Course

@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Brush.horizontalGradient(colors = getTechColors(course.technology))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(getTechEmoji(course.technology), fontSize = 40.sp)
                    Text(course.technology, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(course.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(course.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Chip(text = course.level)
                        Spacer(modifier = Modifier.width(8.dp))
                        Chip(text = "${course.duration}h")
                    }
                    Text(
                        "$${String.format("%,.0f", course.price)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Text("Ver Detalles")
                }
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
    }
}

fun getTechEmoji(tech: String): String = when (tech) {
    "Python" -> "🐍"
    "HTML/CSS/JS" -> "🌐"
    "Git" -> "📚"
    "Java" -> "☕"
    "React" -> "⚛️"
    "MySQL" -> "🗄️"
    else -> "💻"
}

fun getTechColors(tech: String): List<Color> = when (tech) {
    "Python" -> listOf(Color(0xFF3B82F6), Color(0xFFFDBA74))
    "HTML/CSS/JS" -> listOf(Color(0xFFF97316), Color(0xFFFACC15))
    "Git" -> listOf(Color(0xFFDC2626), Color(0xFFF97316))
    "Java" -> listOf(Color(0xFFEA580C), Color(0xFFDC2626))
    "React" -> listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    "MySQL" -> listOf(Color(0xFF0891B2), Color(0xFF06B6D4))
    else -> listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
}
