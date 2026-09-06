package com.example.personalvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.data.models.DataFreshness
import com.example.personalvault.data.models.StockQuote
import com.example.personalvault.theme.AccentCyan
import com.example.personalvault.theme.BearishBg
import com.example.personalvault.theme.BearishRed
import com.example.personalvault.theme.BullishBg
import com.example.personalvault.theme.BullishGreen
import com.example.personalvault.theme.GoldAccent
import com.example.personalvault.theme.TerminalCardBorder
import com.example.personalvault.theme.TerminalSurface
import com.example.personalvault.theme.TerminalSurfaceVariant
import com.example.personalvault.theme.TextMuted
import com.example.personalvault.theme.TextPrimary
import com.example.personalvault.theme.TextSecondary

@Composable
fun StockRowItem(
    stock: StockQuote,
    isWatchlisted: Boolean = false,
    onToggleWatchlist: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = stock.percentChange >= 0
    val deltaColor = if (isPositive) BullishGreen else BearishRed
    val deltaBg = if (isPositive) BullishBg else BearishBg
    val sign = if (isPositive) "+" else ""

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("stock_item_${stock.symbol}")
    ) {
        Column {
            // Top row: Symbol, Sector, Price, Change %
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Star + Symbol + Sector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("star_${stock.symbol}")
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Watchlist toggle",
                            tint = if (isWatchlisted) GoldAccent else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stock.symbol,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TerminalSurfaceVariant)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = stock.sector,
                                    fontSize = 9.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (stock.freshness == DataFreshness.REAL_TIME) Color(0x2200E676) else Color(0x22FFD700))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (stock.source.isNotEmpty()) stock.source else stock.exchange,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stock.freshness == DataFreshness.REAL_TIME) BullishGreen else GoldAccent
                                )
                            }
                        }
                        Text(
                            text = stock.company,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: LTP & Change %
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format("%,.2f", stock.price)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(deltaBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$sign${String.format("%.2f", stock.percentChange)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = deltaColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: 52W Position Bar & Score Pill & Quick Fundamentals
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 52W position bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "52W",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TerminalSurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stock.position52W / 100f)
                                .height(4.dp)
                                .background(if (stock.position52W > 70) BullishGreen else AccentCyan)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stock.position52W}%",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // PE & PB
                Text(
                    text = "PE ${stock.pe} • PB ${stock.pb}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Score pill
                OverallScoreBadge(
                    score = stock.overallScore,
                    label = stock.scoreLabel
                )
            }
        }
    }
}
