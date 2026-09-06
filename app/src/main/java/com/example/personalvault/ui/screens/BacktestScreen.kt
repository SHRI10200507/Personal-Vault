package com.example.personalvault.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.data.models.BacktestTrade
import com.example.personalvault.theme.AccentCyan
import com.example.personalvault.theme.BearishBg
import com.example.personalvault.theme.BearishRed
import com.example.personalvault.theme.BullishBg
import com.example.personalvault.theme.BullishGreen
import com.example.personalvault.theme.GoldAccent
import com.example.personalvault.theme.TerminalCardBackground
import com.example.personalvault.theme.TerminalCardBorder
import com.example.personalvault.theme.TerminalSurface
import com.example.personalvault.theme.TextMuted
import com.example.personalvault.theme.TextPrimary
import com.example.personalvault.theme.TextSecondary
import com.example.personalvault.ui.viewmodel.MarketViewModel

@Composable
fun BacktestScreen(
    viewModel: MarketViewModel
) {
    val backtestResult by viewModel.backtestResult.collectAsState()
    val isBacktesting by viewModel.isBacktesting.collectAsState()
    val stocks by viewModel.stocks.collectAsState()

    val strategies = listOf(
        "EMA 20/50 + RSI",
        "52W Breakout + Volume",
        "Value Mean Reversion"
    )

    val capitalOptions = listOf(50000.0, 100000.0, 250000.0, 500000.0)
    val periodOptions = listOf("1Y", "3Y", "5Y", "MAX")

    var selectedStrategy by remember { mutableStateOf(strategies.first()) }
    var selectedStockSymbol by remember { mutableStateOf(stocks.firstOrNull()?.symbol ?: "RELIANCE") }
    var selectedCapital by remember { mutableStateOf(100000.0) }
    var selectedPeriod by remember { mutableStateOf("1Y") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("backtest_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Lab Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalCardBackground)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "QUANTITATIVE STRATEGY LAB",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Text(
                            text = "Bar-by-bar backtesting with STT taxes (0.1%), brokerage (₹20), slippage & walk-forward validation.",
                            fontSize = 10.5.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Strategy Selector
        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                Text("1. SELECT STRATEGY", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(strategies) { strat ->
                        val isSelected = strat == selectedStrategy
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AccentCyan else TerminalSurface)
                                .border(1.dp, if (isSelected) AccentCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedStrategy = strat
                                    viewModel.runBacktest(strat, selectedCapital, selectedStockSymbol, selectedPeriod)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = strat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Color(0xFF101318) else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Stock Selector
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                Text("2. SELECT ASSET FOR SIMULATION", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(stocks) { s ->
                        val isSelected = s.symbol == selectedStockSymbol
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Color(0x2A00D2FF) else TerminalSurface)
                                .border(1.dp, if (isSelected) AccentCyan else TerminalCardBorder, RoundedCornerShape(4.dp))
                                .clickable {
                                    selectedStockSymbol = s.symbol
                                    viewModel.runBacktest(selectedStrategy, selectedCapital, s.symbol, selectedPeriod)
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = s.symbol,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AccentCyan else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Capital & Period Options
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("3. INITIAL CAPITAL", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Text("4. DATE RANGE", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Capital options
                    Row(modifier = Modifier.weight(1.2f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        capitalOptions.forEach { cap ->
                            val isSelected = selectedCapital == cap
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) GoldAccent else TerminalSurface)
                                    .border(1.dp, if (isSelected) GoldAccent else TerminalCardBorder, RoundedCornerShape(4.dp))
                                    .clickable {
                                        selectedCapital = cap
                                        viewModel.runBacktest(selectedStrategy, cap, selectedStockSymbol, selectedPeriod)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹${(cap / 1000).toInt()}K",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Color(0xFF101318) else TextSecondary
                                )
                            }
                        }
                    }

                    // Period options
                    Row(modifier = Modifier.weight(0.8f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        periodOptions.forEach { p ->
                            val isSelected = selectedPeriod == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) AccentCyan else TerminalSurface)
                                    .border(1.dp, if (isSelected) AccentCyan else TerminalCardBorder, RoundedCornerShape(4.dp))
                                    .clickable {
                                        selectedPeriod = p
                                        viewModel.runBacktest(selectedStrategy, selectedCapital, selectedStockSymbol, p)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Color(0xFF101318) else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isBacktesting) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fetching Historical Candles & Simulating...", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    }
                }
            }
        }

        val res = backtestResult
        if (res != null && !isBacktesting) {
            // Performance Metrics Grid
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TerminalCardBackground)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SIMULATION OUTCOME ($selectedStockSymbol • $selectedPeriod)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            val isNetPos = res.totalReturnPercent >= 0
                            Text(
                                text = "CAGR: ${if (isNetPos) "+" else ""}${res.cagrPercent}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isNetPos) BullishGreen else BearishRed
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MetricCell("NET RETURN", "${if (res.totalReturnPercent >= 0) "+" else ""}${res.totalReturnPercent}%", isPos = res.totalReturnPercent >= 0)
                            MetricCell("WIN RATE", "${res.winRatePercent}%", isPos = res.winRatePercent >= 50.0)
                            MetricCell("PROFIT FACTOR", "${res.profitFactor}", isPos = res.profitFactor >= 1.5)
                            MetricCell("MAX DRAWDOWN", "-${res.maxDrawdownPercent}%", isPos = false)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MetricCell("SHARPE RATIO", "${res.sharpeRatio}", isPos = res.sharpeRatio >= 1.0)
                            MetricCell("SORTINO RATIO", "${res.sortinoRatio}", isPos = res.sortinoRatio >= 1.2)
                            MetricCell("TOTAL TRADES", "${res.totalTrades}", isPos = true)
                            MetricCell("FEES & TAXES", "₹${res.totalCostsPaid.toInt()}", isPos = false)
                        }
                    }
                }
            }

            // Walk-Forward Analysis Card
            res.walkForward?.let { wf ->
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1800D2FF))
                            .border(1.dp, Color(0x4000D2FF), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WALK-FORWARD OVERFITTING TEST", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = AccentCyan)
                                }
                                val riskColor = when (wf.overfittingRisk) {
                                    "LOW" -> BullishGreen
                                    "MODERATE" -> GoldAccent
                                    else -> BearishRed
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(riskColor.copy(alpha = 0.2f))
                                        .border(1.dp, riskColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("RISK: ${wf.overfittingRisk}", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = riskColor)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("In-Sample (70%)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                    Text("${if (wf.inSampleReturnPercent >= 0) "+" else ""}${wf.inSampleReturnPercent}%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (wf.inSampleReturnPercent >= 0) BullishGreen else BearishRed)
                                    Text("Win: ${wf.inSampleWinRate}%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Out-of-Sample (30%)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                    Text("${if (wf.outOfSampleReturnPercent >= 0) "+" else ""}${wf.outOfSampleReturnPercent}%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (wf.outOfSampleReturnPercent >= 0) BullishGreen else BearishRed)
                                    Text("Win: ${wf.outOfSampleWinRate}%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Consistency Ratio", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                    Text("${wf.consistencyRatio}×", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                    Text("Train/Test Decay", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            // Equity Curve Canvas
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Text(
                        text = "EQUITY CURVE PROGRESSION (₹)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    EquityCurveCanvas(
                        points = res.equityCurve.map { it.second },
                        initialCapital = res.initialCapital
                    )
                }
            }

            // Trade-by-trade ledger
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRADE EXECUTION LEDGER (${res.trades.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Text(
                            text = "Net of STT & Fees",
                            fontSize = 9.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            if (res.trades.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No trades triggered under current rule filters.", fontSize = 11.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            } else {
                items(res.trades) { trade ->
                    BacktestTradeRow(trade)
                }
            }
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String, isPos: Boolean) {
    Column {
        Text(text = label, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (label == "FEES & TAXES") TextSecondary else if (isPos) BullishGreen else if (label == "MAX DRAWDOWN") BearishRed else TextPrimary
        )
    }
}

@Composable
private fun EquityCurveCanvas(points: List<Double>, initialCapital: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalCardBackground)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        if (points.isEmpty()) {
            Text("Insufficient data to render curve.", fontSize = 10.sp, color = TextMuted, modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val minVal = minOf(points.minOrNull() ?: initialCapital, initialCapital * 0.95)
            val maxVal = maxOf(points.maxOrNull() ?: initialCapital, initialCapital * 1.05)
            val range = if (maxVal > minVal) (maxVal - minVal) else 1.0

            // Baseline at initialCapital
            val baselineY = h - ((initialCapital - minVal) / range * h).toFloat()
            drawLine(
                color = Color(0x33FFFFFF),
                start = Offset(0f, baselineY),
                end = Offset(w, baselineY),
                strokeWidth = 1.dp.toPx()
            )

            val path = Path()
            points.forEachIndexed { i, v ->
                val x = (i.toFloat() / (points.size - 1).coerceAtLeast(1)) * w
                val y = h - ((v - minVal) / range * h).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            val isGreen = (points.lastOrNull() ?: initialCapital) >= initialCapital
            drawPath(
                path = path,
                color = if (isGreen) BullishGreen else BearishRed,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun BacktestTradeRow(trade: BacktestTrade) {
    val isSell = trade.type == "SELL"
    val isProfit = trade.pnlPercent > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(TerminalCardBackground)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSell) (if (isProfit) BullishBg else BearishBg) else Color(0x2200D2FF))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = trade.type,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSell) (if (isProfit) BullishGreen else BearishRed) else AccentCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${trade.symbol} @ ₹${trade.price}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = "${trade.date} • ${trade.reason}",
                        fontSize = 9.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (isSell) {
                    Text(
                        text = "${if (isProfit) "+" else ""}${trade.pnlPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isProfit) BullishGreen else BearishRed
                    )
                }
                Text(
                    text = "Cost: ₹${trade.transactionCost.toInt()}",
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
