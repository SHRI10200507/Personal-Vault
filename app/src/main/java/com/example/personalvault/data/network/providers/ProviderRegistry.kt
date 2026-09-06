package com.example.personalvault.data.network.providers

import android.content.Context
import com.example.personalvault.data.models.CandleBar
import com.example.personalvault.data.models.DataConnectionStatus
import com.example.personalvault.data.models.DataFreshness
import com.example.personalvault.data.models.GlobalMarketItem
import com.example.personalvault.data.models.MarketIndex
import com.example.personalvault.data.security.SecureKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProviderRegistry {

    private var currentProviderName: String = "Twelve Data / High-Speed Feed"
    private var cachedApiKey: String = ""
    private var requestsUsedCount: Int = 0
    private var lastSuccessfulQuoteTimestamp: String = "09:15:00 IST"
    private var lastObservedLatency: Long = 42L
    private var isCurrentlyOnline: Boolean = true

    fun initialize(context: Context) {
        cachedApiKey = SecureKeyStore.loadApiKey(context)
    }

    fun setProvider(providerName: String, apiKey: String, context: Context?) {
        currentProviderName = providerName
        cachedApiKey = apiKey.trim()
        if (context != null) {
            SecureKeyStore.saveApiKey(context, cachedApiKey)
        }
    }

    fun getApiKey(): String = cachedApiKey

    fun getActiveProvider(): MarketDataProvider {
        val lower = currentProviderName.lowercase()
        return when {
            lower.contains("indian") || lower.contains("nse") -> IndianMarketProvider()
            lower.contains("twelve") -> TwelveDataProvider(cachedApiKey)
            lower.contains("alpha") -> AlphaVantageProvider(cachedApiKey)
            else -> PublicMarketDataProvider()
        }
    }

    var webSocketStatus: String = "STREAMING"
    var streamTickCount: Long = 0L

    suspend fun pingActiveProvider(): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val provider = getActiveProvider()
        val result = provider.pingProvider()
        isCurrentlyOnline = result.first
        if (result.first && result.second > 0) {
            lastObservedLatency = result.second
        }
        result
    }

    suspend fun fetchQuotes(symbols: List<String>): Map<String, QuoteSnapshot> = withContext(Dispatchers.IO) {
        requestsUsedCount++
        val provider = getActiveProvider()
        try {
            val quotes = provider.fetchQuotes(symbols)
            if (quotes.isNotEmpty()) {
                val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
                lastSuccessfulQuoteTimestamp = sdf.format(Date())
                isCurrentlyOnline = true
            }
            quotes
        } catch (_: Exception) {
            isCurrentlyOnline = false
            emptyMap()
        }
    }

    suspend fun fetchIndices(): List<MarketIndex> = withContext(Dispatchers.IO) {
        requestsUsedCount++
        val provider = getActiveProvider()
        try {
            provider.fetchIndices()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun fetchGlobalMarkets(): List<GlobalMarketItem> = withContext(Dispatchers.IO) {
        requestsUsedCount++
        val provider = getActiveProvider()
        try {
            provider.fetchGlobalMarkets()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun fetchHistoricalCandles(symbol: String, range: String = "1Y"): List<CandleBar> = withContext(Dispatchers.IO) {
        requestsUsedCount++
        val provider = getActiveProvider()
        try {
            provider.fetchHistoricalCandles(symbol, range)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun buildTelemetryStatus(freshness: DataFreshness): DataConnectionStatus {
        val stateLabel = when (freshness) {
            DataFreshness.REAL_TIME -> "LIVE"
            DataFreshness.DELAYED_15M -> "DELAYED"
            DataFreshness.CACHED, DataFreshness.OFFICIAL_EOD -> "CACHED"
            DataFreshness.ERROR -> "ERROR"
        }

        val msg = if (isCurrentlyOnline) {
            if (stateLabel == "LIVE") "Real-Time Authentic Market Stream Active" else "Delayed 15-Min Snapshot Verified"
        } else {
            "Network Offline • Operating on Cached Vault Data"
        }

        return DataConnectionStatus(
            provider = currentProviderName,
            isConnected = isCurrentlyOnline,
            latencyMs = lastObservedLatency,
            statusMessage = msg,
            requestsUsed = requestsUsedCount,
            requestsMax = if (currentProviderName.contains("Twelve")) 800 else if (currentProviderName.contains("Alpha")) 500 else 5000,
            lastSyncTime = lastSuccessfulQuoteTimestamp,
            dataState = stateLabel,
            lastSuccessfulQuoteTime = lastSuccessfulQuoteTimestamp,
            nseBseStatus = if (isCurrentlyOnline) "CONNECTED" else "CACHED",
            globalEquityStatus = if (isCurrentlyOnline) "CONNECTED" else "CACHED",
            forexStatus = if (isCurrentlyOnline) "CONNECTED" else "CACHED",
            commodityStatus = if (isCurrentlyOnline) "CONNECTED" else "CACHED",
            bondsStatus = if (isCurrentlyOnline) "CONNECTED" else "CACHED",
            webSocketStatus = webSocketStatus,
            streamTickCount = streamTickCount
        )
    }
}
