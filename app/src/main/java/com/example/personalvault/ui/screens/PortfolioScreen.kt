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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import com.example.personalvault.data.models.PortfolioHolding
import com.example.personalvault.data.models.PortfolioTransaction
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
fun PortfolioScreen(
    viewModel: MarketViewModel,
    onStockClick: (String) -> Unit
) {
    val holdings by viewModel.portfolioHoldings.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()
    val showAddDialog by viewModel.showAddTransactionDialog.collectAsState()

    var activeTab by remember { mutableStateOf("HOLDINGS") } // "HOLDINGS" or "LEDGER"

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("portfolio_screen"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Portfolio Summary Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
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
                            Text(
                                text = "PORTFOLIO INTELLIGENCE & XIRR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x2A00E676))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "XIRR: ${summary.xirrPct}% P.A.",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = BullishGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current Value & Total Return
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("PORTFOLIO VALUE", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = "₹${String.format("%,.2f", summary.currentValue)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("TOTAL UNREALIZED P&L", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                val isPos = summary.unrealizedPnl >= 0
                                Text(
                                    text = "${if (isPos) "+" else ""}₹${String.format("%,.2f", summary.unrealizedPnl)} (${if (isPos) "+" else ""}${summary.totalReturnPct}%)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isPos) BullishGreen else BearishRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = TerminalCardBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("INVESTED CAPITAL", fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                Text("₹${String.format("%,.0f", summary.investedCapital)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            }
                            Column {
                                Text("REALIZED P&L", fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                val rPos = summary.realizedPnl >= 0
                                Text("${if (rPos) "+" else ""}₹${String.format("%,.0f", summary.realizedPnl)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (rPos) BullishGreen else BearishRed, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TODAY'S CHANGE", fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                val tPos = summary.todayPnl >= 0
                                Text("${if (tPos) "+" else ""}₹${String.format("%,.0f", summary.todayPnl)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (tPos) BullishGreen else BearishRed, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            // Tab Switcher: Holdings vs Ledger
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == "HOLDINGS") AccentCyan else TerminalSurface)
                            .border(1.dp, if (activeTab == "HOLDINGS") AccentCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                            .clickable { activeTab = "HOLDINGS" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HOLDINGS (${holdings.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (activeTab == "HOLDINGS") Color(0xFF001524) else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == "LEDGER") AccentCyan else TerminalSurface)
                            .border(1.dp, if (activeTab == "LEDGER") AccentCyan else TerminalCardBorder, RoundedCornerShape(6.dp))
                            .clickable { activeTab = "LEDGER" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TRANSACTION LEDGER (${transactions.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (activeTab == "LEDGER") Color(0xFF001524) else TextSecondary
                        )
                    }
                }
            }

            // Content based on active tab
            if (activeTab == "HOLDINGS") {
                if (holdings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No active holdings found in ledger. Tap '+' to record a buy trade.", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                } else {
                    items(holdings, key = { it.symbol }) { holding ->
                        HoldingCardItem(
                            holding = holding,
                            onClick = { onStockClick(holding.symbol) }
                        )
                    }
                }
            } else {
                // Transactions Ledger
                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions recorded yet. Tap '+' to add a transaction.", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                } else {
                    items(transactions.reversed(), key = { it.id }) { tx ->
                        TransactionRowItem(
                            tx = tx,
                            onDelete = { viewModel.removeTransaction(tx.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Transaction
        FloatingActionButton(
            onClick = { viewModel.setShowAddTransactionDialog(true) },
            containerColor = AccentCyan,
            contentColor = Color(0xFF001524),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("add_transaction_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { viewModel.setShowAddTransactionDialog(false) },
            onAdd = { sym, comp, type, qty, px, brok, taxes, notes ->
                viewModel.addTransaction(sym, comp, type, qty, px, brok, taxes, notes)
            }
        )
    }
}

@Composable
fun HoldingCardItem(
    holding: PortfolioHolding,
    onClick: () -> Unit
) {
    val isPos = holding.totalPnl >= 0
    val deltaColor = if (isPos) BullishGreen else BearishRed
    val sign = if (isPos) "+" else ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalCardBackground)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
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
                        text = holding.symbol,
                        fontSize = 14.sp,
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
                            text = holding.sector,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AccentCyan
                        )
                    }
                }

                Text(
                    text = "${holding.shares} Shares",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Avg: ₹${String.format("%,.2f", holding.buyPrice)}", fontSize = 10.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Text("LTP: ₹${String.format("%,.2f", holding.currentPrice)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Val: ₹${String.format("%,.0f", holding.currentValue)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$sign₹${String.format("%,.0f", holding.totalPnl)} ($sign${String.format("%.2f", holding.pnlPercent)}%)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = deltaColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    tx: PortfolioTransaction,
    onDelete: () -> Unit
) {
    val isBuy = tx.type == "BUY"
    val badgeColor = if (isBuy) BullishGreen else BearishRed
    val badgeBg = if (isBuy) Color(0x2A00E676) else Color(0x2AFF5252)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalCardBackground)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeBg)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = tx.type,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tx.symbol,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${tx.date} • ${tx.shares} shares @ ₹${tx.price}",
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                if (tx.notes.isNotBlank()) {
                    Text(
                        text = "Notes: ${tx.notes}",
                        fontSize = 9.5.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    val gross = tx.shares * tx.price
                    Text(
                        text = "₹${String.format("%,.0f", gross)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Costs: ₹${(tx.brokerage + tx.taxes).toInt()}",
                        fontSize = 9.5.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete transaction",
                        tint = BearishRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int, Double, Double, Double, String) -> Unit
) {
    var symbol by remember { mutableStateOf("RELIANCE") }
    var sharesStr by remember { mutableStateOf("10") }
    var priceStr by remember { mutableStateOf("2950") }
    var type by remember { mutableStateOf("BUY") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalSurface,
        title = {
            Text(
                text = "RECORD TRADE TRANSACTION",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Trade Type Selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (type == "BUY") BullishGreen else TerminalCardBackground)
                            .clickable { type = "BUY" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("BUY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (type == "BUY") Color.Black else TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (type == "SELL") BearishRed else TerminalCardBackground)
                            .clickable { type = "SELL" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SELL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (type == "SELL") Color.White else TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it.uppercase() },
                    label = { Text("NSE Symbol (e.g. TCS)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, unfocusedBorderColor = TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sharesStr,
                    onValueChange = { sharesStr = it },
                    label = { Text("Number of Shares", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, unfocusedBorderColor = TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Execution Price (₹)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, unfocusedBorderColor = TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Strategy Tag", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, unfocusedBorderColor = TerminalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = sharesStr.toIntOrNull() ?: 1
                    val px = priceStr.toDoubleOrNull() ?: 100.0
                    onAdd(symbol.trim(), symbol.trim(), type, qty, px, 20.0, 15.0, notes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("RECORD ENTRY", color = Color(0xFF001524), fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    )
}
