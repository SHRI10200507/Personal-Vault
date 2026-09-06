package com.example.personalvault

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.theme.AccentCyan
import com.example.personalvault.theme.MarketTerminalTheme
import com.example.personalvault.theme.TerminalBackground
import com.example.personalvault.theme.TerminalCardBorder
import com.example.personalvault.theme.TerminalSurface
import com.example.personalvault.theme.TextMuted
import com.example.personalvault.theme.TextPrimary
import com.example.personalvault.ui.components.TerminalTopBar
import com.example.personalvault.ui.screens.AlertsScreen
import com.example.personalvault.ui.screens.BacktestScreen
import com.example.personalvault.ui.screens.DashboardScreen
import com.example.personalvault.ui.screens.DataCenterScreen
import com.example.personalvault.ui.screens.ExportSheetDialog
import com.example.personalvault.ui.screens.LiveMarketScreen
import com.example.personalvault.ui.screens.MarketRadarScreen
import com.example.personalvault.ui.screens.MoreScreen
import com.example.personalvault.ui.screens.PortfolioScreen
import com.example.personalvault.ui.screens.StockAnalyzerScreen
import com.example.personalvault.ui.viewmodel.MarketViewModel

enum class TerminalTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Dashboard),
    MARKETS("Markets", Icons.Default.ShowChart),
    RADAR("Radar", Icons.Default.Radar),
    PORTFOLIO("Portfolio", Icons.Default.PieChart),
    MORE("More", Icons.Default.MoreHoriz)
}

enum class SubScreen {
    NONE,
    ANALYZER,
    STRATEGY,
    ALERTS,
    DATA_CENTER
}

class MainActivity : ComponentActivity() {

    private val viewModel: MarketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MarketTerminalTheme {
                MainTerminalApp(viewModel)
            }
        }
    }
}

@Composable
fun MainTerminalApp(viewModel: MarketViewModel) {
    var currentTab by remember { mutableStateOf(TerminalTab.HOME) }
    var activeSubScreen by remember { mutableStateOf(SubScreen.NONE) }

    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val dataStatus by viewModel.dataCenterStatus.collectAsState()
    val marketStatus = remember { viewModel.getMarketStatus() }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Handle system back navigation
    BackHandler(enabled = activeSubScreen != SubScreen.NONE) {
        activeSubScreen = SubScreen.NONE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Handled */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarPadding)
            ) {
                if (activeSubScreen != SubScreen.NONE) {
                    // Sub-screen navigation top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurface)
                            .border(width = 1.dp, color = TerminalCardBorder)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeSubScreen = SubScreen.NONE },
                            modifier = Modifier.testTag("back_to_terminal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to terminal",
                                tint = AccentCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (activeSubScreen) {
                                SubScreen.ANALYZER -> "STOCK ANALYZER & CANDLESTICKS"
                                SubScreen.STRATEGY -> "QUANTITATIVE STRATEGY LAB"
                                SubScreen.ALERTS -> "MARKET ALERT ENGINE"
                                SubScreen.DATA_CENTER -> "DATA CENTER & TELEMETRY"
                                else -> ""
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                } else {
                    TerminalTopBar(
                        marketStatus = marketStatus,
                        lastUpdated = lastUpdated,
                        isRefreshing = isRefreshing,
                        freshnessLabel = if (dataStatus.webSocketStatus == "STREAMING") "LIVE STREAM" else dataStatus.dataState,
                        onManualRefresh = { viewModel.triggerManualRefresh() },
                        onExportClick = { viewModel.setShowExportDialog(true) }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = TerminalSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = TerminalCardBorder)
                    .padding(bottom = navBarPadding)
            ) {
                TerminalTab.values().forEach { tab ->
                    val isSelected = (currentTab == tab) && (activeSubScreen == SubScreen.NONE)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = tab
                            activeSubScreen = SubScreen.NONE
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentCyan,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = AccentCyan,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0x2A00D2FF)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = TerminalBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeSubScreen != SubScreen.NONE) {
                when (activeSubScreen) {
                    SubScreen.ANALYZER -> StockAnalyzerScreen(viewModel = viewModel)
                    SubScreen.STRATEGY -> BacktestScreen(viewModel = viewModel)
                    SubScreen.ALERTS -> AlertsScreen(viewModel = viewModel)
                    SubScreen.DATA_CENTER -> DataCenterScreen(viewModel = viewModel)
                    else -> Unit
                }
            } else {
                when (currentTab) {
                    TerminalTab.HOME -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAnalyzer = { stock ->
                            viewModel.selectStock(stock)
                            activeSubScreen = SubScreen.ANALYZER
                        },
                        onNavigateToRadar = { currentTab = TerminalTab.RADAR },
                        onNavigateToPortfolio = { currentTab = TerminalTab.PORTFOLIO }
                    )
                    TerminalTab.MARKETS -> LiveMarketScreen(
                        viewModel = viewModel,
                        onStockClick = { stock ->
                            viewModel.selectStock(stock)
                            activeSubScreen = SubScreen.ANALYZER
                        }
                    )
                    TerminalTab.RADAR -> MarketRadarScreen(
                        viewModel = viewModel,
                        onStockClick = { stock ->
                            viewModel.selectStock(stock)
                            activeSubScreen = SubScreen.ANALYZER
                        }
                    )
                    TerminalTab.PORTFOLIO -> PortfolioScreen(
                        viewModel = viewModel,
                        onStockClick = { symbol ->
                            viewModel.selectStockBySymbol(symbol)
                            activeSubScreen = SubScreen.ANALYZER
                        }
                    )
                    TerminalTab.MORE -> MoreScreen(
                        viewModel = viewModel,
                        onNavigateToAnalyzer = { activeSubScreen = SubScreen.ANALYZER },
                        onNavigateToStrategy = { activeSubScreen = SubScreen.STRATEGY },
                        onNavigateToAlerts = { activeSubScreen = SubScreen.ALERTS },
                        onNavigateToDataCenter = { activeSubScreen = SubScreen.DATA_CENTER },
                        onExportExcel = { viewModel.setShowExportDialog(true) }
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        ExportSheetDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowExportDialog(false) }
        )
    }
}
