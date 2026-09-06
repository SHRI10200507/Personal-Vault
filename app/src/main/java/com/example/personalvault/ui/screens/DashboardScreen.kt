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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.personalvault.data.models.GlobalCorrelationItem
import com.example.personalvault.data.models.MarketCategory
import com.example.personalvault.data.models.MarketRegimeItem
import com.example.personalvault.data.models.StockQuote
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
import com.example.personalvault.ui.components.KpiCard
import com.example.personalvault.ui.components.StockRowItem
import com.example.personalvault.ui.viewmodel.MarketViewModel

@Composable
fun DashboardScreen(
    viewModel: MarketViewModel,
    onNavigateToAnalyzer: (StockQuote) -> Unit,
    onNavigateToRadar: () -> Unit,
    onNavigateToPortfolio: () -> Unit
) {
    val stocks by viewModel.stocks.collectAsState()
    val indiaIndices by viewModel.indiaIndices.collectAsState()
    val globalMarkets by viewModel.globalMarkets.collectAsState()
    val marketRegimes by viewModel.marketRegimes.collectAsState()
    val globalCorrelations by viewModel.globalCorrelations.collectAsState()
    val portfolioSummary by viewModel.portfolioSummary.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    val bullishCount = stocks.count { it.percentChange > 0 }
    val bearishCount = stocks.count { it.percentChange < 0 }
    val near52WHigh = stocks.count { it.position52W >= 85 }
    val near52WLow = stocks.count { it.position52W <= 20 }

    // Macro indicators: Forex, Commodities, Crypto
    val macroItems = globalMarkets.filter {
        it.category == MarketCategory.FOREX || it.category == MarketCategory.COMMODITY || it.category == MarketCategory.CRYPTO
    }

    // World Indices
    val worldIndices = globalMarkets.filter { it.category == MarketCategory.INDEX }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. INDIA BENCHMARK INDICES
        item {
            Column(modifier = Modifier.padding(top = 12.dp, start = 14.dp, end = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🇮🇳 INDIA MARKET PULSE (NSE / BSE)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    val pulseFreshness = indiaIndices.firstOrNull()?.freshness ?: com.example.personalvault.data.models.DataFreshness.REAL_TIME
                    Text(
                        text = pulseFreshness.label,
                        fontSize = 9.sp,
                        color = if (pulseFreshness == com.example.personalvault.data.models.DataFreshness.REAL_TIME) BullishGreen else GoldAccent,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(indiaIndices, key = { it.name }) { idx ->
                    val isPositive = idx.percentChange >= 0
                    val sign = if (isPositive) "+" else ""
                    KpiCard(
                        title = idx.name,
                        value = String.format("%,.2f", idx.value),
                        changeText = "$sign${String.format("%.2f", idx.change)} ($sign${String.format("%.2f", idx.percentChange)}%)",
                        isPositive = isPositive,
                        modifier = Modifier.width(170.dp),
                        sparklinePoints = if (isPositive) listOf(idx.value * 0.992, idx.value * 0.996, idx.value)
                        else listOf(idx.value * 1.008, idx.value * 1.003, idx.value)
                    )
                }
            }
        }

        // 2. GLOBAL MARKET PULSE (USA, Europe, Asia)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                Text(
                    text = "🌐 GLOBAL INDICES PULSE",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(worldIndices, key = { it.symbol }) { item ->
                    val isPos = item.percentChange >= 0
                    val sign = if (isPos) "+" else ""
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalCardBackground)
                            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                Text(item.region.icon, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%,.2f", item.price),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$sign${String.format("%.2f", item.percentChange)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isPos) BullishGreen else BearishRed
                            )
                            Text(
                                text = "${item.exchange} • ${item.freshness.label.take(8)}",
                                fontSize = 8.5.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 3. GLOBAL MACRO (Forex, Commodities, Crypto)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                Text(
                    text = "🛢️ GLOBAL MACRO (CURRENCIES & COMMODITIES)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(macroItems, key = { it.symbol }) { item ->
                    val isPos = item.percentChange >= 0
                    val sign = if (isPos) "+" else ""
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalCardBackground)
                            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(item.symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (item.price > 1000) String.format("%,.1f", item.price) else String.format("%.2f", item.price),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Text(
                                text = "$sign${String.format("%.2f", item.percentChange)}% (${item.currency})",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isPos) BullishGreen else BearishRed
                            )
                        }
                    }
                }
            }
        }

        // 4. MARKET BREADTH & ADVANCES / DECLINES
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalCardBackground)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("NSE BREADTH", fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("▲ $bullishCount", color = BullishGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("▼ $bearishCount", color = BearishRed, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalCardBackground)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("52W EXTREMES", fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Highs: $near52WHigh", color = BullishGreen, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lows: $near52WLow", color = BearishRed, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // 5. QUANTITATIVE MARKET REGIME PANEL
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "QUANTITATIVE MARKET REGIME",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }
                        Text("Risk Assessment", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    marketRegimes.forEach { regime ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(regime.region, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                Text(regime.primaryDriver, fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                Text(regime.regime, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("Risk: ${regime.riskLevel} (${regime.riskScore}/100)", fontSize = 9.5.sp, color = GoldAccent, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // 6. GLOBAL CORRELATION MATRIX
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GLOBAL INTERMARKET CORRELATIONS",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    globalCorrelations.forEach { corr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(corr.pair, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                Text(corr.description, fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.7f)) {
                                val isPos = corr.correlation >= 0
                                Text(
                                    text = "${if (isPos) "+" else ""}${String.format("%.2f", corr.correlation)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (corr.correlation > 0.4) BullishGreen else if (corr.correlation < -0.2) BearishRed else GoldAccent
                                )
                                Text(corr.bias, fontSize = 9.sp, color = AccentCyan, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // 7. PORTFOLIO SNAPSHOT (Computed dynamically from persistent ledger)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalCardBackground)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
                    .clickable { onNavigateToPortfolio() }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PORTFOLIO P&L & XIRR",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Open Ledger", fontSize = 11.sp, color = AccentCyan, fontFamily = FontFamily.Monospace)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PORTFOLIO VALUE", fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "₹${String.format("%,.2f", portfolioSummary.currentValue)}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("ESTIMATED XIRR", fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "${portfolioSummary.xirrPct}% p.a.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (portfolioSummary.xirrPct >= 12.0) BullishGreen else AccentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Invested: ₹${String.format("%,.0f", portfolioSummary.investedCapital)}",
                            fontSize = 10.5.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        val totalPnl = portfolioSummary.unrealizedPnl + portfolioSummary.realizedPnl
                        val isPos = totalPnl >= 0
                        Text(
                            text = "P&L: ${if (isPos) "+" else ""}₹${String.format("%,.0f", totalPnl)} (${if (isPos) "+" else ""}${portfolioSummary.totalReturnPct}%)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPos) BullishGreen else BearishRed,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 8. WATCHLIST & TOP STOCKS
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP TRACKED EQUITIES",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "View Radar →",
                    fontSize = 11.sp,
                    color = AccentCyan,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onNavigateToRadar() }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(stocks.take(5), key = { it.symbol }) { stock ->
            StockRowItem(
                stock = stock,
                onClick = { onNavigateToAnalyzer(stock) }
            )
        }
    }
}
