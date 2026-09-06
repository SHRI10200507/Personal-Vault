package com.example.personalvault.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.data.models.CandleBar
import com.example.personalvault.theme.AccentCyan
import com.example.personalvault.theme.BearishRed
import com.example.personalvault.theme.BullishGreen
import com.example.personalvault.theme.GoldAccent
import com.example.personalvault.theme.TerminalCardBorder
import com.example.personalvault.theme.TerminalCardBackground
import com.example.personalvault.theme.TerminalSurface
import com.example.personalvault.theme.TextMuted
import com.example.personalvault.theme.TextPrimary
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<CandleBar>,
    modifier: Modifier = Modifier,
    heightDp: Int = 260
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .background(TerminalSurface),
            contentAlignment = Alignment.Center
        ) {
            Text("No candlestick data available", color = TextMuted, fontSize = 12.sp)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableStateOf<Float?>(null) }

    val activeCandles = candles.takeLast(45) // Focus on visible window
    val selectedCandle = selectedIndex?.let { if (it in activeCandles.indices) activeCandles[it] else activeCandles.last() } ?: activeCandles.last()

    val minPrice = activeCandles.minOf { it.low } * 0.995
    val maxPrice = activeCandles.maxOf { it.high } * 1.005
    val priceRange = max(0.01, maxPrice - minPrice)

    val maxVolume = activeCandles.maxOf { it.volume }.toDouble().coerceAtLeast(100.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalCardBackground)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        // Crosshair HUD Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DATE: ${selectedCandle.dateStr}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "O: ₹${selectedCandle.open}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = "H: ₹${selectedCandle.high}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BullishGreen
                    )
                    Text(
                        text = "L: ₹${selectedCandle.low}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BearishRed
                    )
                    Text(
                        text = "C: ₹${selectedCandle.close}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (selectedCandle.isBullish) BullishGreen else BearishRed
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "VOL: ${selectedCandle.volume / 1000}K",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AccentCyan
                )
                val chg = selectedCandle.close - selectedCandle.open
                val chgPct = (chg / selectedCandle.open) * 100.0
                Text(
                    text = "${if (chg >= 0) "+" else ""}${String.format("%.2f", chgPct)}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (chg >= 0) BullishGreen else BearishRed
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).background(AccentCyan))
            Spacer(modifier = Modifier.width(3.dp))
            Text("SMA 20", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(6.dp).background(GoldAccent))
            Spacer(modifier = Modifier.width(3.dp))
            Text("SMA 50", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .pointerInput(activeCandles) {
                    detectTapGestures(
                        onTap = { offset ->
                            val candleWidth = size.width / activeCandles.size
                            val idx = (offset.x / candleWidth).toInt().coerceIn(0, activeCandles.size - 1)
                            selectedIndex = idx
                            touchX = offset.x
                        }
                    )
                }
                .pointerInput(activeCandles) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            val candleWidth = size.width / activeCandles.size
                            val idx = (change.position.x / candleWidth).toInt().coerceIn(0, activeCandles.size - 1)
                            selectedIndex = idx
                            touchX = change.position.x
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val priceHeight = canvasHeight * 0.75f
                val volumeHeight = canvasHeight * 0.22f
                val volumeTop = canvasHeight * 0.78f

                val count = activeCandles.size
                val slotWidth = canvasWidth / count
                val candleWidth = (slotWidth * 0.65f).coerceAtLeast(2f)

                // Draw Grid Lines
                val gridLines = 4
                for (g in 0..gridLines) {
                    val y = (priceHeight / gridLines) * g
                    drawLine(
                        color = Color(0x1A00D2FF),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Draw Candlesticks & Volume
                for (i in 0 until count) {
                    val bar = activeCandles[i]
                    val x = (i * slotWidth) + (slotWidth / 2f)

                    // Price Y coordinates
                    val openY = priceHeight - ((bar.open - minPrice) / priceRange * priceHeight).toFloat()
                    val closeY = priceHeight - ((bar.close - minPrice) / priceRange * priceHeight).toFloat()
                    val highY = priceHeight - ((bar.high - minPrice) / priceRange * priceHeight).toFloat()
                    val lowY = priceHeight - ((bar.low - minPrice) / priceRange * priceHeight).toFloat()

                    val color = if (bar.isBullish) BullishGreen else BearishRed

                    // High/Low Wick
                    drawLine(
                        color = color,
                        start = Offset(x, highY),
                        end = Offset(x, lowY),
                        strokeWidth = 1.5f
                    )

                    // Candle Body
                    val bodyTop = min(openY, closeY)
                    val bodyBottom = max(openY, closeY)
                    val bodyHeight = max(2f, bodyBottom - bodyTop)

                    drawRect(
                        color = color,
                        topLeft = Offset(x - candleWidth / 2f, bodyTop),
                        size = Size(candleWidth, bodyHeight)
                    )

                    // Volume Bar
                    val volPct = (bar.volume.toDouble() / maxVolume).toFloat().coerceIn(0.05f, 1f)
                    val volBarHeight = volumeHeight * volPct
                    val volBarTop = canvasHeight - volBarHeight

                    drawRect(
                        color = color.copy(alpha = 0.45f),
                        topLeft = Offset(x - candleWidth / 2f, volBarTop),
                        size = Size(candleWidth, volBarHeight)
                    )
                }

                // Draw SMA 20 Line
                val sma20Path = Path()
                var sma20Started = false
                for (i in 0 until count) {
                    if (i >= 19) {
                        val subCloses = activeCandles.subList(i - 19, i + 1).map { it.close }
                        val avg = subCloses.average()
                        val x = (i * slotWidth) + (slotWidth / 2f)
                        val y = priceHeight - ((avg - minPrice) / priceRange * priceHeight).toFloat()
                        if (!sma20Started) {
                            sma20Path.moveTo(x, y)
                            sma20Started = true
                        } else {
                            sma20Path.lineTo(x, y)
                        }
                    }
                }
                if (sma20Started) {
                    drawPath(
                        path = sma20Path,
                        color = AccentCyan.copy(alpha = 0.85f),
                        style = Stroke(width = 2f)
                    )
                }

                // Crosshair cursor
                touchX?.let { tx ->
                    if (tx in 0f..canvasWidth) {
                        drawLine(
                            color = Color(0x77FFFFFF),
                            start = Offset(tx, 0f),
                            end = Offset(tx, canvasHeight),
                            strokeWidth = 1f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }
                }
            }
        }
    }
}
