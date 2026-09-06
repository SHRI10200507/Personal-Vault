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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.theme.AccentCyan
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
fun MoreScreen(
    viewModel: MarketViewModel,
    onNavigateToAnalyzer: () -> Unit,
    onNavigateToStrategy: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToDataCenter: () -> Unit,
    onExportExcel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.personalvault.theme.TerminalBackground)
            .testTag("more_screen"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalCardBackground)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "TERMINAL SUITE & SERVICES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Access secondary analytical tools, backtest environments, alert triggers, and data feeds.",
                        fontSize = 10.5.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            TerminalToolCard(
                title = "Stock Analyzer & Candlesticks",
                description = "Interactive Candlestick + Volume chart, SMA overlays, quantitative factor scores, and risk drivers.",
                icon = Icons.Default.Analytics,
                accentColor = AccentCyan,
                tag = "analyzer_tile",
                onClick = onNavigateToAnalyzer
            )
        }

        item {
            TerminalToolCard(
                title = "Quantitative Strategy Lab",
                description = "Simulate EMA, 52W Breakouts, and Mean-Reversion rules bar-by-bar with slippage, brokerage, and STT costs.",
                icon = Icons.Default.Science,
                accentColor = GoldAccent,
                tag = "strategy_tile",
                onClick = onNavigateToStrategy
            )
        }

        item {
            TerminalToolCard(
                title = "Alerts & Risk Triggers",
                description = "Monitor dynamic threshold alerts on NSE equities, 52W breakouts, and volatility spikes.",
                icon = Icons.Default.NotificationsActive,
                accentColor = AccentCyan,
                tag = "alerts_tile",
                onClick = onNavigateToAlerts
            )
        }

        item {
            TerminalToolCard(
                title = "Data Center & Connection Telemetry",
                description = "Manage API providers (Twelve Data, Alpha Vantage), global exchange time clocks, and latency diagnostics.",
                icon = Icons.Default.Dns,
                accentColor = BullishGreen,
                tag = "data_center_tile",
                onClick = onNavigateToDataCenter
            )
        }

        item {
            TerminalToolCard(
                title = "Export Excel Terminal (.xlsx / CSV)",
                description = "Generate multi-sheet workbook with live symbols, portfolio ledger, XIRR formulas, and market overview.",
                icon = Icons.Default.FileDownload,
                accentColor = BullishGreen,
                tag = "export_tile",
                onClick = onExportExcel
            )
        }
    }
}

@Composable
fun TerminalToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = TerminalCardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = TextMuted,
                        lineHeight = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}
