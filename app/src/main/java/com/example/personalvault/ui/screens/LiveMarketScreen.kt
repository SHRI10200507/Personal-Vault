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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.data.models.GlobalMarketItem
import com.example.personalvault.data.models.MarketRegion
import com.example.personalvault.data.models.StockQuote
import com.example.personalvault.theme.AccentCyan
import com.example.personalvault.theme.BearishRed
import com.example.personalvault.theme.BullishGreen
import com.example.personalvault.theme.GoldAccent
import com.example.personalvault.theme.TerminalCardBackground
import com.example.personalvault.theme.TerminalCardBorder
import com.example.personalvault.theme.TerminalSurface
import com.example.personalvault.theme.TextMuted
import com.example.personalvault.theme.TextPrimary
import com.example.personalvault.theme.TextSecondary
import com.example.personalvault.ui.components.StockRowItem
import com.example.personalvault.ui.viewmodel.MarketViewModel
import com.example.personalvault.ui.viewmodel.SortOption

@Composable
fun LiveMarketScreen(
    viewModel: MarketViewModel,
    onStockClick: (StockQuote) -> Unit
) {
    val filteredStocks by viewModel.filteredStocks.collectAsState()
    val globalMarkets by viewModel.globalMarkets.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sectorFilter by viewModel.sectorFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var activeMarketTab by remember { mutableStateOf("INDIA") } // "INDIA" vs "GLOBAL"
    val sectors = listOf("All", "Banking", "IT", "Auto", "Energy", "Metal", "Infra", "FMCG", "Pharma", "Telecom", "Finance")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("live_market_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Market Scope Switcher (India vs Global)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeMarketTab == "INDIA") AccentCyan else TerminalSurface)
                        .border(1.dp, if (activeMarketTab == "INDIA") AccentCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                        .clickable { activeMarketTab = "INDIA" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🇮🇳 INDIA (NSE / BSE)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (activeMarketTab == "INDIA") Color(0xFF001524) else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeMarketTab == "GLOBAL") AccentCyan else TerminalSurface)
                        .border(1.dp, if (activeMarketTab == "GLOBAL") AccentCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                        .clickable { activeMarketTab = "GLOBAL" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌐 GLOBAL (US/EU/ASIA)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (activeMarketTab == "GLOBAL") Color(0xFF001524) else TextSecondary
                    )
                }
            }
        }

        if (activeMarketTab == "INDIA") {
            // Search bar
            item {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = "Search symbol, company or sector...",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = TerminalSurface,
                            unfocusedContainerColor = TerminalSurface,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = TerminalCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_stock_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sector Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sectors) { sec ->
                            val isSelected = (sectorFilter == sec) || (sectorFilter == null && sec == "All")
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AccentCyan else TerminalSurface)
                                    .border(1.dp, if (isSelected) AccentCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setSectorFilter(if (sec == "All") null else sec) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = sec,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF101318) else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sort Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(SortOption.values()) { opt ->
                            val isSelected = sortOption == opt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) Color(0x2A00D2FF) else Color.Transparent)
                                    .border(0.8.dp, if (isSelected) AccentCyan else TerminalCardBorder, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.setSortOption(opt) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = opt.label,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) AccentCyan else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredStocks.size} EQUITIES TRACKED",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        val isRealTime = filteredStocks.any { it.freshness == com.example.personalvault.data.models.DataFreshness.REAL_TIME }
                        Text(
                            text = if (isRealTime) "DATA: REAL-TIME STREAM" else "DATA: 15-MIN DELAYED",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isRealTime) BullishGreen else GoldAccent
                        )
                    }
                }
            }

            items(filteredStocks, key = { it.symbol }) { stock ->
                StockRowItem(
                    stock = stock,
                    onClick = { onStockClick(stock) }
                )
            }
        } else {
            // GLOBAL MARKETS VIEW
            item {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(
                        text = "GLOBAL INTERMARKET ASSETS & INDICES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(globalMarkets, key = { it.symbol }) { item ->
                GlobalMarketRowItem(item = item)
            }
        }
    }
}

@Composable
fun GlobalMarketRowItem(item: GlobalMarketItem) {
    val isPos = item.percentChange >= 0
    val sign = if (isPos) "+" else ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalCardBackground)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.symbol,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x2A00D2FF))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.exchange,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AccentCyan
                        )
                    }
                }
                Text(
                    text = item.name,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${item.region.label} • ${item.freshness.label}",
                    fontSize = 9.sp,
                    color = GoldAccent,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (item.price > 1000) String.format("%,.2f", item.price) else String.format("%.2f", item.price),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                Text(
                    text = "$sign${String.format("%.2f", item.percentChange)}%",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isPos) BullishGreen else BearishRed
                )
                Text(
                    text = "${item.currency} / 24h: ${item.low24h.toInt()} - ${item.high24h.toInt()}",
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
