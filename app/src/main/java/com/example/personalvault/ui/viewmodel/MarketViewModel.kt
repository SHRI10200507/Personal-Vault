package com.example.personalvault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalvault.data.local.LocalStoreManager
import com.example.personalvault.data.models.BacktestResult
import com.example.personalvault.data.models.DataConnectionStatus
import com.example.personalvault.data.models.ExchangeInfo
import com.example.personalvault.data.models.GlobalCorrelationItem
import com.example.personalvault.data.models.GlobalMarketItem
import com.example.personalvault.data.models.MarketAlert
import com.example.personalvault.data.models.MarketIndex
import com.example.personalvault.data.models.MarketRegimeItem
import com.example.personalvault.data.models.MarketRegion
import com.example.personalvault.data.models.MarketStatus
import com.example.personalvault.data.models.PortfolioHolding
import com.example.personalvault.data.models.PortfolioSummary
import com.example.personalvault.data.models.PortfolioTransaction
import com.example.personalvault.data.models.RadarCategory
import com.example.personalvault.data.models.SectorInfo
import com.example.personalvault.data.models.StockQuote
import com.example.personalvault.data.repository.MarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    DEFAULT("Default"),
    TOP_GAINERS("Top Gainers"),
    TOP_LOSERS("Top Losers"),
    OVERALL_SCORE("Highest Score"),
    TECH_SCORE("Technical Score"),
    FUND_SCORE("Fundamental Score"),
    PRICE_HIGH("Price (High to Low)"),
    PRICE_LOW("Price (Low to High)")
}

class MarketViewModel : ViewModel() {

    val stocks: StateFlow<List<StockQuote>> = MarketRepository.stocks
    val indiaIndices: StateFlow<List<MarketIndex>> = MarketRepository.indiaIndices
    val globalMarkets: StateFlow<List<GlobalMarketItem>> = MarketRepository.globalMarkets
    val selectedRegion: StateFlow<MarketRegion> = MarketRepository.selectedRegion
    val marketRegimes: StateFlow<List<MarketRegimeItem>> = MarketRepository.marketRegimes
    val globalCorrelations: StateFlow<List<GlobalCorrelationItem>> = MarketRepository.globalCorrelations
    val exchangeSchedule: StateFlow<List<ExchangeInfo>> = MarketRepository.exchangeSchedule

    val transactions: StateFlow<List<PortfolioTransaction>> = MarketRepository.transactions
    val portfolioHoldings: StateFlow<List<PortfolioHolding>> = MarketRepository.portfolioHoldings
    val portfolioSummary: StateFlow<PortfolioSummary> = MarketRepository.portfolioSummary
    val alerts: StateFlow<List<MarketAlert>> = MarketRepository.alerts
    val watchlistSymbols: StateFlow<Set<String>> = MarketRepository.watchlistSymbols
    val dataCenterStatus: StateFlow<DataConnectionStatus> = MarketRepository.dataCenterStatus
    val apiSettings: StateFlow<LocalStoreManager.ApiSettings> = MarketRepository.apiSettings
    val isRefreshing: StateFlow<Boolean> = MarketRepository.isRefreshing
    val lastUpdated: StateFlow<String> = MarketRepository.lastUpdatedTime

    private val _selectedStock = MutableStateFlow<StockQuote?>(null)
    val selectedStock: StateFlow<StockQuote?> = _selectedStock.asStateFlow()

    private val _selectedRadarCategory = MutableStateFlow<RadarCategory?>(null)
    val selectedRadarCategory: StateFlow<RadarCategory?> = _selectedRadarCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sectorFilter = MutableStateFlow<String?>(null)
    val sectorFilter: StateFlow<String?> = _sectorFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DEFAULT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showAddTransactionDialog = MutableStateFlow(false)
    val showAddTransactionDialog: StateFlow<Boolean> = _showAddTransactionDialog.asStateFlow()

    private val _showAddAlertDialog = MutableStateFlow(false)
    val showAddAlertDialog: StateFlow<Boolean> = _showAddAlertDialog.asStateFlow()

    private val _backtestResult = MutableStateFlow<BacktestResult?>(null)
    val backtestResult: StateFlow<BacktestResult?> = _backtestResult.asStateFlow()

    private val _isBacktesting = MutableStateFlow(false)
    val isBacktesting: StateFlow<Boolean> = _isBacktesting.asStateFlow()

