package com.example.personalvault.data.engine

import com.example.personalvault.data.models.DataFreshness
import com.example.personalvault.data.models.GlobalMarketItem
import com.example.personalvault.data.models.MarketIndex
import com.example.personalvault.data.models.PriceUpdate
import com.example.personalvault.data.models.StockQuote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe In-Memory Local Price Cache.
 * Acts as the single-point buffer between high-speed REST snapshots / WebSocket streaming
 * and the presentation layer (Dashboard, Stock Analyzer, Charts, Alerts, Portfolio).
 *
 * Implements granular instrument updates:
 * Sub-second price ticks update ONLY the affected instrument in O(1), preventing full UI
 * recreation and executing targeted alert evaluations.
 */
class LocalPriceCache {

    private val stockMap = ConcurrentHashMap<String, StockQuote>()
    private val indexMap = ConcurrentHashMap<String, MarketIndex>()
    private val globalMap = ConcurrentHashMap<String, GlobalMarketItem>()

    private val _stocks = MutableStateFlow<List<StockQuote>>(emptyList())
    val stocks: StateFlow<List<StockQuote>> = _stocks.asStateFlow()

    private val _indices = MutableStateFlow<List<MarketIndex>>(emptyList())
    val indices: StateFlow<List<MarketIndex>> = _indices.asStateFlow()

    private val _globalMarkets = MutableStateFlow<List<GlobalMarketItem>>(emptyList())
    val globalMarkets: StateFlow<List<GlobalMarketItem>> = _globalMarkets.asStateFlow()

    /**
     * Initializes or bulk-replaces the cache with a full REST snapshot.
     */
    fun populateFromRestSnapshot(
        stocksList: List<StockQuote>,
        indicesList: List<MarketIndex>,
        globalList: List<GlobalMarketItem>
    ) {
        stockMap.clear()
        for (s in stocksList) {
            stockMap[s.symbol.uppercase()] = s
        }
        _stocks.value = stocksList

        indexMap.clear()
        for (idx in indicesList) {
            indexMap[idx.name.uppercase()] = idx
        }
        _indices.value = indicesList

        globalMap.clear()
        for (item in globalList) {
            globalMap[item.symbol.uppercase()] = item
        }
        _globalMarkets.value = globalList
    }

    /**
     * Applies an incoming granular WebSocket or live stream tick.
     * Updates ONLY the matching instrument.
     * Evaluates alert rules for that instrument immediately.
     * Returns true if an instrument was found and updated.
     */
    fun applyPriceUpdate(update: PriceUpdate): Boolean {
        val upperSymbol = update.symbol.uppercase().trim()
        val nowMillis = if (update.timestampEpochMillis > 0) update.timestampEpochMillis else System.currentTimeMillis()

        // 1. Check Equities
        val existingStock = stockMap[upperSymbol]
        if (existingStock != null) {
            val updatedSparkline = if (existingStock.sparkline.isNotEmpty()) {
                val list = existingStock.sparkline.toMutableList()
                if (list.size > 15) list.removeAt(0)
                list.add(update.price)
                list
            } else {
                listOf(update.price)
            }

            val updatedStock = existingStock.copy(
                price = update.price,
                change = update.change,
                percentChange = update.percentChange,
                dayHigh = if (update.dayHigh > 0) maxOf(existingStock.dayHigh, update.dayHigh) else maxOf(existingStock.dayHigh, update.price),
                dayLow = if (update.dayLow > 0) minOf(existingStock.dayLow, update.dayLow) else minOf(existingStock.dayLow, update.price),
                volume = if (update.volume > 0) update.volume else existingStock.volume,
                source = if (update.source.isNotEmpty()) update.source else existingStock.source,
                exchange = if (update.exchange.isNotEmpty()) update.exchange else existingStock.exchange,
                freshness = update.freshness,
                updatedAt = if (update.timestamp.isNotEmpty()) update.timestamp else existingStock.updatedAt,
                sparkline = updatedSparkline,
                lastUpdateEpochMillis = nowMillis
            )

            stockMap[upperSymbol] = updatedStock
            _stocks.value = stockMap.values.toList()

            // Trigger targeted alert evaluation for this symbol in O(1)
            AlertEngine.evaluateSingleStock(updatedStock)
            return true
        }

        // 2. Check Benchmark Indices (e.g. NIFTY 50, BANK NIFTY, S&P 500)
        val existingIndex = indexMap[upperSymbol] ?: indexMap.values.firstOrNull { it.name.equals(upperSymbol, ignoreCase = true) }
        if (existingIndex != null) {
            val updatedIndex = existingIndex.copy(
                value = update.price,
                change = update.change,
                percentChange = update.percentChange,
                source = if (update.source.isNotEmpty()) update.source else existingIndex.source,
                exchange = if (update.exchange.isNotEmpty()) update.exchange else existingIndex.exchange,
                freshness = update.freshness,
                updatedAt = if (update.timestamp.isNotEmpty()) update.timestamp else existingIndex.updatedAt,
                lastUpdateEpochMillis = nowMillis
            )
            indexMap[existingIndex.name.uppercase()] = updatedIndex
            _indices.value = indexMap.values.toList()
            return true
        }

        // 3. Check Global Markets (Crypto, Forex, Commodities, Global Equities)
        val existingGlobal = globalMap[upperSymbol] ?: globalMap.values.firstOrNull { it.symbol.equals(upperSymbol, ignoreCase = true) }
        if (existingGlobal != null) {
            val updatedSparkline = if (existingGlobal.sparkline.isNotEmpty()) {
                val list = existingGlobal.sparkline.toMutableList()
                if (list.size > 15) list.removeAt(0)
                list.add(update.price)
                list
            } else {
                listOf(update.price)
            }

            val updatedGlobal = existingGlobal.copy(
                price = update.price,
                change = update.change,
                percentChange = update.percentChange,
                source = if (update.source.isNotEmpty()) update.source else existingGlobal.source,
                exchange = if (update.exchange.isNotEmpty()) update.exchange else existingGlobal.exchange,
                freshness = update.freshness,
                updatedAt = if (update.timestamp.isNotEmpty()) update.timestamp else existingGlobal.updatedAt,
                high24h = if (update.dayHigh > 0) maxOf(existingGlobal.high24h, update.dayHigh) else maxOf(existingGlobal.high24h, update.price),
                low24h = if (update.dayLow > 0) minOf(existingGlobal.low24h, update.dayLow) else minOf(existingGlobal.low24h, update.price),
                sparkline = updatedSparkline,
                lastUpdateEpochMillis = nowMillis
            )
            globalMap[existingGlobal.symbol.uppercase()] = updatedGlobal
            _globalMarkets.value = globalMap.values.toList()
            return true
        }

        return false
    }

    fun getStock(symbol: String): StockQuote? = stockMap[symbol.uppercase().trim()]
    fun getIndex(name: String): MarketIndex? = indexMap[name.uppercase().trim()]
    fun getGlobalItem(symbol: String): GlobalMarketItem? = globalMap[symbol.uppercase().trim()]

    fun getAllStocks(): List<StockQuote> = stockMap.values.toList()
    fun getAllIndices(): List<MarketIndex> = indexMap.values.toList()
    fun getAllGlobalMarkets(): List<GlobalMarketItem> = globalMap.values.toList()
}
