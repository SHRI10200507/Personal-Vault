package com.example.personalvault.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.personalvault.theme.BearishRed
import com.example.personalvault.theme.BullishGreen

@Composable
fun MiniChart(
    points: List<Double>,
    modifier: Modifier = Modifier,
    isPositive: Boolean = true
) {
    if (points.isEmpty()) return

    val lineColor = if (isPositive) BullishGreen else BearishRed
    val gradientColor = if (isPositive) Color(0x3300E676) else Color(0x33FF5252)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val minVal = points.minOrNull() ?: 0.0
        val maxVal = points.maxOrNull() ?: 1.0
        val range = if (maxVal - minVal > 0.0001) maxVal - minVal else 1.0

        val stepX = width / (points.size - 1).coerceAtLeast(1)
        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = 1.0 - ((value - minVal) / range)
            val y = (normalizedY * (height * 0.8f) + (height * 0.1f)).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw fill gradient under line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientColor, Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw stroke line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // Draw dot at last point
        val lastVal = points.last()
        val lastX = (points.size - 1) * stepX
        val lastY = ((1.0 - ((lastVal - minVal) / range)) * (height * 0.8f) + (height * 0.1f)).toFloat()
        drawCircle(
            color = lineColor,
            radius = 3.5f,
            center = Offset(lastX, lastY)
        )
    }
}
