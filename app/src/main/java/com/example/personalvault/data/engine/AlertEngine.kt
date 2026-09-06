package com.example.personalvault.data.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.personalvault.data.local.LocalStoreManager
import com.example.personalvault.data.models.MarketAlert
import com.example.personalvault.data.models.StockQuote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlertEngine {

    private const val CHANNEL_ID = "market_terminal_alerts"
    private const val CHANNEL_NAME = "Market Price & Technical Alerts"
    private var appContext: Context? = null
    private var notificationIdCounter = 1001

    fun initialize(context: Context) {
        appContext = context.applicationContext
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ctx = appContext ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time market price, breakout and technical indicator triggers"
                enableVibration(true)
            }
            val manager = ctx.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Evaluates active alerts against freshly synced stock quotes.
     * Respects cooldown intervals and avoids repeated duplicate triggers.
     */
    fun evaluateAlerts(quotes: List<StockQuote>): List<MarketAlert> {
        val currentAlerts = LocalStoreManager.loadAlerts()
        if (currentAlerts.isEmpty()) return emptyList()

        val quotesMap = quotes.associateBy { it.symbol.uppercase() }
        val updatedAlerts = mutableListOf<MarketAlert>()
        var changed = false
        val nowMillis = System.currentTimeMillis()
        val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
        val nowTime = sdf.format(Date())

        for (alert in currentAlerts) {
            if (!alert.enabled) {
                updatedAlerts.add(alert)
                continue
            }

            val quote = quotesMap[alert.symbol.uppercase()]
            if (quote == null) {
                updatedAlerts.add(alert)
                continue
            }

            // Cooldown check (default 15 mins)
            val cooldownDurationMillis = alert.cooldownMinutes * 60 * 1000L
            val isCooldownElapsed = (nowMillis - alert.lastTriggeredTimeMillis) >= cooldownDurationMillis

            var conditionMet = false
            var triggerMessage = ""
            var severity = alert.severity

            when (alert.condition) {
                "Price >" -> {
                    if (quote.price >= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} surged past target ₹${alert.targetValue} (Current: ₹${quote.price})"
                        severity = "success"
                    }
                }
                "Price <" -> {
                    if (quote.price <= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} dropped below target ₹${alert.targetValue} (Current: ₹${quote.price})"
                        severity = "warning"
                    }
                }
                "% Change >" -> {
                    if (quote.percentChange >= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} gained +${quote.percentChange}% exceeding +${alert.targetValue}% threshold"
                        severity = "success"
                    }
                }
                "% Change <" -> {
                    if (quote.percentChange <= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} fell ${quote.percentChange}% breaching ${alert.targetValue}% threshold"
                        severity = "warning"
                    }
                }
                "RSI >" -> {
                    if (quote.rsi >= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} RSI reached ${quote.rsi.toInt()} (> ${alert.targetValue.toInt()} Overbought)"
                        severity = "warning"
                    }
                }
                "RSI <" -> {
                    if (quote.rsi <= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} RSI dropped to ${quote.rsi.toInt()} (< ${alert.targetValue.toInt()} Oversold)"
                        severity = "info"
                    }
                }
                "52W High Breakout" -> {
                    if (quote.price >= quote.week52High * 0.985) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} trading at 52-Week High ₹${quote.price} (Peak: ₹${quote.week52High})"
                        severity = "success"
                    }
                }
                "52W Low Breakdown" -> {
                    if (quote.price <= quote.week52Low * 1.015) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} testing 52-Week Low at ₹${quote.price} (Trough: ₹${quote.week52Low})"
                        severity = "critical"
                    }
                }
                "Volume Spike" -> {
                    if (quote.volumeRatio >= 1.8) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} institutional volume spike: ${String.format("%.1f", quote.volumeRatio)}× average 20-day volume"
                        severity = "success"
                    }
                }
                "EMA Bullish Cross" -> {
                    if (quote.sma20 > quote.sma50 && quote.price > quote.sma20) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} Bullish 20 EMA > 50 EMA continuation confirmed"
                        severity = "success"
                    }
                }
                "EMA Bearish Cross" -> {
                    if (quote.sma20 < quote.sma50 && quote.price < quote.sma20) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} Bearish 20 EMA < 50 EMA breakdown confirmed"
                        severity = "warning"
                    }
                }
                else -> {
                    if (quote.price >= alert.targetValue && alert.targetValue > 0) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} hit trigger level ₹${alert.targetValue}"
                    }
                }
            }

            if (conditionMet && (isCooldownElapsed || !alert.triggered)) {
                changed = true
                val updated = alert.copy(
                    triggered = true,
                    timestamp = nowTime,
                    message = triggerMessage,
                    severity = severity,
                    lastTriggeredPrice = quote.price,
                    lastTriggeredTimeMillis = nowMillis
                )
                updatedAlerts.add(updated)
                postNotification(updated.symbol, triggerMessage)
            } else {
                updatedAlerts.add(alert)
            }
        }

        if (changed) {
            LocalStoreManager.saveAlerts(updatedAlerts)
        }

        return updatedAlerts
    }

    /**
     * Instantly evaluates active alerts for a single instrument tick in O(1).
     * Prevents scanning the entire universe on every sub-second streaming price update.
     */
    fun evaluateSingleStock(quote: StockQuote): List<MarketAlert>? {
        val currentAlerts = LocalStoreManager.loadAlerts()
        val symbolAlerts = currentAlerts.filter { it.enabled && it.symbol.equals(quote.symbol, ignoreCase = true) }
        if (symbolAlerts.isEmpty()) return null

        val nowMillis = System.currentTimeMillis()
        val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
        val nowTime = sdf.format(Date())
        var changed = false
        val updatedAlerts = currentAlerts.toMutableList()

        for (i in updatedAlerts.indices) {
            val alert = updatedAlerts[i]
            if (!alert.enabled || !alert.symbol.equals(quote.symbol, ignoreCase = true)) continue

            val cooldownDurationMillis = alert.cooldownMinutes * 60 * 1000L
            val isCooldownElapsed = (nowMillis - alert.lastTriggeredTimeMillis) >= cooldownDurationMillis

            var conditionMet = false
            var triggerMessage = ""
            var severity = alert.severity

            when (alert.condition) {
                "Price >" -> {
                    if (quote.price >= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} surged past target ₹${alert.targetValue} (Current: ₹${quote.price})"
                        severity = "success"
                    }
                }
                "Price <" -> {
                    if (quote.price <= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} dropped below target ₹${alert.targetValue} (Current: ₹${quote.price})"
                        severity = "warning"
                    }
                }
                "% Change >" -> {
                    if (quote.percentChange >= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} gained +${quote.percentChange}% exceeding +${alert.targetValue}% threshold"
                        severity = "success"
                    }
                }
                "% Change <" -> {
                    if (quote.percentChange <= alert.targetValue) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} fell ${quote.percentChange}% breaching ${alert.targetValue}% threshold"
                        severity = "warning"
                    }
                }
                "52W High Breakout" -> {
                    if (quote.price >= quote.week52High * 0.985) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} trading at 52-Week High ₹${quote.price}"
                        severity = "success"
                    }
                }
                "52W Low Breakdown" -> {
                    if (quote.price <= quote.week52Low * 1.015) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} testing 52-Week Low at ₹${quote.price}"
                        severity = "critical"
                    }
                }
                else -> {
                    if (quote.price >= alert.targetValue && alert.targetValue > 0) {
                        conditionMet = true
                        triggerMessage = "${quote.symbol} reached trigger ₹${alert.targetValue}"
                    }
                }
            }

            if (conditionMet && (isCooldownElapsed || !alert.triggered)) {
                changed = true
                val updated = alert.copy(
                    triggered = true,
                    timestamp = nowTime,
                    message = triggerMessage,
                    severity = severity,
                    lastTriggeredPrice = quote.price,
                    lastTriggeredTimeMillis = nowMillis
                )
                updatedAlerts[i] = updated
                postNotification(updated.symbol, triggerMessage)
            }
        }

        if (changed) {
            LocalStoreManager.saveAlerts(updatedAlerts)
            return updatedAlerts
        }
        return null
    }

    private fun postNotification(title: String, message: String) {
        val ctx = appContext ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS)
            if (permission != PackageManager.PERMISSION_GRANTED) return
        }

        try {
            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Market Alert: $title")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(notificationIdCounter++, builder.build())
        } catch (_: Exception) {}
    }
}
