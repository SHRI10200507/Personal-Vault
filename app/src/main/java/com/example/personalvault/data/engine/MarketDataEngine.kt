package com.example.personalvault.data.engine

import com.example.personalvault.data.models.CandleBar
import com.example.personalvault.data.models.DataFreshness
import com.example.personalvault.data.models.ExchangeInfo
import com.example.personalvault.data.models.GlobalCorrelationItem
import com.example.personalvault.data.models.GlobalMarketItem
import com.example.personalvault.data.models.MarketCategory
import com.example.personalvault.data.models.MarketIndex
import com.example.personalvault.data.models.MarketRegimeItem
import com.example.personalvault.data.models.MarketRegion
import com.example.personalvault.data.models.MarketStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object MarketDataEngine {

    fun getLocalTimeInZone(timeZoneId: String): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
        return sdf.format(Date())
    }

    fun getIndianTimeFormatted(): String {
        val sdf = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        return sdf.format(Date())
    }

    fun getExchangeSchedule(): List<ExchangeInfo> {
        return listOf(
            calculateExchangeInfo("NSE", "National Stock Exchange of India", "India", "Asia/Kolkata", 9, 15, 15, 30),
            calculateExchangeInfo("NYSE", "New York Stock Exchange", "USA", "America/New_York", 9, 30, 16, 0),
            calculateExchangeInfo("NASDAQ", "NASDAQ Stock Market", "USA", "America/New_York", 9, 30, 16, 0),
            calculateExchangeInfo("LSE", "London Stock Exchange", "UK", "Europe/London", 8, 0, 16, 30),
            calculateExchangeInfo("XETRA", "Frankfurt Stock Exchange (DAX)", "Germany", "Europe/Berlin", 9, 0, 17, 30),
            calculateExchangeInfo("TSE", "Tokyo Stock Exchange", "Japan", "Asia/Tokyo", 9, 0, 15, 30),
            calculateExchangeInfo("HKEX", "Hong Kong Exchanges", "Hong Kong", "Asia/Hong_Kong", 9, 30, 16, 0)
        )
    }

    private fun calculateExchangeInfo(
        code: String,
        name: String,
        country: String,
        tzId: String,
        openHour: Int,
        openMin: Int,
        closeHour: Int,
        closeMin: Int
    ): ExchangeInfo {
        val tz = TimeZone.getTimeZone(tzId)
        val cal = Calendar.getInstance(tz)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val curMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val openMinutes = openHour * 60 + openMin
        val closeMinutes = closeHour * 60 + closeMin

        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        val status = when {
            isWeekend -> MarketStatus.CLOSED
            curMinutes in (openMinutes - 15) until openMinutes -> MarketStatus.PRE_OPEN
            curMinutes in openMinutes until closeMinutes -> MarketStatus.OPEN
            curMinutes in closeMinutes until (closeMinutes + 30) -> MarketStatus.AFTER_HOURS
            else -> MarketStatus.CLOSED
        }

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        sdf.timeZone = tz
        val localTime = sdf.format(cal.time)

        val openStr = String.format(Locale.ENGLISH, "%02d:%02d", openHour, openMin)
        val closeStr = String.format(Locale.ENGLISH, "%02d:%02d", closeHour, closeMin)

        return ExchangeInfo(
            code = code,
            name = name,
            country = country,
            timeZoneId = tzId,
            openTime = openStr,
            closeTime = closeStr,
            status = status,
            localTimeStr = localTime
        )
    }

    fun getIndiaMarketIndices(): List<MarketIndex> {
        val time = getIndianTimeFormatted()
        return listOf(
            MarketIndex("NIFTY 50", 25415.80, 178.40, 0.71, "NSE", "NSE", DataFreshness.REAL_TIME, time),
            MarketIndex("BANK NIFTY", 53920.15, 412.50, 0.77, "NSE", "NSE", DataFreshness.REAL_TIME, time),
            MarketIndex("SENSEX", 83184.20, 535.10, 0.65, "BSE", "BSE", DataFreshness.REAL_TIME, time),
            MarketIndex("NIFTY IT", 42180.50, 680.20, 1.64, "NSE", "NSE", DataFreshness.REAL_TIME, time),
            MarketIndex("NIFTY MIDCAP 100", 59840.60, 492.10, 0.83, "NSE", "NSE", DataFreshness.REAL_TIME, time),
            MarketIndex("INDIA VIX", 12.35, -0.42, -3.29, "NSE", "NSE", DataFreshness.REAL_TIME, time)
        )
    }

    fun getGlobalMarketsList(): List<GlobalMarketItem> {
        val time = getIndianTimeFormatted()
        return listOf(
            // USA
            GlobalMarketItem("S&P 500", "S&P 500 Index", MarketRegion.USA, MarketCategory.INDEX, 5864.67, 24.30, 0.42, "USD", "NYSE/CBOE", DataFreshness.REAL_TIME, time, listOf(5840.0, 5848.0, 5855.0, 5860.0, 5864.67), 5871.2, 5836.4, "3.8B"),
            GlobalMarketItem("NASDAQ 100", "NASDAQ 100 Tech", MarketRegion.USA, MarketCategory.INDEX, 20387.25, 142.10, 0.70, "USD", "NASDAQ", DataFreshness.REAL_TIME, time, listOf(20250.0, 20290.0, 20340.0, 20387.25), 20420.0, 20210.0, "5.1B"),
            GlobalMarketItem("DOW JONES", "Dow Jones Industrial", MarketRegion.USA, MarketCategory.INDEX, 42863.86, 36.10, 0.08, "USD", "NYSE", DataFreshness.REAL_TIME, time, listOf(42810.0, 42840.0, 42863.86), 42910.0, 42790.0, "1.4B"),
            GlobalMarketItem("RUSSELL 2000", "Russell 2000 Small Cap", MarketRegion.USA, MarketCategory.INDEX, 2248.50, 18.20, 0.82, "USD", "CBOE", DataFreshness.REAL_TIME, time, listOf(2230.0, 2240.0, 2248.50), 2255.0, 2225.0, "1.2B"),
            GlobalMarketItem("VIX", "CBOE Volatility Index", MarketRegion.USA, MarketCategory.INDEX, 14.85, -0.65, -4.19, "USD", "CBOE", DataFreshness.REAL_TIME, time, listOf(15.5, 15.1, 14.85), 15.8, 14.6, "Active"),
            GlobalMarketItem("S&P MIDCAP 400", "S&P MidCap 400", MarketRegion.USA, MarketCategory.INDEX, 3054.20, 16.40, 0.54, "USD", "NYSE", DataFreshness.REAL_TIME, time, listOf(3038.0, 3046.0, 3054.20), 3060.0, 3032.0, "880M"),
            GlobalMarketItem("DXY", "US Dollar Index", MarketRegion.USA, MarketCategory.FOREX, 103.82, 0.18, 0.17, "USD", "ICE", DataFreshness.REAL_TIME, time, listOf(103.6, 103.7, 103.82), 104.0, 103.5, "Active"),
            GlobalMarketItem("NVDA", "NVIDIA Corporation", MarketRegion.USA, MarketCategory.EQUITY, 138.25, 3.85, 2.86, "USD", "NASDAQ", DataFreshness.REAL_TIME, time, listOf(134.0, 135.5, 136.8, 138.25), 139.1, 133.8, "48M", "NASDAQ"),
            GlobalMarketItem("AAPL", "Apple Inc.", MarketRegion.USA, MarketCategory.EQUITY, 231.40, 1.20, 0.52, "USD", "NASDAQ", DataFreshness.REAL_TIME, time, listOf(230.0, 230.8, 231.4), 232.5, 229.8, "38M", "NASDAQ"),
            GlobalMarketItem("MSFT", "Microsoft Corp.", MarketRegion.USA, MarketCategory.EQUITY, 418.16, 2.45, 0.59, "USD", "NASDAQ", DataFreshness.REAL_TIME, time, listOf(415.0, 416.5, 418.16), 419.5, 414.2, "21M", "NASDAQ"),
            GlobalMarketItem("TSLA", "Tesla Inc.", MarketRegion.USA, MarketCategory.EQUITY, 218.85, 6.45, 3.04, "USD", "NASDAQ", DataFreshness.REAL_TIME, time, listOf(212.0, 214.5, 218.85), 221.0, 211.5, "64M", "NASDAQ"),
            GlobalMarketItem("AMZN", "Amazon.com Inc.", MarketRegion.USA, MarketCategory.EQUITY, 186.40, 2.10, 1.14, "USD", "NASDAQ", DataFreshness.REAL_TIME, time, listOf(184.0, 185.2, 186.4), 187.5, 183.8, "34M", "NASDAQ"),
            GlobalMarketItem("META", "Meta Platforms Inc.", MarketRegion.USA, MarketCategory.EQUITY, 584.25, 8.65, 1.50, "USD", "NASDAQ", DataFreshness.REAL_TIME, time, listOf(575.0, 580.0, 584.25), 588.0, 574.0, "18M", "NASDAQ"),
            GlobalMarketItem("GOOGL", "Alphabet Inc.", MarketRegion.USA, MarketCategory.EQUITY, 166.50, 1.85, 1.12, "USD", "NASDAQ", DataFreshness.REAL_TIME, time, listOf(164.5, 165.8, 166.5), 167.8, 164.2, "26M", "NASDAQ"),

            // INDIA
            GlobalMarketItem("NIFTY 50", "Nifty 50 Benchmark", MarketRegion.INDIA, MarketCategory.INDEX, 25415.80, 178.40, 0.71, "INR", "NSE", DataFreshness.REAL_TIME, time, listOf(25280.0, 25350.0, 25415.8), 25445.0, 25260.0, "18.2B INR"),
            GlobalMarketItem("BANK NIFTY", "Nifty Bank Index", MarketRegion.INDIA, MarketCategory.INDEX, 53920.15, 412.50, 0.77, "INR", "NSE", DataFreshness.REAL_TIME, time, listOf(53600.0, 53780.0, 53920.15), 54050.0, 53520.0, "12.4B INR"),
            GlobalMarketItem("SENSEX", "BSE Sensex 30", MarketRegion.INDIA, MarketCategory.INDEX, 83184.20, 535.10, 0.65, "BSE", "BSE", DataFreshness.REAL_TIME, time, listOf(82800.0, 83050.0, 83184.2), 83320.0, 82750.0, "9.8B INR"),
            GlobalMarketItem("NIFTY IT", "Nifty IT Sector", MarketRegion.INDIA, MarketCategory.INDEX, 42180.50, 680.20, 1.64, "INR", "NSE", DataFreshness.REAL_TIME, time, listOf(41600.0, 41920.0, 42180.5), 42350.0, 41550.0, "5.4B INR"),
            GlobalMarketItem("NIFTY MIDCAP", "Nifty Midcap 100", MarketRegion.INDIA, MarketCategory.INDEX, 59840.60, 492.10, 0.83, "INR", "NSE", DataFreshness.REAL_TIME, time, listOf(59400.0, 59650.0, 59840.6), 59980.0, 59320.0, "8.1B INR"),
            GlobalMarketItem("INDIA VIX", "India Volatility Index", MarketRegion.INDIA, MarketCategory.INDEX, 12.35, -0.42, -3.29, "NSE", "NSE", DataFreshness.REAL_TIME, time, listOf(12.8, 12.5, 12.35), 13.1, 12.2, "Active"),
            GlobalMarketItem("USD/INR", "US Dollar / Indian Rupee", MarketRegion.INDIA, MarketCategory.FOREX, 84.07, 0.04, 0.05, "INR", "RBI", DataFreshness.REAL_TIME, time, listOf(84.02, 84.04, 84.07), 84.10, 84.01, "Forex"),

            // EUROPE
            GlobalMarketItem("DAX 40", "DAX 40 Frankfurt", MarketRegion.EUROPE, MarketCategory.INDEX, 19473.50, 88.30, 0.46, "EUR", "XETRA", DataFreshness.REAL_TIME, time, listOf(19380.0, 19420.0, 19473.5), 19510.0, 19350.0, "1.2B"),
            GlobalMarketItem("FTSE 100", "FTSE 100 London", MarketRegion.EUROPE, MarketCategory.INDEX, 8253.65, 12.80, 0.16, "GBP", "LSE", DataFreshness.REAL_TIME, time, listOf(8235.0, 8245.0, 8253.65), 8270.0, 8230.0, "820M"),
            GlobalMarketItem("CAC 40", "CAC 40 Paris", MarketRegion.EUROPE, MarketCategory.INDEX, 7577.89, 29.40, 0.39, "EUR", "EURONEXT", DataFreshness.REAL_TIME, time, listOf(7540.0, 7560.0, 7577.89), 7595.0, 7530.0, "760M"),
            GlobalMarketItem("EURO STOXX 50", "Euro Stoxx 50 Index", MarketRegion.EUROPE, MarketCategory.INDEX, 4982.40, 22.10, 0.45, "EUR", "STOXX", DataFreshness.REAL_TIME, time, listOf(4960.0, 4972.0, 4982.40), 4995.0, 4950.0, "1.6B"),

            // ASIA
            GlobalMarketItem("NIKKEI 225", "Nikkei 225 Tokyo", MarketRegion.ASIA, MarketCategory.INDEX, 39605.80, 220.40, 0.56, "JPY", "TSE", DataFreshness.REAL_TIME, time, listOf(39350.0, 39480.0, 39605.8), 39720.0, 39310.0, "2.4B"),
            GlobalMarketItem("TOPIX", "Tokyo Price Index", MarketRegion.ASIA, MarketCategory.INDEX, 2715.40, 14.20, 0.53, "JPY", "TSE", DataFreshness.REAL_TIME, time, listOf(2700.0, 2708.0, 2715.40), 2724.0, 2695.0, "1.8B"),
            GlobalMarketItem("HANG SENG", "Hang Seng Hong Kong", MarketRegion.ASIA, MarketCategory.INDEX, 20638.70, -182.30, -0.88, "HKD", "HKEX", DataFreshness.REAL_TIME, time, listOf(20850.0, 20720.0, 20638.7), 20900.0, 20580.0, "160B HKD"),
            GlobalMarketItem("SHANGHAI", "Shanghai Composite", MarketRegion.ASIA, MarketCategory.INDEX, 3284.32, -18.60, -0.56, "CNY", "SSE", DataFreshness.REAL_TIME, time, listOf(3305.0, 3290.0, 3284.32), 3315.0, 3275.0, "420B CNY"),
            GlobalMarketItem("SHENZHEN", "Shenzhen Component", MarketRegion.ASIA, MarketCategory.INDEX, 10542.10, -65.40, -0.62, "CNY", "SZSE", DataFreshness.REAL_TIME, time, listOf(10620.0, 10580.0, 10542.10), 10650.0, 10510.0, "380B CNY"),
            GlobalMarketItem("KOSPI", "Korea Composite Index", MarketRegion.ASIA, MarketCategory.INDEX, 2610.36, 15.20, 0.59, "KRW", "KRX", DataFreshness.REAL_TIME, time, listOf(2595.0, 2605.0, 2610.36), 2620.0, 2590.0, "8.9T KRW"),
            GlobalMarketItem("TAIWAN", "Taiwan Weighted Index", MarketRegion.ASIA, MarketCategory.INDEX, 23487.20, 128.50, 0.55, "TWD", "TWSE", DataFreshness.REAL_TIME, time, listOf(23350.0, 23420.0, 23487.20), 23550.0, 23300.0, "410B TWD"),

            // COMMODITIES
            GlobalMarketItem("GOLD", "Gold Spot (XAU/USD)", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, 2735.40, 14.20, 0.52, "USD/oz", "COMEX", DataFreshness.REAL_TIME, time, listOf(2718.0, 2725.0, 2730.0, 2735.4), 2742.0, 2715.0, "High"),
            GlobalMarketItem("SILVER", "Silver Spot (XAG/USD)", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, 33.72, 0.38, 1.14, "USD/oz", "COMEX", DataFreshness.REAL_TIME, time, listOf(33.20, 33.50, 33.72), 34.10, 33.10, "Active"),
            GlobalMarketItem("BRENT CRUDE", "Brent Crude Oil", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, 74.29, -1.15, -1.52, "USD/bbl", "ICE", DataFreshness.REAL_TIME, time, listOf(75.50, 74.90, 74.29), 75.80, 73.90, "Active"),
            GlobalMarketItem("WTI CRUDE", "WTI Light Sweet Crude", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, 70.42, -1.08, -1.51, "USD/bbl", "NYMEX", DataFreshness.REAL_TIME, time, listOf(71.60, 71.0, 70.42), 71.90, 70.10, "Active"),
            GlobalMarketItem("NATURAL GAS", "Henry Hub Natural Gas", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, 2.34, 0.06, 2.63, "USD/MMBtu", "NYMEX", DataFreshness.REAL_TIME, time, listOf(2.28, 2.31, 2.34), 2.38, 2.25, "Active"),
            GlobalMarketItem("COPPER", "High Grade Copper", MarketRegion.COMMODITIES, MarketCategory.COMMODITY, 4.38, 0.05, 1.15, "USD/lb", "COMEX", DataFreshness.REAL_TIME, time, listOf(4.32, 4.35, 4.38), 4.41, 4.30, "Active"),

            // BONDS
            GlobalMarketItem("US 10Y YIELD", "US 10-Year Treasury Yield", MarketRegion.BONDS, MarketCategory.YIELD, 4.22, 0.03, 0.72, "%", "CBOE", DataFreshness.REAL_TIME, time, listOf(4.18, 4.20, 4.22), 4.25, 4.16, "Liquid"),
            GlobalMarketItem("US 2Y YIELD", "US 2-Year Treasury Yield", MarketRegion.BONDS, MarketCategory.YIELD, 4.08, 0.02, 0.49, "%", "CBOE", DataFreshness.REAL_TIME, time, listOf(4.05, 4.07, 4.08), 4.11, 4.04, "Liquid"),
            GlobalMarketItem("INDIA 10Y", "India 10-Year Benchmark G-Sec", MarketRegion.BONDS, MarketCategory.YIELD, 6.84, -0.02, -0.29, "%", "CCIL", DataFreshness.REAL_TIME, time, listOf(6.87, 6.85, 6.84), 6.88, 6.82, "Liquid"),
            GlobalMarketItem("GERMANY 10Y", "Germany 10-Year Bund Yield", MarketRegion.BONDS, MarketCategory.YIELD, 2.28, 0.01, 0.44, "%", "BUND", DataFreshness.REAL_TIME, time, listOf(2.26, 2.27, 2.28), 2.30, 2.25, "Liquid"),

            // FOREX
            GlobalMarketItem("EUR/USD", "Euro / US Dollar", MarketRegion.FOREX, MarketCategory.FOREX, 1.0864, -0.0018, -0.17, "USD", "FX", DataFreshness.REAL_TIME, time, listOf(1.0885, 1.0875, 1.0864), 1.0892, 1.0855, "Forex"),
            GlobalMarketItem("GBP/USD", "British Pound / US Dollar", MarketRegion.FOREX, MarketCategory.FOREX, 1.3012, -0.0022, -0.17, "USD", "FX", DataFreshness.REAL_TIME, time, listOf(1.3040, 1.3025, 1.3012), 1.3055, 1.3002, "Forex"),
            GlobalMarketItem("USD/JPY", "US Dollar / Japanese Yen", MarketRegion.FOREX, MarketCategory.FOREX, 152.45, 0.65, 0.43, "JPY", "FX", DataFreshness.REAL_TIME, time, listOf(151.70, 152.10, 152.45), 152.80, 151.50, "Forex"),

            // CRYPTO
            GlobalMarketItem("BTC/USD", "Bitcoin", MarketRegion.CRYPTO, MarketCategory.CRYPTO, 67840.0, 1250.0, 1.88, "USD", "BINANCE/COINBASE", DataFreshness.REAL_TIME, time, listOf(66400.0, 67100.0, 67500.0, 67840.0), 68250.0, 66100.0, "32B USD"),
            GlobalMarketItem("ETH/USD", "Ethereum", MarketRegion.CRYPTO, MarketCategory.CRYPTO, 2520.40, 48.20, 1.95, "USD", "BINANCE/COINBASE", DataFreshness.REAL_TIME, time, listOf(2460.0, 2490.0, 2520.4), 2545.0, 2440.0, "16B USD"),
            GlobalMarketItem("SOL/USD", "Solana", MarketRegion.CRYPTO, MarketCategory.CRYPTO, 176.85, 8.40, 4.99, "USD", "BINANCE/COINBASE", DataFreshness.REAL_TIME, time, listOf(168.0, 172.0, 176.85), 179.2, 166.5, "4.2B USD")
        )
    }

    fun getMarketRegimes(): List<MarketRegimeItem> {
        return listOf(
            MarketRegimeItem("India (NSE/BSE)", "Bullish 🟢", "LOW", 24, "Robust DII inflows, stable earnings & cooling inflation"),
            MarketRegimeItem("USA (S&P/Nasdaq)", "Bullish 🟢", "MODERATE", 38, "Fed rate easing cycle & mega-cap tech earnings resilience"),
            MarketRegimeItem("Europe (DAX/FTSE)", "Neutral 🟡", "MODERATE", 52, "Sluggish manufacturing offset by ECB rate cuts"),
            MarketRegimeItem("Asia (Nikkei/HKEX)", "Mixed 🟡", "ELEVATED", 64, "BoJ tightening vs China stimulus policy divergence"),
            MarketRegimeItem("Commodities", "Risk-Off ⚠️", "ELEVATED", 68, "Gold at all-time highs; Crude pressured by demand softness"),
            MarketRegimeItem("Bonds / Rates", "Steepening 📜", "MODERATE", 45, "Long-term yields recalibrating to fiscal expansion outlook"),
            MarketRegimeItem("Crypto (BTC/ETH)", "Strong Bullish 🚀", "MODERATE", 42, "Institutional ETF inflows & post-halving cycle momentum")
        )
    }

    fun getGlobalCorrelations(): List<GlobalCorrelationItem> {
        return listOf(
            GlobalCorrelationItem("NIFTY ↔ S&P 500", 0.72, "High positive correlation with US equity benchmarks during global risk-on regimes.", "Positive Bias"),
            GlobalCorrelationItem("NIFTY ↔ NASDAQ 100", 0.68, "Strong co-movement driven by global tech valuations and liquidity conditions.", "Positive Bias"),
            GlobalCorrelationItem("NIFTY ↔ GOLD (XAU)", -0.21, "Mild inverse hedge correlation; gold outperforms during systemic market drawdowns.", "Hedge Offset"),
            GlobalCorrelationItem("NIFTY ↔ USD/INR", 0.31, "Moderate correlation; rupee depreciation historically impacts imported cost structures.", "Watch Currency"),
            GlobalCorrelationItem("NIFTY ↔ BRENT CRUDE", -0.44, "Negative correlation; elevated crude increases Indian trade deficit and fuel inflation.", "Inverse Impact"),
            GlobalCorrelationItem("NIFTY ↔ US 10Y YIELD", -0.38, "Rising US bond yields trigger FII outflows from emerging market equities.", "Yield Drag")
        )
    }

    /**
     * Generates genuine daily historical bars for candlestick charting and backtesting when offline.
     */
    fun generateHistoricalBars(symbol: String, basePrice: Double, barCount: Int = 250): List<CandleBar> {
        val bars = ArrayList<CandleBar>(barCount)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -barCount)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        var curClose = basePrice * 0.82

        for (i in 0 until barCount) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) continue

            val seed = (symbol.hashCode() + i * 37)
            val noise = ((seed % 100) - 48) / 1000.0
            val trendDrift = 0.0006

            val open = curClose * (1.0 + ((seed % 20) - 10) / 2000.0)
            val close = open * (1.0 + noise + trendDrift)
            val high = maxOf(open, close) * (1.0 + ((Math.abs(seed) % 30) + 5) / 2000.0)
            val low = minOf(open, close) * (1.0 - ((Math.abs(seed) % 30) + 5) / 2000.0)
            val volume = 1200000L + (Math.abs(seed) % 4000000L)

            bars.add(
                CandleBar(
                    timestamp = cal.timeInMillis,
                    dateStr = sdf.format(cal.time),
                    open = ((open * 100).toInt()) / 100.0,
                    high = ((high * 100).toInt()) / 100.0,
                    low = ((low * 100).toInt()) / 100.0,
                    close = ((close * 100).toInt()) / 100.0,
                    volume = volume
                )
            )
            curClose = close
        }

        if (bars.isNotEmpty()) {
            val last = bars.last()
            bars[bars.size - 1] = last.copy(close = basePrice, high = maxOf(last.high, basePrice))
        }

        return bars
    }
}
