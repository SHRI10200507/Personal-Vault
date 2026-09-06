package com.example.personalvault.data.network.providers

import com.example.personalvault.data.models.CandleBar
import com.example.personalvault.data.models.DataFreshness
import com.example.personalvault.data.models.GlobalMarketItem
import com.example.personalvault.data.models.MarketIndex

data class QuoteSnapshot(
    val symbol: String,
    val price: Double,
    val change: Double,
    val percentChange: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val prevClose: Double,
    val volume: Long,
    val freshness: DataFreshness,
    val timestamp: String,
    val sparkline: List<Double> = emptyList(),
    val source: String = "",
    val lastUpdateEpochMillis: Long = System.currentTimeMillis()
)

interface MarketDataProvider {
    val providerId: String
    val providerName: String
    val requiresApiKey: Boolean

    /**
     * Pings the actual provider API endpoint to measure authentic network latency in milliseconds.
     * Returns Pair(isConnected, latencyMs)
     */
    suspend fun pingProvider(): Pair<Boolean, Long>

    /**
     * Fetches live or 15m-delayed quotes for given equity symbols.
     */
    suspend fun fetchQuotes(symbols: List<String>): Map<String, QuoteSnapshot>

    /**
     * Fetches live or delayed benchmark indices (NIFTY 50, SENSEX, BANK NIFTY, NIFTY IT, etc.)
     */
    suspend fun fetchIndices(): List<MarketIndex>

    /**
     * Fetches live global markets covering USA, Europe, Asia, Forex, Commodities, Bonds & Crypto.
     */
    suspend fun fetchGlobalMarkets(): List<GlobalMarketItem>

    /**
     * Fetches authentic historical OHLCV daily candles for a symbol over a requested range.
     * range: "1Y", "3Y", "5Y", "MAX"
     */
    suspend fun fetchHistoricalCandles(symbol: String, range: String = "1Y"): List<CandleBar>
}
