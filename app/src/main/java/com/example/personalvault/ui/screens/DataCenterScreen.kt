package com.example.personalvault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.personalvault.data.local.LocalStoreManager
import com.example.personalvault.data.models.ExchangeInfo
import com.example.personalvault.data.models.MarketStatus
import com.example.personalvault.theme.AccentCyan
import com.example.personalvault.theme.BearishRed
import com.example.personalvault.theme.BullishGreen
import com.example.personalvault.theme.GoldAccent
import com.example.personalvault.theme.TerminalBackground
import com.example.personalvault.theme.TerminalCardBackground
import com.example.personalvault.theme.TerminalCardBorder
import com.example.personalvault.theme.TerminalSurface
import com.example.personalvault.theme.TextMuted
import com.example.personalvault.theme.TextPrimary
import com.example.personalvault.theme.TextSecondary
import com.example.personalvault.ui.viewmodel.MarketViewModel

@Composable
fun DataCenterScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val dataStatus by viewModel.dataCenterStatus.collectAsState()
    val apiSettings by viewModel.apiSettings.collectAsState()
    val exchanges by viewModel.exchangeSchedule.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var apiKeyInput by remember(apiSettings.apiKey) { mutableStateOf(apiSettings.apiKey) }
    var selectedProvider by remember(apiSettings.provider) { mutableStateOf(apiSettings.provider) }
    var savedMessage by remember { mutableStateOf("") }

    val providers = listOf(
        "Twelve Data / High-Speed Feed",
        "Alpha Vantage Global Equity",
        "Global Real-Time Market Feed (Zero-Key)",
        "NSE & Global Benchmark Snapshot Engine"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Telemetry Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DATA CENTER & FEED TELEMETRY",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (dataStatus.isConnected) Color(0x2A00E676) else Color(0x2AFF5252))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (dataStatus.isConnected) dataStatus.dataState else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (dataStatus.isConnected) BullishGreen else BearishRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Latency Metric
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Latency", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${dataStatus.latencyMs} ms",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (dataStatus.latencyMs < 100) BullishGreen else GoldAccent
                                )
                            }
                        }

                        // Stream Ticks
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stream Ticks", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${dataStatus.streamTickCount}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = BullishGreen
                                )
                            }
                        }

                        // API Calls
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("REST Calls", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${dataStatus.requestsUsed}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Active Feed: ${dataStatus.provider}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "WebSocket Pipe: ${dataStatus.webSocketStatus}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dataStatus.webSocketStatus == "STREAMING" || dataStatus.webSocketStatus == "CONNECTED") BullishGreen else AccentCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Status: ${dataStatus.statusMessage}",
                        fontSize = 10.5.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Last Synchronized: ${dataStatus.lastSyncTime}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Subsystems Health Check
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MARKET DATA SUBSYSTEM HEALTH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val feeds = listOf(
                        Pair("WebSocket High-Speed Pipe", dataStatus.webSocketStatus),
                        Pair("NSE / BSE Equities Feed", dataStatus.nseBseStatus),
                        Pair("US & Global Benchmarks Feed", dataStatus.globalEquityStatus),
                        Pair("Global Currencies / Forex Stream", dataStatus.forexStatus),
                        Pair("Metals & Energy Commodities Feed", dataStatus.commodityStatus),
                        Pair("Sovereign Bond Yields Stream", dataStatus.bondsStatus)
                    )

                    feeds.forEach { (feed, status) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(feed, fontSize = 10.5.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (status == "CONNECTED") Color(0x2A00E676) else Color(0x2AFFB300))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = status,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (status == "CONNECTED") BullishGreen else GoldAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global Exchange Sessions Clock
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GLOBAL EXCHANGE CALENDAR & TIMEZONES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    exchanges.forEach { ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (ex.status) {
                                                MarketStatus.OPEN -> BullishGreen
                                                MarketStatus.PRE_OPEN, MarketStatus.AFTER_HOURS -> GoldAccent
                                                MarketStatus.CLOSED -> BearishRed
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "${ex.code} (${ex.country})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${ex.openTime} - ${ex.closeTime} local",
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            when (ex.status) {
                                                MarketStatus.OPEN -> Color(0x2A00E676)
                                                MarketStatus.PRE_OPEN, MarketStatus.AFTER_HOURS -> Color(0x2AFFB300)
                                                MarketStatus.CLOSED -> Color(0x2AFF5252)
                                            }
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = ex.status.label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = when (ex.status) {
                                            MarketStatus.OPEN -> BullishGreen
                                            MarketStatus.PRE_OPEN, MarketStatus.AFTER_HOURS -> GoldAccent
                                            MarketStatus.CLOSED -> BearishRed
                                        }
                                    )
                                }
                                Text(
                                    text = "Local: ${ex.localTimeStr}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Provider & API Key Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROVIDER CONFIGURATION",
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("KEYSTORE AES-256 GCM", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = AccentCyan)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Primary Provider:", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))

                    providers.forEach { p ->
                        val isSelected = selectedProvider == p
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Color(0x2A00D2FF) else TerminalSurface)
                                .border(1.dp, if (isSelected) AccentCyan else TerminalCardBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = isSelected,
                                onClick = { selectedProvider = p },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = AccentCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = p,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) AccentCyan else TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Provider API Key (Stored in Hardware Keystore):", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        placeholder = { Text("Paste Twelve Data / Alpha Vantage Key...", fontSize = 11.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentCyan
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveApiSettings(
                                    LocalStoreManager.ApiSettings(
                                        provider = selectedProvider,
                                        apiKey = apiKeyInput.trim(),
                                        autoRefresh = apiSettings.autoRefresh,
                                        intervalSeconds = apiSettings.intervalSeconds
                                    )
                                )
                                savedMessage = "API key secured in Android Keystore & saved."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.testTag("save_api_settings_button")
                        ) {
                            Text("SAVE SECURELY", color = Color(0xFF001524), fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { viewModel.triggerManualRefresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PING & SYNC", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    if (savedMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = savedMessage,
                            fontSize = 11.sp,
                            color = BullishGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
