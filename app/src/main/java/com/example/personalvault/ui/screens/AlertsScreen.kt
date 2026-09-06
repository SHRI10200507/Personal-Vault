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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.data.models.MarketAlert
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
fun AlertsScreen(
    viewModel: MarketViewModel
) {
    val alerts by viewModel.alerts.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val showAddDialog by viewModel.showAddAlertDialog.collectAsState()

    var filterTriggeredOnly by remember { mutableStateOf(false) }
    val displayedAlerts = if (filterTriggeredOnly) alerts.filter { it.triggered } else alerts

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("alerts_screen"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header Info Card
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
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MULTI-FACTOR MARKET ALERTS ENGINE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Text(
                                text = "Monitored against live market ticks with 15m cooldown & Android notifications.",
                                fontSize = 10.5.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Filter Tabs & Stats
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (!filterTriggeredOnly) AccentCyan else TerminalSurface)
                                .clickable { filterTriggeredOnly = false }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "ALL (${alerts.size})",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (!filterTriggeredOnly) Color(0xFF101318) else TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (filterTriggeredOnly) AccentCyan else TerminalSurface)
                                .clickable { filterTriggeredOnly = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "TRIGGERED (${alerts.count { it.triggered }})",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (filterTriggeredOnly) Color(0xFF101318) else TextSecondary
                            )
                        }
                    }

                    Text(
                        text = "Active Watch: ${alerts.count { it.enabled }}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Alerts List
            if (displayedAlerts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (filterTriggeredOnly) "No triggered alerts." else "No active alerts configured.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the '+' button below to set a new threshold.",
                                fontSize = 10.5.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else {
                items(displayedAlerts, key = { it.id }) { alert ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalCardBackground)
                            .border(
                                1.dp,
                                if (alert.triggered) AccentCyan.copy(alpha = 0.6f) else TerminalCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = alert.symbol,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                when (alert.severity) {
                                                    "critical" -> BearishBg
                                                    "success" -> BullishBg
                                                    else -> TerminalSurface
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = alert.condition,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = when (alert.severity) {
                                                "critical" -> BearishRed
                                                "success" -> BullishGreen
                                                else -> AccentCyan
                                            }
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (alert.triggered) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(BullishBg)
                                                .border(1.dp, BullishGreen, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "TRIGGERED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = BullishGreen
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(TerminalSurface)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "MONITORING",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.removeAlert(alert.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Alert",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = alert.message,
                                fontSize = 11.5.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Target: ₹${alert.targetValue}",
                                    fontSize = 9.5.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Sync: ${alert.timestamp}",
                                    fontSize = 9.5.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Alert FAB
        FloatingActionButton(
            onClick = { viewModel.setShowAddAlertDialog(true) },
            containerColor = AccentCyan,
            contentColor = Color(0xFF101318),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("add_alert_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add alert")
        }
    }

    if (showAddDialog) {
        AddAlertDialog(
            availableSymbols = stocks.map { it.symbol },
            onDismiss = { viewModel.setShowAddAlertDialog(false) },
            onAdd = { symbol, condition, target ->
                viewModel.addAlert(symbol, condition, target)
            }
        )
    }
}

@Composable
fun AddAlertDialog(
    availableSymbols: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, Double) -> Unit
) {
    val conditionPresets = listOf(
        "Price >",
        "Price <",
        "% Change >",
        "% Change <",
        "RSI >",
        "RSI <",
        "52W High Breakout",
        "52W Low Breakdown",
        "Volume Spike",
        "EMA Bullish Cross",
        "EMA Bearish Cross"
    )

    var symbol by remember { mutableStateOf(availableSymbols.firstOrNull() ?: "RELIANCE") }
    var condition by remember { mutableStateOf(conditionPresets.first()) }
    var targetStr by remember { mutableStateOf("3000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CREATE MARKET ALERT",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Stock:", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(availableSymbols) { sym ->
                        val isSel = sym == symbol
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) AccentCyan else TerminalSurface)
                                .clickable { symbol = sym }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = sym,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSel) Color(0xFF101318) else TextSecondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it.uppercase() },
                    label = { Text("Or Custom Symbol", fontSize = 10.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TerminalCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("alert_symbol_input")
                )

                Text("Alert Condition:", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(conditionPresets) { cond ->
                        val isSel = cond == condition
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) GoldAccent else TerminalSurface)
                                .clickable { condition = cond }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cond,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSel) Color(0xFF101318) else TextSecondary
                            )
                        }
                    }
                }

                val requiresTarget = !condition.contains("Breakout") && !condition.contains("Breakdown") && !condition.contains("Cross") && !condition.contains("Volume Spike")
                if (requiresTarget) {
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("Target Threshold", fontSize = 10.5.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = TerminalCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("alert_target_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetStr.toDoubleOrNull() ?: 0.0
                    if (symbol.isNotBlank()) {
                        onAdd(symbol.trim(), condition.trim(), target)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color(0xFF101318)),
                modifier = Modifier.testTag("save_alert_button")
            ) {
                Text("ACTIVATE ALERT", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        },
        containerColor = TerminalSurface,
        tonalElevation = 6.dp
    )
}
