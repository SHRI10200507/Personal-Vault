package com.example.personalvault.ui.screens

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
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.data.models.StockQuote
import com.example.personalvault.data.models.TrendBias
import com.example.personalvault.theme.AccentCyan
import com.example.personalvault.theme.BearishBg
import com.example.personalvault.theme.BearishRed
import com.example.personalvault.theme.BullishBg
import com.example.personalvault.theme.BullishGreen
import com.example.personalvault.theme.GoldAccent
import com.example.personalvault.theme.NeutralBg
import com.example.personalvault.theme.NeutralYellow
import com.example.personalvault.theme.TerminalCardBackground
import com.example.personalvault.theme.TerminalCardBorder
import com.example.personalvault.theme.TerminalSurface
import com.example.personalvault.theme.TextMuted
import com.example.personalvault.theme.TextPrimary
import com.example.personalvault.theme.TextSecondary
import com.example.personalvault.ui.components.CandlestickChart
import com.example.personalvault.ui.components.ScoreBar
import com.example.personalvault.ui.viewmodel.MarketViewModel

@Composable
fun StockAnalyzerScreen(
    viewModel: MarketViewModel
) {
    val stocks by viewModel.stocks.collectAsState()
    val selectedStock by viewModel.selectedStock.collectAsState()
    val watchlist by viewModel.watchlistSymbols.collectAsState()

    val stock = selectedStock ?: stocks.firstOrNull()

    if (stock == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No stock selected", color = TextMuted)
        }
        return
    }

    val isPositive = stock.percentChange >= 0
    val deltaColor = if (isPositive) BullishGreen else BearishRed
    val deltaBg = if (isPositive) BullishBg else BearishBg
    val sign = if (isPositive) "+" else ""
    val isWatchlisted = watchlist.contains(stock.symbol)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stock_analyzer_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Stock Selector Chips
        item {
            LazyRow(
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stocks, key = { it.symbol }) { s ->
                    val isCurrent = s.symbol == stock.symbol
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurrent) AccentCyan else TerminalSurface)
                            .border(1.dp, if (isCurrent) AccentCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.selectStock(s) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = s.symbol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isCurrent) Color(0xFF101318) else TextSecondary
                        )
                    }
                }
            }
        }

        // Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalCardBackground)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stock.symbol,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (stock.freshness == com.example.personalvault.data.models.DataFreshness.REAL_TIME) Color(0x2A00E676) else Color(0x2AFFB300))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${if (stock.source.isNotEmpty()) stock.source else stock.exchange} • ${stock.freshness.label}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (stock.freshness == com.example.personalvault.data.models.DataFreshness.REAL_TIME) BullishGreen else GoldAccent
                                    )
                                }
                            }
                            Text(
                                text = "${stock.company} • ${stock.sector} • Updated: ${stock.updatedAt}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleWatchlist(stock.symbol) },
                            modifier = Modifier.testTag("watchlist_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Toggle Watchlist",
                                tint = if (isWatchlisted) GoldAccent else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CURRENT PRICE", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "₹${String.format("%,.2f", stock.price)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(deltaBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$sign${String.format("%.2f", stock.change)} ($sign${String.format("%.2f", stock.percentChange)}%)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = deltaColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Day Range: ₹${stock.dayLow.toInt()} - ₹${stock.dayHigh.toInt()}",
                                fontSize = 10.5.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Interactive Candlestick & Volume Chart
        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                CandlestickChart(
                    candles = stock.candles,
                    heightDp = 240
                )
            }
        }

        // Quantitative Scores Grid
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "QUANTITATIVE FACTOR RATINGS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x2A00D2FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${stock.overallScore}/100 • ${stock.scoreLabel.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = AccentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ScoreBar(label = "Overall Composite Score", score = stock.overallScore, color = AccentCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    ScoreBar(label = "Technical Strength (Trend & MAs)", score = stock.technicalScore, color = BullishGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    ScoreBar(label = "Fundamental Quality (ROE & Balance Sheet)", score = stock.fundamentalScore, color = GoldAccent)
                    Spacer(modifier = Modifier.height(6.dp))
                    ScoreBar(label = "Momentum (RSI & Volume)", score = stock.momentumScore, color = Color(0xFF00E5FF))
                }
            }
        }

        // Multi-Timeframe Trend Matrix
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalCardBackground)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "MULTI-TIMEFRAME TREND MATRIX",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrendBox(label = "1D", bias = stock.trend1D)
                        TrendBox(label = "1W", bias = stock.trend1W)
                        TrendBox(label = "1M", bias = stock.trend1M)
                        TrendBox(label = "3M", bias = stock.trend3M)
                        TrendBox(label = "6M", bias = stock.trend6M)
                        TrendBox(label = "1Y", bias = stock.trend1Y)
                    }
                }
            }
        }

        // Quantitative Market Analysis (Replacing fake AI claim with real quant intelligence)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0D1B2A))
                    .border(1.dp, AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "QUANTITATIVE MARKET ANALYSIS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Conf: ${stock.confidencePct}% • ${stock.riskLevel}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("PRIMARY SIGNAL DRIVERS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BullishGreen, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    stock.signalDrivers.forEach { driver ->
                        Text("• $driver", fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("RISK CONSTRAINTS & SENSITIVITIES:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    stock.riskFactors.forEach { risk ->
                        Text("• $risk", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0x3300D2FF))
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Valuation Benchmark: Trading at ${stock.pe} PE, ${stock.pb} PB with ROE of ${stock.roe}% in the ${stock.sector} sector.",
                        fontSize = 10.5.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Technical Moving Averages & Momentum
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalCardBackground)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "MOVING AVERAGES & TECHNICAL GAUGES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MaItem("SMA 20", stock.sma20, stock.price)
                        MaItem("SMA 50", stock.sma50, stock.price)
                        MaItem("SMA 100", stock.sma100, stock.price)
                        MaItem("SMA 200", stock.sma200, stock.price)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MaItem("EMA 9", stock.ema9, stock.price)
                        MaItem("EMA 21", stock.ema21, stock.price)
                        MaItem("EMA 50", stock.ema50, stock.price)
                        MaItem("RSI (14)", stock.rsi, 50.0, isRsi = true)
                    }
                }
            }
        }

        // Fundamental Ratios
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalCardBackground)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "FUNDAMENTAL METRICS & FINANCIAL HEALTH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        FundamentalStat("PE RATIO", "${stock.pe}")
                        FundamentalStat("PB RATIO", "${stock.pb}")
                        FundamentalStat("ROE", "${stock.roe}%")
                        FundamentalStat("ROCE", "${stock.roce}%")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        FundamentalStat("REV GROWTH", "+${stock.revenueGrowth}%")
                        FundamentalStat("PROFIT GRW", "+${stock.profitGrowth}%")
                        FundamentalStat("DEBT/EQUITY", "${stock.debtToEquity}")
                        FundamentalStat("52W HIGH", "₹${stock.week52High.toInt()}")
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendBox(label: String, bias: TrendBias) {
    val (color, bg, arrow) = when (bias) {
        TrendBias.BULLISH -> Triple(BullishGreen, BullishBg, "▲")
        TrendBias.BEARISH -> Triple(BearishRed, BearishBg, "▼")
        TrendBias.NEUTRAL -> Triple(NeutralYellow, NeutralBg, "―")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
        Text(text = arrow, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
        Text(text = bias.name.take(4), fontSize = 8.sp, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun MaItem(label: String, maValue: Double, currentPrice: Double, isRsi: Boolean = false) {
    val isAbove = if (isRsi) maValue in 50.0..70.0 else currentPrice > maValue
    val statusColor = if (isAbove) BullishGreen else if (isRsi && maValue > 70) GoldAccent else BearishRed

    Column(modifier = Modifier.width(72.dp)) {
        Text(text = label, fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
        Text(
            text = if (isRsi) "${maValue.toInt()}" else "₹${maValue.toInt()}",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = statusColor
        )
    }
}

@Composable
private fun FundamentalStat(label: String, value: String) {
    Column(modifier = Modifier.width(72.dp)) {
        Text(text = label, fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
        Text(text = value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
    }
}