    val filteredStocks: StateFlow<List<StockQuote>> = combine(
        stocks,
        _searchQuery,
        _sectorFilter,
        _sortOption
    ) { stockList, query, sector, sort ->
        var list = stockList

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.symbol.lowercase().contains(q) ||
                it.company.lowercase().contains(q) ||
                it.sector.lowercase().contains(q)
            }
        }

        if (sector != null && sector != "All") {
            list = list.filter { it.sector.equals(sector, ignoreCase = true) }
        }

        when (sort) {
            SortOption.DEFAULT -> list
            SortOption.TOP_GAINERS -> list.sortedByDescending { it.percentChange }
            SortOption.TOP_LOSERS -> list.sortedBy { it.percentChange }
            SortOption.OVERALL_SCORE -> list.sortedByDescending { it.overallScore }
            SortOption.TECH_SCORE -> list.sortedByDescending { it.technicalScore }
            SortOption.FUND_SCORE -> list.sortedByDescending { it.fundamentalScore }
            SortOption.PRICE_HIGH -> list.sortedByDescending { it.price }
            SortOption.PRICE_LOW -> list.sortedBy { it.price }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            stocks.collect { list ->
                if (_selectedStock.value == null && list.isNotEmpty()) {
                    _selectedStock.value = list.first()
                } else if (_selectedStock.value != null) {
                    val updated = list.find { it.symbol == _selectedStock.value?.symbol }
                    if (updated != null) {
                        _selectedStock.value = updated
                    }
                }
            }
        }

        // Initialize default backtest
        viewModelScope.launch {
            if (_backtestResult.value == null && stocks.value.isNotEmpty()) {
                val defaultSymbol = stocks.value.first().symbol
                _backtestResult.value = MarketRepository.runBacktest("EMA 20/50 + RSI", 100000.0, defaultSymbol, "1Y")
            }
        }
    }

    fun getMarketStatus(): MarketStatus = MarketRepository.getIndianMarketStatus()

    fun getSectors(): List<SectorInfo> = MarketRepository.getSectors()

    fun selectStock(stock: StockQuote) {
        _selectedStock.value = stock
    }

    fun selectStockBySymbol(symbol: String) {
        val match = stocks.value.find { it.symbol.equals(symbol, ignoreCase = true) }
        if (match != null) {
            _selectedStock.value = match
        }
    }

    fun selectRegion(region: MarketRegion) {
        MarketRepository.setSelectedRegion(region)
    }

    fun selectRadarCategory(cat: RadarCategory?) {
        _selectedRadarCategory.value = cat
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSectorFilter(sector: String?) {
        _sectorFilter.value = sector
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleWatchlist(symbol: String) {
        MarketRepository.toggleWatchlist(symbol)
    }

    fun isWatchlisted(symbol: String): Boolean {
        return watchlistSymbols.value.contains(symbol)
    }

    fun triggerManualRefresh() {
        MarketRepository.refreshMarketData()
    }

    fun addTransaction(
        symbol: String,
        company: String,
        type: String,
        shares: Int,
        price: Double,
        brokerage: Double = 20.0,
        taxes: Double = 15.0,
        notes: String = ""
    ) {
        MarketRepository.addTransaction(symbol, company, type, shares, price, brokerage, taxes, notes)
        _showAddTransactionDialog.value = false
    }

    fun removeTransaction(id: String) {
        MarketRepository.removeTransaction(id)
    }

    fun addAlert(symbol: String, condition: String, target: Double, severity: String = "info") {
        MarketRepository.addAlert(symbol, condition, target, severity)
        _showAddAlertDialog.value = false
    }

    fun removeAlert(id: String) {
        MarketRepository.removeAlert(id)
    }

    fun saveApiSettings(settings: LocalStoreManager.ApiSettings) {
        MarketRepository.saveApiSettings(settings)
    }

    fun runBacktest(strategy: String, capital: Double, symbol: String, period: String = "1Y") {
        viewModelScope.launch {
            _isBacktesting.value = true
            _backtestResult.value = MarketRepository.runBacktest(strategy, capital, symbol, period)
            _isBacktesting.value = false
        }
    }

    fun setShowExportDialog(show: Boolean) {
        _showExportDialog.value = show
    }

    fun setShowAddTransactionDialog(show: Boolean) {
        _showAddTransactionDialog.value = show
    }

    fun setShowAddAlertDialog(show: Boolean) {
        _showAddAlertDialog.value = show
    }

    fun getExcelExportContent(): String {
        return MarketRepository.generateExcelTerminalExport()
    }
}
