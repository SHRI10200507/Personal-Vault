package com.example.personalvault.data.models

enum class TrendBias {
    BULLISH,
    BEARISH,
    NEUTRAL
}

enum class RadarCategory(val label: String, val icon: String) {
    BREAKOUT("Breakouts", "🚀"),
    STRONG_MOMENTUM("Momentum", "📈"),
    VOLUME_SPIKE("Volume Spike", "🔥"),
    OVERSOLD("Oversold", "💎"),
    OVERBOUGHT("Overbought", "⚠️"),
    TRENDING("Trend", "📊"),
    HIGH_52W("52W High", "🏆"),
    LOW_52W("52W Low", "🔻")
}

data class PatternSignal(
    val title: String,
    val description: String,
    val isBullish: Boolean,
    val icon: String
)

data class StockQuote(
    val symbol: String,
    val company: String,
    val price: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val prevClose: Double,
    val change: Double,
    val percentChange: Double,
    val pe: Double,
    val pb: Double,
    val week52High: Double,
    val week52Low: Double,
    val sector: String,
    val status: String = "Verified",
    val volume: Long,
    val avgVolume: Long,
    val rsi: Double,
    val macd: Double,
    val macdSignal: Double,
    val sma20: Double,
    val sma50: Double,
    val sma100: Double,
    val sma200: Double,
    val ema9: Double,
    val ema21: Double,
    val ema50: Double,
    val roe: Double,
    val roce: Double,
    val revenueGrowth: Double,
    val profitGrowth: Double,
    val debtToEquity: Double,
    val earningsConsistency: Double,
    val updatedAt: String,
    val trend1D: TrendBias,
    val trend1W: TrendBias,
    val trend1M: TrendBias,
    val trend3M: TrendBias,
    val trend6M: TrendBias,
    val trend1Y: TrendBias,
    val sparkline: List<Double> = emptyList(),
    val exchange: String = "NSE",
    val source: String = "NSE",
    val freshness: DataFreshness = DataFreshness.REAL_TIME,
    val candles: List<CandleBar> = emptyList(),
    val lastUpdateEpochMillis: Long = System.currentTimeMillis()
) {
    val position52W: Int
        get() {
            val range = week52High - week52Low
            return if (range > 0) (((price - week52Low) / range) * 100).toInt().coerceIn(0, 100) else 50
        }

    val distanceTo52WHighPct: Double
        get() = if (week52High > 0) ((week52High - price) / week52High) * 100 else 0.0

    val volumeRatio: Double
        get() = if (avgVolume > 0) volume.toDouble() / avgVolume else 1.0

    val technicalScore: Int
        get() {
            var score = 50
            if (trend1D == TrendBias.BULLISH) score += 5
            if (trend1W == TrendBias.BULLISH) score += 6
            if (trend1M == TrendBias.BULLISH) score += 5
            if (trend1D == TrendBias.BEARISH) score -= 5
            if (trend1W == TrendBias.BEARISH) score -= 6

            if (price > sma20) score += 5 else score -= 4
            if (price > sma50) score += 5 else score -= 5
            if (price > sma200) score += 8 else score -= 8
            if (ema9 > ema21) score += 4

            if (rsi in 50.0..70.0) score += 6
            else if (rsi > 75.0) score -= 2
            else if (rsi < 30.0) score += 3

            if (macd > macdSignal) score += 6 else score -= 5

            if (volume > avgVolume * 1.3) score += 6

            if (position52W > 80) score += 6
            if (position52W < 25) score -= 6

            return score.coerceIn(12, 98)
        }

    val fundamentalScore: Int
        get() {
            var score = 45
            if (pe in 10.0..28.0) score += 12
            else if (pe > 55.0) score -= 8
            else if (pe in 28.0..40.0) score += 4

            if (pb in 1.0..4.5) score += 9
            else if (pb > 12.0) score -= 6

            if (roe > 18.0) score += 12
            else if (roe > 12.0) score += 7
            else score -= 4

            if (roce > 20.0) score += 9
            else if (roce > 14.0) score += 5

            if (revenueGrowth > 15.0) score += 8
            if (profitGrowth > 15.0) score += 10
            else if (profitGrowth < 0.0) score -= 8

            if (debtToEquity < 0.5) score += 8
            else if (debtToEquity > 1.8) score -= 7

            return score.coerceIn(15, 96)
        }

    val momentumScore: Int
        get() {
            var m = if (percentChange > 0) 55 else 40
            if (rsi in 55.0..70.0) m += 20
            if (volume > avgVolume * 1.2) m += 15
            if (price > ema9) m += 10
            return m.coerceIn(15, 95)
        }

    val overallScore: Int
        get() {
            val weighted = (fundamentalScore * 0.45) + (technicalScore * 0.35) + (momentumScore * 0.20)
            return weighted.toInt().coerceIn(15, 98)
        }

    val scoreLabel: String
        get() = when {
            overallScore >= 80 -> "Bullish Bias"
            overallScore >= 65 -> "Moderate Bullish"
            overallScore >= 50 -> "Neutral Stance"
            overallScore >= 38 -> "Cautious / Mixed"
            else -> "Bearish Bias"
        }

    val confidencePct: Int
        get() = (overallScore * 0.7 + (if (volume > avgVolume) 20 else 10)).toInt().coerceIn(55, 92)

    val riskLevel: String
        get() = when {
            debtToEquity > 1.5 || rsi > 76.0 || position52W < 20 -> "Elevated Risk"
            pe > 45.0 || rsi > 68.0 -> "Moderate Risk"
            else -> "Low / Managed Risk"
        }

    val signalDrivers: List<String>
        get() {
            val drivers = mutableListOf<String>()
            if (price > sma200) drivers.add("Price established above 200 SMA (₹${sma200.toInt()})")
            if (macd > macdSignal) drivers.add("MACD positive histogram expansion")
            if (volume > avgVolume * 1.25) drivers.add("Volume surge ${String.format("%.1f", volume.toDouble() / avgVolume)}× over 20-day mean")
            if (roe >= 15.0) drivers.add("High Return on Equity (${roe}%)")
            if (revenueGrowth >= 15.0) drivers.add("Double-digit revenue growth (${revenueGrowth}%)")
            if (debtToEquity < 0.8) drivers.add("Prudent leverage (D/E: ${debtToEquity})")
            if (drivers.isEmpty()) drivers.add("Stable consolidated trading range")
            return drivers
        }

    val riskFactors: List<String>
        get() {
            val risks = mutableListOf<String>()
            if (rsi > 70.0) risks.add("RSI at ${rsi.toInt()} approaching overbought territory")
            if (pe > 40.0) risks.add("Valuation premium (PE: ${pe} vs sector)")
            if (debtToEquity > 1.2) risks.add("Elevated debt leverage (${debtToEquity})")
            if (price < sma50) risks.add("Trading below intermediate 50 SMA")
            if (risks.isEmpty()) risks.add("Macro market volatility & broad sector pullbacks")
            return risks
        }

    val radarCategory: RadarCategory
        get() = when {
            volumeRatio >= 1.6 -> RadarCategory.VOLUME_SPIKE
            distanceTo52WHighPct <= 2.5 -> RadarCategory.HIGH_52W
            position52W <= 12 -> RadarCategory.LOW_52W
            rsi >= 72.0 -> RadarCategory.OVERBOUGHT
            rsi <= 32.0 -> RadarCategory.OVERSOLD
            price > sma50 && price > sma200 && position52W >= 80 -> RadarCategory.BREAKOUT
            technicalScore >= 72 && percentChange > 0.4 -> RadarCategory.STRONG_MOMENTUM
            else -> RadarCategory.TRENDING
        }

    val patterns: List<PatternSignal>
        get() {
            val list = mutableListOf<PatternSignal>()
            if (price >= week52High * 0.98) {
                list.add(PatternSignal("52W High Breakout", "Trading within 2% of 52-week peak", true, "🚀"))
            }
            if (sma50 > sma200 && price > sma50) {
                list.add(PatternSignal("Golden Cross", "50 SMA trending above 200 SMA bullish continuation", true, "✨"))
            } else if (sma50 < sma200 && price < sma50) {
                list.add(PatternSignal("Death Cross", "50 SMA below 200 SMA bearish pressure", false, "💀"))
            }
            if (volume > avgVolume * 1.5) {
                list.add(PatternSignal("Volume Spike", "${String.format("%.1f", volume.toDouble() / avgVolume)}× average 20-day trading volume", true, "🔥"))
            }
            if (rsi > 70) {
                list.add(PatternSignal("RSI Overbought", "RSI at ${rsi.toInt()} indicates high momentum, potential consolidation", false, "🟠"))
            } else if (rsi < 32) {
                list.add(PatternSignal("RSI Oversold", "RSI at ${rsi.toInt()} offers attractive mean-reversion risk/reward", true, "🟢"))
            }
            if (macd > macdSignal && macd > 0) {
                list.add(PatternSignal("MACD Expansion", "MACD line expanding above signal line in positive zone", true, "📈"))
            }
            return list
        }

    val quantitativeAnalysis: String
        get() {
            val sb = StringBuilder()
            sb.append("QUANTITATIVE SUMMARY\n")
            sb.append("Bias: $scoreLabel (Quant Score: $overallScore/100, Confidence: $confidencePct%)\n")
            sb.append("Risk Profile: $riskLevel\n\n")
            sb.append("KEY CATALYSTS:\n")
            signalDrivers.forEach { sb.append("• $it\n") }
            sb.append("\nRISK FACTORS:\n")
            riskFactors.forEach { sb.append("• $it\n") }
            sb.append("\nVALUATION BENCHMARK:\n")
            sb.append("Trading at PE of $pe and PB of $pb with ROE of ${roe}% against sector $sector.")
            return sb.toString()
        }
}
