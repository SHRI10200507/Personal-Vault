package com.example.personalvault.data.network.providers

import com.example.personalvault.data.models.CandleBar
import com.example.personalvault.data.models.DataFreshness
import com.example.personalvault.data.models.GlobalMarketItem
import com.example.personalvault.data.models.MarketIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AlphaVantageProvider(
    private val apiKey: String,
    private val fallbackProvider: MarketDataProvider = PublicMarketDataProvider(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) : MarketDataProvider {

    override val providerId: String = "alpha_vantage"
    override val providerName: String = "Alpha Vantage Global Feed"
    override val requiresApiKey: Boolean = true

    override suspend fun pingProvider(): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val keyParam = if (apiKey.isNotBlank()) "&apikey=$apiKey" else "&apikey=demo"
            val request = Request.Builder()
                .url("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=IBM$keyParam")
                .build()
            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - start
                Pair(response.isSuccessful, latency)
            }
        } catch (_: Exception) {
            Pair(false, 0L)
        }
    }

    override suspend fun fetchQuotes(symbols: List<String>): Map<String, QuoteSnapshot> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext fallbackProvider.fetchQuotes(symbols)
        }

        val result = mutableMapOf<String, QuoteSnapshot>()
        for (sym in symbols) {
            try {
                val querySymbol = if (sym.contains(".")) sym else "$sym.BSE"
                val url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=$querySymbol&apikey=$apiKey"
                val request = Request.Builder().url(url).build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@use
                        val json = JSONObject(body)
                        if (json.has("Global Quote")) {
                            val gq = json.getJSONObject("Global Quote")
                            val price = gq.optDouble("05. price", 0.0)
                            if (price > 0) {
                                val prevClose = gq.optDouble("08. previous close", price)
                                val change = gq.optDouble("09. change", price - prevClose)
                                val pctStr = gq.optString("10. change percent", "0%").replace("%", "")
                                val pctChange = pctStr.toDoubleOrNull() ?: 0.0
                                val high = gq.optDouble("03. high", price)
                                val low = gq.optDouble("04. low", price)
                                val vol = gq.optLong("06. volume", 0L)

                                val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
                                val nowTime = sdf.format(Date())

                                result[sym] = QuoteSnapshot(
                                    symbol = sym,
                                    price = ((price * 100).toInt()) / 100.0,
                                    change = ((change * 100).toInt()) / 100.0,
                                    percentChange = ((pctChange * 100).toInt()) / 100.0,
                                    dayHigh = ((high * 100).toInt()) / 100.0,
                                    dayLow = ((low * 100).toInt()) / 100.0,
                                    prevClose = ((prevClose * 100).toInt()) / 100.0,
                                    volume = vol,
                                    freshness = DataFreshness.REAL_TIME,
                                    timestamp = nowTime
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        val missing = symbols.filter { !result.containsKey(it) }
        if (missing.isNotEmpty()) {
            val fallbackQuotes = fallbackProvider.fetchQuotes(missing)
            result.putAll(fallbackQuotes)
        }

        result
    }

    override suspend fun fetchIndices(): List<MarketIndex> = fallbackProvider.fetchIndices()

    override suspend fun fetchGlobalMarkets(): List<GlobalMarketItem> = fallbackProvider.fetchGlobalMarkets()

    override suspend fun fetchHistoricalCandles(symbol: String, range: String): List<CandleBar> {
        return fallbackProvider.fetchHistoricalCandles(symbol, range)
    }
}
