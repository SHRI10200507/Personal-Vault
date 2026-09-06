package com.example.personalvault.data.models

enum class DataFreshness(val label: String, val badgeColorHex: Long) {
    REAL_TIME("LIVE", 0xFF00E676),
    DELAYED_15M("15-MIN DELAYED", 0xFFFFB300),
    OFFICIAL_EOD("EOD CLOSE", 0xFF00D2FF),
    CACHED("CACHED SNAPSHOT", 0xFF8A99AD),
    ERROR("DATA ERROR", 0xFFFF5252)
}

enum class MarketRegion(val label: String, val icon: String) {
    INDIA("India", "🇮🇳"),
    USA("USA", "🇺🇸"),
    EUROPE("Europe", "🇪🇺"),
    ASIA("Asia", "🌏"),
    FOREX("Forex", "💵"),
    COMMODITIES("Commodities", "🛢️"),
    BONDS("Bonds", "📜"),
    CRYPTO("Crypto", "₿")
}

enum class MarketCategory {
    INDEX,
    EQUITY,
    FOREX,
    COMMODITY,
    CRYPTO,
    YIELD,
    BOND
}

data class PriceUpdate(
    val symbol: String,
    val price: Double,
    val change: Double,
    val percentChange: Double,
    val dayHigh: Double = 0.0,
    val dayLow: Double = 0.0,
    val volume: Long = 0L,
    val exchange: String = "NSE",
    val source: String = "NSE",
    val freshness: DataFreshness = DataFreshness.REAL_TIME,
    val timestamp: String = "",
    val timestampEpochMillis: Long = System.currentTimeMillis()
)

data class GlobalMarketItem(
    val symbol: String,
    val name: String,
    val region: MarketRegion,
    val category: MarketCategory,
    val price: Double,
    val change: Double,
    val percentChange: Double,
    val currency: String,
    val exchange: String,
    val freshness: DataFreshness,
    val updatedAt: String,
    val sparkline: List<Double> = emptyList(),
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume: String = "",
    val source: String = "",
    val lastUpdateEpochMillis: Long = System.currentTimeMillis()
)

data class CandleBar(
    val timestamp: Long,
    val dateStr: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    val isBullish: Boolean get() = close >= open
}

data class PortfolioTransaction(
    val id: String,
    val date: String,
    val symbol: String,
    val company: String,
    val type: String, // "BUY" or "SELL"
    val shares: Int,
    val price: Double,
    val brokerage: Double = 20.0,
    val taxes: Double = 0.0,
    val notes: String = ""
) {
    val netAmount: Double
        get() {
            val gross = shares * price
            return if (type == "BUY") gross + brokerage + taxes else gross - brokerage - taxes
        }
}

data class PortfolioHolding(
    val id: String,
    val symbol: String,
    val company: String,
    val shares: Int,
    val buyPrice: Double,
    val currentPrice: Double,
    val dayChangePercent: Double,
    val sector: String,
    val exchange: String = "NSE",
    val realizedPnl: Double = 0.0
) {
    val invested: Double get() = shares * buyPrice
    val currentValue: Double get() = shares * currentPrice
    val unrealizedPnl: Double get() = currentValue - invested
    val pnlPercent: Double get() = if (invested > 0) (unrealizedPnl / invested) * 100 else 0.0
    val dayPnl: Double get() = currentValue * (dayChangePercent / 100.0)
    val totalPnl: Double get() = unrealizedPnl + realizedPnl
}

data class PortfolioSummary(
    val investedCapital: Double,
    val currentValue: Double,
    val unrealizedPnl: Double,
    val realizedPnl: Double,
    val todayPnl: Double,
    val totalReturnPct: Double,
    val xirrPct: Double,
    val healthScore: Int
)

data class MarketAlert(
    val id: String,
    val symbol: String,
    val condition: String,
    val targetValue: Double,
    val triggered: Boolean,
    val timestamp: String,
    val message: String,
    val severity: String, // "success", "warning", "info", "critical"
    val enabled: Boolean = true,
    val lastTriggeredPrice: Double = 0.0,
    val cooldownMinutes: Int = 15,
    val lastTriggeredTimeMillis: Long = 0L
)

data class MarketIndex(
    val name: String,
    val value: Double,
    val change: Double,
    val percentChange: Double,
    val exchange: String = "NSE",
    val source: String = "NSE",
    val freshness: DataFreshness = DataFreshness.REAL_TIME,
    val updatedAt: String = "",
    val lastUpdateEpochMillis: Long = System.currentTimeMillis()
)

data class SectorInfo(
    val name: String,
    val percentChange: Double,
    val strengthScore: Int,
    val stockCount: Int,
    val topLeader: String
)

data class MarketRegimeItem(
    val region: String,
    val regime: String,
    val riskLevel: String,
    val riskScore: Int,
    val primaryDriver: String
)

data class GlobalCorrelationItem(
    val pair: String,
    val correlation: Double,
    val description: String,
    val bias: String
)

data class ExchangeInfo(
    val code: String,
    val name: String,
    val country: String,
    val timeZoneId: String,
    val openTime: String,
    val closeTime: String,
    val status: MarketStatus,
    val localTimeStr: String
)

data class DataConnectionStatus(
    val provider: String,
    val isConnected: Boolean,
    val latencyMs: Long,
    val statusMessage: String,
    val requestsUsed: Int,
    val requestsMax: Int,
    val lastSyncTime: String,
    val dataState: String = "LIVE", // "LIVE", "DELAYED", "CACHED", "ERROR"
    val lastSuccessfulQuoteTime: String = "",
    val nseBseStatus: String = "CONNECTED", // "CONNECTED", "DELAYED", "UNAVAILABLE"
    val globalEquityStatus: String = "CONNECTED",
    val forexStatus: String = "CONNECTED",
    val commodityStatus: String = "CONNECTED",
    val bondsStatus: String = "CONNECTED",
    val webSocketStatus: String = "STREAMING", // "STREAMING", "CONNECTING", "STANDBY", "OFFLINE"
    val streamTickCount: Long = 0L
)

data class BacktestTrade(
    val date: String,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val price: Double,
    val exitPrice: Double = 0.0,
    val pnlPercent: Double,
    val transactionCost: Double = 0.0,
    val reason: String = ""
)

data class WalkForwardMetrics(
    val inSampleReturnPercent: Double,
    val outOfSampleReturnPercent: Double,
    val inSampleWinRate: Double,
    val outOfSampleWinRate: Double,
    val overfittingRisk: String, // "LOW", "MODERATE", "ELEVATED"
    val consistencyRatio: Double,
    val inSampleRange: String,
    val outOfSampleRange: String
)

data class BacktestResult(
    val strategyName: String,
    val symbol: String = "NIFTY",
    val period: String = "1Y",
    val initialCapital: Double,
    val finalCapital: Double,
    val totalReturnPercent: Double,
    val cagrPercent: Double,
    val winRatePercent: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val maxDrawdownPercent: Double,
    val profitFactor: Double,
    val sharpeRatio: Double,
    val sortinoRatio: Double,
    val totalCostsPaid: Double,
    val trades: List<BacktestTrade>,
    val equityCurve: List<Pair<String, Double>>,
    val walkForward: WalkForwardMetrics? = null
)

enum class MarketStatus(val label: String, val isOpen: Boolean, val note: String) {
    OPEN("OPEN", true, "Regular Trading Session"),
    PRE_OPEN("PRE-OPEN", false, "Pre-open Discovery"),
    AFTER_HOURS("AFTER-HOURS", false, "Post-market Session"),
    CLOSED("CLOSED", false, "Session Closed")
}
