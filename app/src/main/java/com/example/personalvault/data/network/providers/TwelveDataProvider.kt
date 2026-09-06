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

class TwelveDataProvider(
    private val apiKey: String,
    private val fallbackProvider: MarketDataProvider = PublicMarketDataProvider(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) : MarketDataProvider {

    override val providerId: String = "twelve_data"
    override val providerName: String = "Twelve Data Pro Feed"
    override val requiresApiKey: Boolean = true

    override suspend fun pingProvider(): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val keyParam = if (apiKey.isNotBlank()) "&apikey=$apiKey" else ""
            val request = Request.Builder()
                .url("https://api.twelvedata.com/price?symbol=AAPL$keyParam")
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
                val querySymbol = if (sym.contains(":") || sym.contains(".")) sym else "$sym:NSE"
                val url = "https://api.twelvedata.com/quote?symbol=$querySymbol&apikey=$apiKey"
                val request = Request.Builder().url(url).build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@use
                        val json = JSONObject(body)
                        if (json.has("price") || json.has("close")) {
                            val price = json.optDouble("close", json.optDouble("price", 0.0))
                            val prevClose = json.optDouble("previous_close", price)
                            val change = json.optDouble("change", price - prevClose)
                            val pctChange = json.optDouble("percent_change", 0.0)
                            val high = json.optDouble("high", price)
                            val low = json.optDouble("low", price)
                            val vol = json.optLong("volume", 0L)

                            val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
                            val nowTime = sdf.format(Date())

                            val isIndian = sym.endsWith(":NSE") || !sym.contains(":")
                            val freshness = if (isIndian && !apiKey.contains("pro", ignoreCase = true)) DataFreshness.DELAYED_15M else DataFreshness.REAL_TIME

                            result[sym] = QuoteSnapshot(
                                symbol = sym,
                                price = ((price * 100).toInt()) / 100.0,
                                change = ((change * 100).toInt()) / 100.0,
                                percentChange = ((pctChange * 100).toInt()) / 100.0,
                                dayHigh = ((high * 100).toInt()) / 100.0,
                                dayLow = ((low * 100).toInt()) / 100.0,
                                prevClose = ((prevClose * 100).toInt()) / 100.0,
                                volume = vol,
                                freshness = freshness,
                                timestamp = nowTime,
                                source = "Twelve Data",
                                lastUpdateEpochMillis = System.currentTimeMillis()
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // Fill remaining from fallback
        val missing = symbols.filter { !result.containsKey(it) }
        if (missing.isNotEmpty()) {
            val fallbackQuotes = fallbackProvider.fetchQuotes(missing)
            result.putAll(fallbackQuotes)
        }

        result
    }

    override suspend fun fetchIndices(): List<MarketIndex> {
        return fallbackProvider.fetchIndices()
    }

    override suspend fun fetchGlobalMarkets(): List<GlobalMarketItem> {
        return fallbackProvider.fetchGlobalMarkets()
    }

    override suspend fun fetchHistoricalCandles(symbol: String, range: String): List<CandleBar> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext fallbackProvider.fetchHistoricalCandles(symbol, range)
        }

        try {
            val querySymbol = if (symbol.contains(":") || symbol.contains(".")) symbol else "$symbol:NSE"
            val outputSize = when (range) {
                "1Y" -> 250
                "3Y" -> 750
                "5Y" -> 1250
                else -> 250
            }
            val url = "https://api.twelvedata.com/time_series?symbol=$querySymbol&interval=1day&outputsize=$outputSize&apikey=$apiKey"
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    if (json.has("values")) {
                        val values = json.getJSONArray("values")
                        val candles = mutableListOf<CandleBar>()
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                        for (i in (values.length() - 1) downTo 0) {
                            val item = values.getJSONObject(i)
                            val datetime = item.getString("datetime")
                            val o = item.getDouble("open")
                            val h = item.getDouble("high")
                            val l = item.getDouble("low")
                            val c = item.getDouble("close")
                            val v = item.optLong("volume", 0L)
                            val parsedDate = sdf.parse(datetime.substringBefore(" "))
                            val ts = parsedDate?.time ?: System.currentTimeMillis()

                            candles.add(
                                CandleBar(
                                    timestamp = ts,
                                    dateStr = datetime.substringBefore(" "),
                                    open = o,
                                    high = h,
                                    low = l,
                                    close = c,
                                    volume = v
                                )
                            )
                        }
                        if (candles.isNotEmpty()) return@withContext candles
                    }
                }
            }
        } catch (_: Exception) {}

        fallbackProvider.fetchHistoricalCandles(symbol, range)
    }
}
