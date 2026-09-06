package com.example.personalvault.data.network.providers

import com.example.personalvault.data.models.CandleBar
import com.example.personalvault.data.models.DataFreshness
import com.example.personalvault.data.models.GlobalMarketItem
import com.example.personalvault.data.models.MarketCategory
import com.example.personalvault.data.models.MarketIndex
import com.example.personalvault.data.models.MarketRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PublicMarketDataProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) : MarketDataProvider {

    override val providerId: String = "yahoo_direct"
    override val providerName: String = "Global Real-Time Market Feed"
    override val requiresApiKey: Boolean = false

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private fun mapToTicker(symbol: String): String {
        return when (symbol.uppercase()) {
            "RELIANCE" -> "RELIANCE.NS"
            "TCS" -> "TCS.NS"
            "HDFCBANK" -> "HDFCBANK.NS"
            "INFY" -> "INFY.NS"
            "ICICIBANK" -> "ICICIBANK.NS"
            "BHARTIARTL" -> "BHARTIARTL.NS"
            "ITC" -> "ITC.NS"
            "SBIN" -> "SBIN.NS"
            "LT" -> "LT.NS"
            "TATAMOTORS" -> "TATAMOTORS.NS"
            "NIFTY 50", "NIFTY" -> "^NSEI"
            "BANK NIFTY", "BANKNIFTY" -> "^NSEBANK"
            "SENSEX" -> "^BSESN"
            "NIFTY IT" -> "^CNXIT"
            "NIFTY MIDCAP 100", "NIFTY MIDCAP" -> "^NSEMDCP50"
            "INDIA VIX" -> "^INDIAVIX"
            "S&P 500", "SPX" -> "^GSPC"
            "NASDAQ 100", "NDX" -> "^IXIC"
            "DOW JONES", "DJI" -> "^DJI"
            "RUSSELL 2000" -> "^RUT"
            "VIX" -> "^VIX"
            "S&P MIDCAP 400" -> "^MID"
            "DOLLAR INDEX", "DXY" -> "DX-Y.NYB"
            "DAX 40", "DAX" -> "^GDAXI"
            "FTSE 100" -> "^FTSE"
            "CAC 40" -> "^FCHI"
            "EURO STOXX 50" -> "^STOXX50E"
            "NIKKEI 225" -> "^N225"
            "HANG SENG" -> "^HSI"
            "SHANGHAI COMPOSITE" -> "000001.SS"
            "KOSPI" -> "^KS11"
            "TAIWAN WEIGHTED" -> "^TWII"
            "GOLD" -> "GC=F"
            "SILVER" -> "SI=F"
            "BRENT CRUDE" -> "BZ=F"
            "WTI CRUDE" -> "CL=F"
            "NATURAL GAS" -> "NG=F"
            "COPPER" -> "HG=F"
            "US 10Y YIELD" -> "^TNX"
            "US 2Y YIELD" -> "^IRX"
            "USD/INR" -> "USDINR=X"
            "EUR/USD" -> "EURUSD=X"
            "GBP/USD" -> "GBPUSD=X"
            "USD/JPY" -> "USDJPY=X"
            "BITCOIN", "BTC" -> "BTC-USD"
            "ETHEREUM", "ETH" -> "ETH-USD"
            "SOLANA", "SOL" -> "SOL-USD"
            else -> if (symbol.contains("^") || symbol.contains("=") || symbol.contains(".")) symbol else "${symbol}.NS"
        }
    }

    override suspend fun pingProvider(): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url("https://query1.finance.yahoo.com/v8/finance/chart/%5ENSEI?interval=1d&range=1d")
                .header("User-Agent", userAgent)
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
        val result = mutableMapOf<String, QuoteSnapshot>()
        for (sym in symbols) {
            val ticker = mapToTicker(sym)
            try {
                val quote = fetchSingleQuote(sym, ticker)
                if (quote != null) {
                    result[sym] = quote
                }
            } catch (_: Exception) {}
        }
        result
    }

    private fun fetchSingleQuote(originalSymbol: String, ticker: String): QuoteSnapshot? {
        val url = "https://query1.finance.yahoo.com/v8/finance/chart/$ticker?interval=15m&range=5d"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            val chart = json.getJSONObject("chart")
            val resultArray = chart.getJSONArray("result")
            if (resultArray.length() == 0) return null
            val obj = resultArray.getJSONObject(0)
            val meta = obj.getJSONObject("meta")

            val currentPrice = meta.optDouble("regularMarketPrice", 0.0)
            val prevClose = meta.optDouble("chartPreviousClose", currentPrice)
            val dayHigh = meta.optDouble("regularMarketDayHigh", currentPrice)
            val dayLow = meta.optDouble("regularMarketDayLow", currentPrice)
            val volume = meta.optLong("regularMarketVolume", 0L)

            if (currentPrice <= 0.0) return null

            val change = currentPrice - prevClose
            val pctChange = if (prevClose > 0) (change / prevClose) * 100 else 0.0

            // Extract sparkline from closes
            val sparkline = mutableListOf<Double>()
            val indicators = obj.optJSONObject("indicators")
            val quoteArr = indicators?.optJSONArray("quote")
            if (quoteArr != null && quoteArr.length() > 0) {
                val quoteObj = quoteArr.getJSONObject(0)
                val closes = quoteObj.optJSONArray("close")
                if (closes != null) {
                    for (i in 0 until closes.length()) {
                        val c = closes.optDouble(i, Double.NaN)
                        if (!c.isNaN()) sparkline.add(c)
                    }
                }
            }

            val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
            val nowTime = sdf.format(Date())

            return QuoteSnapshot(
                symbol = originalSymbol,
                price = ((currentPrice * 100).toInt()) / 100.0,
                change = ((change * 100).toInt()) / 100.0,
                percentChange = ((pctChange * 100).toInt()) / 100.0,
                dayHigh = ((dayHigh * 100).toInt()) / 100.0,
                dayLow = ((dayLow * 100).toInt()) / 100.0,
                prevClose = ((prevClose * 100).toInt()) / 100.0,
                volume = volume,
                freshness = DataFreshness.REAL_TIME,
                timestamp = nowTime,
                sparkline = if (sparkline.size > 10) sparkline.takeLast(20) else sparkline
            )
        }
    }

    override suspend fun fetchIndices(): List<MarketIndex> = withContext(Dispatchers.IO) {
        val indexTargets = listOf(
            Triple("NIFTY 50", "^NSEI", "NSE"),
            Triple("SENSEX", "^BSESN", "BSE"),
            Triple("BANK NIFTY", "^NSEBANK", "NSE"),
            Triple("NIFTY IT", "^CNXIT", "NSE"),
            Triple("NIFTY MIDCAP", "^NSEMDCP50", "NSE"),
            Triple("INDIA VIX", "^INDIAVIX", "NSE")
        )

        val list = mutableListOf<MarketIndex>()
        val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
        val nowTime = sdf.format(Date())

        for ((name, ticker, exchange) in indexTargets) {
            try {
                val q = fetchSingleQuote(name, ticker)
                if (q != null) {
                    list.add(
                        MarketIndex(
                            name = name,
                            value = q.price,
                            change = q.change,
                            percentChange = q.percentChange,
                            exchange = exchange,
                            freshness = DataFreshness.REAL_TIME,
                            updatedAt = nowTime
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        list
    }

    override suspend fun fetchGlobalMarkets(): List<GlobalMarketItem> = withContext(Dispatchers.IO) {
        data class GlobalSpec(
            val symbol: String,
            val name: String,
            val ticker: String,
            val region: MarketRegion,
            val category: MarketCategory,
            val currency: String,
            val exchange: String
        )

        val specs = listOf(
            // USA
            GlobalSpec("S&P 500", "S&P 500 Index", "^GSPC", MarketRegion.USA, MarketCategory.INDEX, "USD", "NYSE"),
            GlobalSpec("NASDAQ 100", "Nasdaq 100 Index", "^IXIC", MarketRegion.USA, MarketCategory.INDEX, "USD", "NASDAQ"),
            GlobalSpec("DOW JONES", "Dow Jones Industrial", "^DJI", MarketRegion.USA, MarketCategory.INDEX, "USD", "DJI"),
            GlobalSpec("RUSSELL 2000", "Russell 2000 Small Cap", "^RUT", MarketRegion.USA, MarketCategory.INDEX, "USD", "CBOE"),
            GlobalSpec("VIX", "CBOE Volatility Index", "^VIX", MarketRegion.USA, MarketCategory.INDEX, "USD", "CBOE"),
            GlobalSpec("S&P MIDCAP 400", "S&P MidCap 400", "^MID", MarketRegion.USA, MarketCategory.INDEX, "USD", "NYSE"),
            GlobalSpec("DXY", "US Dollar Index", "DX-Y.NYB", MarketRegion.USA, MarketCategory.FOREX, "USD", "ICE"),
            GlobalSpec("NVDA", "Nvidia Corporation", "NVDA", MarketRegion.USA, MarketCategory.EQUITY, "USD", "NASDAQ"),
            GlobalSpec("AAPL", "Apple Inc", "AAPL", MarketRegion.USA, MarketCategory.EQUITY, "USD", "NASDAQ"),
            GlobalSpec("MSFT", "Microsoft Corporation", "MSFT", MarketRegion.USA, MarketCategory.EQUITY, "USD", "NASDAQ"),

            // India
            GlobalSpec("NIFTY 50", "Nifty 50 Benchmark", "^NSEI", MarketRegion.INDIA, MarketCategory.INDEX, "INR", "NSE"),
            GlobalSpec("BANK NIFTY", "Nifty Bank Index", "^NSEBANK", MarketRegion.INDIA, MarketCategory.INDEX, "INR", "NSE"),
            GlobalSpec("SENSEX", "BSE Sensex 30", "^BSESN", MarketRegion.INDIA, MarketCategory.INDEX, "INR", "BSE"),
            GlobalSpec("NIFTY IT", "Nifty IT Sector", "^CNXIT", MarketRegion.INDIA, MarketCategory.INDEX, "INR", "NSE"),
            GlobalSpec("NIFTY MIDCAP", "Nifty Midcap 100", "^NSEMDCP50", MarketRegion.INDIA, MarketCategory.INDEX, "INR", "NSE"),
            GlobalSpec("INDIA VIX", "India Volatility Index", "^INDIAVIX", MarketRegion.INDIA, MarketCategory.INDEX, "INR", "NSE"),
            GlobalSpec("USD/INR", "US Dollar / Indian Rupee", "USDINR=X", MarketRegion.INDIA, MarketCategory.FOREX, "INR", "RBI"),

            // Europe
            GlobalSpec("DAX 40", "German DAX Index", "^GDAXI", MarketRegion.EUROPE, MarketCategory.INDEX, "EUR", "XETRA"),
            GlobalSpec("FTSE 100", "UK FTSE 100 Index", "^FTSE", MarketRegion.EUROPE, MarketCategory.INDEX, "GBP", "LSE"),
            GlobalSpec("CAC 40", "French CAC 40", "^FCHI", MarketRegion.EUROPE, MarketCategory.INDEX, "EUR", "EURONEXT"),
            GlobalSpec("EURO STOXX 50", "Euro Stoxx 50 Index", "^STOXX50E", MarketRegion.EUROPE, MarketCategory.INDEX, "EUR", "STOXX"),

            // Asia
            GlobalSpec("NIKKEI 225", "Japan Nikkei 225", "^N225", MarketRegion.ASIA, MarketCategory.INDEX, "JPY", "TSE"),
            GlobalSpec("HANG SENG", "Hong Kong Hang Seng", "^HSI", MarketRegion.ASIA, MarketCategory.INDEX, "HKD", "HKEX"),
            GlobalSpec("SHANGHAI", "Shanghai Composite", "000001.SS", MarketRegion.ASIA, MarketCategory.INDEX, "CNY", "SSE"),
            GlobalSpec("KOSPI", "Korea Composite", "^KS11", MarketRegion.ASIA, MarketCategory.INDEX, "KRW", "KRX"),
            GlobalSpec("TAIWAN", "Taiwan Weighted", "^TWII", MarketRegion.ASIA, MarketCategory.INDEX, "TWD", "TWSE"),

            // Commodities
            GlobalSpec("GOLD", "Gold Futures (oz)", "GC=F", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, "USD", "COMEX"),
            GlobalSpec("SILVER", "Silver Futures (oz)", "SI=F", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, "USD", "COMEX"),
            GlobalSpec("BRENT CRUDE", "Brent Crude Oil", "BZ=F", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, "USD", "ICE"),
            GlobalSpec("WTI CRUDE", "WTI Light Crude Oil", "CL=F", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, "USD", "NYMEX"),
            GlobalSpec("NATURAL GAS", "Natural Gas Henry Hub", "NG=F", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, "USD", "NYMEX"),
            GlobalSpec("COPPER", "High Grade Copper", "HG=F", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, "USD", "COMEX"),

            // Bonds
            GlobalSpec("US 10Y YIELD", "US 10-Year Treasury Yield", "^TNX", MarketRegion.BONDS, MarketCategory.YIELD, "%", "CBOE"),
            GlobalSpec("US 2Y YIELD", "US 2-Year Treasury Yield", "^IRX", MarketRegion.BONDS, MarketCategory.YIELD, "%", "CBOE"),

            // Forex
            GlobalSpec("EUR/USD", "Euro / US Dollar", "EURUSD=X", MarketRegion.FOREX, MarketCategory.FOREX, "USD", "FOREX"),
            GlobalSpec("GBP/USD", "British Pound / USD", "GBPUSD=X", MarketRegion.FOREX, MarketCategory.FOREX, "USD", "FOREX"),
            GlobalSpec("USD/JPY", "US Dollar / Japanese Yen", "USDJPY=X", MarketRegion.FOREX, MarketCategory.FOREX, "JPY", "FOREX"),

            // Crypto
            GlobalSpec("BITCOIN", "Bitcoin (USD)", "BTC-USD", MarketRegion.CRYPTO, MarketCategory.CRYPTO, "USD", "GLOBAL"),
            GlobalSpec("ETHEREUM", "Ethereum (USD)", "ETH-USD", MarketRegion.CRYPTO, MarketCategory.CRYPTO, "USD", "GLOBAL"),
            GlobalSpec("SOLANA", "Solana (USD)", "SOL-USD", MarketRegion.CRYPTO, MarketCategory.CRYPTO, "USD", "GLOBAL")
        )

        val results = mutableListOf<GlobalMarketItem>()
        val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
        val nowTime = sdf.format(Date())

        for (spec in specs) {
            try {
                val q = fetchSingleQuote(spec.symbol, spec.ticker)
                if (q != null) {
                    results.add(
                        GlobalMarketItem(
                            symbol = spec.symbol,
                            name = spec.name,
                            region = spec.region,
                            category = spec.category,
                            price = q.price,
                            change = q.change,
                            percentChange = q.percentChange,
                            currency = spec.currency,
                            exchange = spec.exchange,
                            freshness = DataFreshness.REAL_TIME,
                            updatedAt = nowTime,
                            sparkline = q.sparkline,
                            high24h = q.dayHigh,
                            low24h = q.dayLow
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        results
    }

    override suspend fun fetchHistoricalCandles(symbol: String, range: String): List<CandleBar> = withContext(Dispatchers.IO) {
        val ticker = mapToTicker(symbol)
        val yfRange = when (range.uppercase()) {
            "1Y" -> "1y"
            "3Y" -> "3y"
            "5Y" -> "5y"
            "MAX" -> "max"
            else -> "1y"
        }

        val url = "https://query1.finance.yahoo.com/v8/finance/chart/$ticker?interval=1d&range=$yfRange"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .build()

        val candles = mutableListOf<CandleBar>()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val chart = json.getJSONObject("chart")
                val resultArray = chart.getJSONArray("result")
                if (resultArray.length() == 0) return@withContext emptyList()
                val obj = resultArray.getJSONObject(0)

                val timestamps = obj.optJSONArray("timestamp") ?: return@withContext emptyList()
                val indicators = obj.getJSONObject("indicators")
                val quoteArr = indicators.getJSONArray("quote")
                if (quoteArr.length() == 0) return@withContext emptyList()
                val quoteObj = quoteArr.getJSONObject(0)

                val opens = quoteObj.optJSONArray("open")
                val highs = quoteObj.optJSONArray("high")
                val lows = quoteObj.optJSONArray("low")
                val closes = quoteObj.optJSONArray("close")
                val volumes = quoteObj.optJSONArray("volume")

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                for (i in 0 until timestamps.length()) {
                    val ts = timestamps.optLong(i, 0L)
                    val c = closes?.optDouble(i, Double.NaN) ?: Double.NaN
                    val o = opens?.optDouble(i, Double.NaN) ?: c
                    val h = highs?.optDouble(i, Double.NaN) ?: c
                    val l = lows?.optDouble(i, Double.NaN) ?: c
                    val v = volumes?.optLong(i, 0L) ?: 0L

                    if (!c.isNaN() && !o.isNaN() && ts > 0) {
                        val dateStr = sdf.format(Date(ts * 1000L))
                        candles.add(
                            CandleBar(
                                timestamp = ts * 1000L,
                                dateStr = dateStr,
                                open = ((o * 100).toInt()) / 100.0,
                                high = ((h * 100).toInt()) / 100.0,
                                low = ((l * 100).toInt()) / 100.0,
                                close = ((c * 100).toInt()) / 100.0,
                                volume = v
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        candles
    }
}
