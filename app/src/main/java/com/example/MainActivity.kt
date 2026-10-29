package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.CardRepository
import com.example.data.model.AppStrings
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.example.nfc.NfcManager
import com.example.nfc.NfcScanResult
import com.example.ui.components.HoldNearReaderModal
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.CardConstructorSheet
import com.example.ui.screens.CardDetailDialog
import com.example.ui.screens.CatalogSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.TechGuideScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import kotlinx.coroutines.launch

enum class MainNavTab {
    WALLET,
    SCANNER,
    TERMINAL,
    GUIDE,
    AI
}

class MainActivity : ComponentActivity() {
    private lateinit var nfcManager: NfcManager
    private val scannedTagState = mutableStateOf<NfcScanResult?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Configure App Check before UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        nfcManager = NfcManager(this)

        // Handle initial intent if launched via NFC tap
        intent?.let {
            val scan = nfcManager.parseIntent(it)
            if (scan != null) {
                scannedTagState.value = scan
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    WalletAllApp(
                        nfcScanResult = scannedTagState.value
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (nfcManager.isNfcEnabled) {
            nfcManager.enableReaderMode(this) { result ->
                scannedTagState.value = result
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcManager.isNfcEnabled) {
            nfcManager.disableReaderMode(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val scan = nfcManager.parseIntent(intent)
        if (scan != null) {
            scannedTagState.value = scan
        }
    }
}

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
            }
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        }
    } catch (e: Exception) {
        Log.e("AppCheckInit", "Failed to initialize Firebase App Check", e)
    }
}

@Composable
fun WalletAllApp(
    nfcScanResult: NfcScanResult?
) {
    val context = LocalContext.current
    val repository = remember { CardRepository(context) }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(MainNavTab.WALLET) }
    var currentLanguage by remember { mutableStateOf(Language.RU) }

    // Collect cards from Room Database
    val cards by repository.allCards.collectAsStateWithLifecycle(initialValue = emptyList())

    // Ensure starter cards on first install
    LaunchedEffect(Unit) {
        repository.ensureInitialData()
    }

    // Modal Sheet states
    var selectedCardForDetail by remember { mutableStateOf<WalletCard?>(null) }
    var selectedCardForReader by remember { mutableStateOf<WalletCard?>(null) }
    var isCatalogOpen by remember { mutableStateOf(false) }
    var isConstructorOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = Color.Black,
        bottomBar = {
            WalletAllBottomNavigation(
                activeTab = activeTab,
                currentLanguage = currentLanguage,
                onTabSelected = { activeTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                MainNavTab.WALLET -> HomeScreen(
                    cards = cards,
                    currentLanguage = currentLanguage,
                    onLanguageChange = { currentLanguage = it },
                    onCardClick = { card -> selectedCardForDetail = card },
                    onOpenCatalog = { isCatalogOpen = true },
                    onOpenConstructor = { isConstructorOpen = true },
                    onOpenScanner = { activeTab = MainNavTab.SCANNER },
                    onToggleFavorite = { cardId, isFav ->
                        scope.launch { repository.toggleFavorite(cardId, isFav) }
                    }
                )
                MainNavTab.SCANNER -> ScannerScreen(
                    currentLanguage = currentLanguage,
                    lastScannedNfcResult = nfcScanResult,
                    onAddScannedCard = { card ->
                        scope.launch {
                            repository.addCard(card)
                            activeTab = MainNavTab.WALLET
                        }
                    }
                )
                MainNavTab.TERMINAL -> TerminalScreen(
                    cards = cards,
                    currentLanguage = currentLanguage,
                    onUpdateCardBalance = { cardId, newBal ->
                        scope.launch { repository.updateBalance(cardId, newBal) }
                    }
                )
                MainNavTab.GUIDE -> TechGuideScreen(
                    currentLanguage = currentLanguage
                )
                MainNavTab.AI -> AiChatScreen(
                    currentLanguage = currentLanguage,
                    onAddCard = { card ->
                        scope.launch {
                            repository.addCard(card)
                        }
                    },
                    onRestoreTemplates = {
                        repository.restoreAllTemplates()
                    },
                    onRepairBalances = {
                        repository.repairAllBalances()
                    }
                )
            }

            // === MODALS ===
            if (selectedCardForDetail != null) {
                CardDetailDialog(
                    card = selectedCardForDetail!!,
                    currentLanguage = currentLanguage,
                    onDismiss = { selectedCardForDetail = null },
                    onDelete = { cardId ->
                        scope.launch { repository.deleteCard(cardId) }
                    },
                    onToggleFavorite = { cardId, isFav ->
                        scope.launch { repository.toggleFavorite(cardId, isFav) }
                        selectedCardForDetail = selectedCardForDetail?.copy(isFavorite = isFav)
                    },
                    onUpdateBalance = { cardId, newBal ->
                        scope.launch { repository.updateBalance(cardId, newBal) }
                        selectedCardForDetail = selectedCardForDetail?.copy(balance = newBal)
                    },
                    onTapNearReader = { card ->
                        selectedCardForReader = card
                    }
                )
            }

            if (selectedCardForReader != null) {
                HoldNearReaderModal(
                    card = selectedCardForReader!!,
                    currentLanguage = currentLanguage,
                    onDismiss = { selectedCardForReader = null },
                    onSuccess = {
                        // After reader tap
                    }
                )
            }

            if (isCatalogOpen) {
                CatalogSheet(
                    existingCardTitles = cards.map { it.title }.toSet(),
                    currentLanguage = currentLanguage,
                    onDismiss = { isCatalogOpen = false },
                    onAddCard = { card ->
                        scope.launch { repository.addCard(card) }
                    }
                )
            }

            if (isConstructorOpen) {
                CardConstructorSheet(
                    currentLanguage = currentLanguage,
                    onDismiss = { isConstructorOpen = false },
                    onSaveCard = { card ->
                        scope.launch { repository.addCard(card) }
                    }
                )
            }
        }
    }
}

@Composable
private fun WalletAllBottomNavigation(
    activeTab: MainNavTab,
    currentLanguage: Language,
    onTabSelected: (MainNavTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(Color.Black.copy(alpha = 0.96f))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = AppStrings.get("tab_wallet", currentLanguage),
                icon = Icons.Default.AccountBalanceWallet,
                isSelected = activeTab == MainNavTab.WALLET,
                onClick = { onTabSelected(MainNavTab.WALLET) },
                tag = "tab_wallet"
            )
            NavItem(
                label = AppStrings.get("tab_scanner", currentLanguage),
                icon = Icons.Default.QrCodeScanner,
                isSelected = activeTab == MainNavTab.SCANNER,
                onClick = { onTabSelected(MainNavTab.SCANNER) },
                tag = "tab_scanner"
            )
            NavItem(
                label = AppStrings.get("tab_terminal", currentLanguage),
                icon = Icons.Default.Nfc,
                isSelected = activeTab == MainNavTab.TERMINAL,
                onClick = { onTabSelected(MainNavTab.TERMINAL) },
                tag = "tab_terminal"
            )
            NavItem(
                label = AppStrings.get("tab_guide", currentLanguage),
                icon = Icons.Default.MenuBook,
                isSelected = activeTab == MainNavTab.GUIDE,
                onClick = { onTabSelected(MainNavTab.GUIDE) },
                tag = "tab_guide"
            )
            NavItem(
                label = AppStrings.get("tab_ai", currentLanguage),
                icon = Icons.Default.AutoAwesome,
                isSelected = activeTab == MainNavTab.AI,
                onClick = { onTabSelected(MainNavTab.AI) },
                tag = "tab_ai"
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}
