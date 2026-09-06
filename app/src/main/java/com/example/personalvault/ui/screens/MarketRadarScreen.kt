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
import com.example.personalvault.data.models.RadarCategory
import com.example.personalvault.data.models.StockQuote
import com.example.personalvault.theme.AccentCyan
import com.example.personalvault.theme.TerminalCardBorder
import com.example.personalvault.theme.TerminalSurface
import com.example.personalvault.theme.TerminalSurfaceVariant
import com.example.personalvault.theme.TextMuted
import com.example.personalvault.theme.TextPrimary
import com.example.personalvault.theme.TextSecondary
import com.example.personalvault.ui.components.StockRowItem
import com.example.personalvault.ui.viewmodel.MarketViewModel

@Composable
fun MarketRadarScreen(
    viewModel: MarketViewModel,
    onStockClick: (StockQuote) -> Unit
) {
    val stocks by viewModel.stocks.collectAsState()
    val selectedCategory by viewModel.selectedRadarCategory.collectAsState()
    val watchlist by viewModel.watchlistSymbols.collectAsState()

    val currentCategory = selectedCategory ?: RadarCategory.BREAKOUT

    val filteredStocks = when (currentCategory) {
        RadarCategory.BREAKOUT -> stocks.filter { it.position52W >= 80 || it.percentChange >= 1.5 }
        RadarCategory.STRONG_MOMENTUM -> stocks.filter { it.technicalScore >= 65 && it.percentChange > 0 }
        RadarCategory.VOLUME_SPIKE -> stocks.filter { it.volumeRatio >= 1.2 || it.percentChange >= 2.0 }
        RadarCategory.OVERSOLD -> stocks.filter { it.rsi <= 45.0 }
        RadarCategory.OVERBOUGHT -> stocks.filter { it.rsi >= 65.0 }
        RadarCategory.TRENDING -> stocks.filter { it.price > it.sma50 }
        RadarCategory.HIGH_52W -> stocks.filter { it.distanceTo52WHighPct <= 5.0 }
        RadarCategory.LOW_52W -> stocks.filter { it.position52W <= 20 }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("market_radar_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Radar Description Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalSurface)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📡",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SMART MARKET RADAR & SCREENER",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Text(
                            text = "Automated scanner grouping Indian stocks by momentum, breakout velocity, volume, and oversold setups.",
                            fontSize = 10.5.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Category Selector Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(RadarCategory.values()) { cat ->
                    val isSelected = cat == currentCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AccentCyan else TerminalSurface)
                            .border(1.dp, if (isSelected) AccentCyan else TerminalCardBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectRadarCategory(cat) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("radar_tab_${cat.name}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Color(0xFF101318) else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Filter Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${currentCategory.label.uppercase()} CANDIDATES (${filteredStocks.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
                Text(
                    text = "Sorted by Signal Strength",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Filtered Stocks
        if (filteredStocks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No stocks currently qualify for ${currentCategory.label} filters.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            items(filteredStocks, key = { it.symbol }) { stock ->
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    StockRowItem(
                        stock = stock,
                        isWatchlisted = watchlist.contains(stock.symbol),
                        onToggleWatchlist = { viewModel.toggleWatchlist(stock.symbol) },
                        onClick = { onStockClick(stock) }
                    )
                }
            }
        }
    }
}
