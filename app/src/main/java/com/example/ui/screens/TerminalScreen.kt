package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.data.model.CardCategory
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.example.nfc.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TerminalMode {
    INTERCOM,
    METRO,
    POS
}

@Composable
fun TerminalScreen(
    cards: List<WalletCard>,
    currentLanguage: Language,
    onUpdateCardBalance: (String, Double) -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val scope = rememberCoroutineScope()

    var activeMode by remember { mutableStateOf(TerminalMode.INTERCOM) }
    var selectedCardId by remember {
        mutableStateOf(cards.firstOrNull { it.category == CardCategory.INTERCOM }?.id ?: cards.firstOrNull()?.id)
    }

    var isAccessGranted by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Ожидание касания...") }
    var fareDeducted by remember { mutableStateOf<String?>(null) }
    var showReceipt by remember { mutableStateOf(false) }

    val selectedCard = cards.find { it.id == selectedCardId } ?: cards.firstOrNull()

    fun triggerTerminalTap() {
        if (selectedCard == null || isAccessGranted) return
        isAccessGranted = true

        when (activeMode) {
            TerminalMode.INTERCOM -> {
                soundManager.playIntercomUnlockSound()
                statusMessage = "ДОСТУП РАЗРЕШЕН • ЗАМОК ОТКРЫТ"
                scope.launch {
                    delay(2500)
                    isAccessGranted = false
                    statusMessage = "Ожидание касания..."
                }
            }
            TerminalMode.METRO -> {
                soundManager.playMetroDing()
                val currentBal = selectedCard.balance ?: 500.0
                val fare = if (selectedCard.currency == "₸") 100.0 else if (selectedCard.currency == "₽") 57.0 else 2.5
                val newBal = maxOf(0.0, currentBal - fare)
                onUpdateCardBalance(selectedCard.id, newBal)
                fareDeducted = "-${fare.toInt()} ${selectedCard.currency}"
                statusMessage = "ПРОХОД ОТКРЫТ • ОСТАТОК: ${newBal.toInt()} ${selectedCard.currency}"
                scope.launch {
                    delay(2500)
                    isAccessGranted = false
                    fareDeducted = null
                    statusMessage = "Ожидание касания..."
                }
            }
            TerminalMode.POS -> {
                soundManager.playPosBeep()
                statusMessage = "ОПЛАТА ОДОБРЕНА"
                showReceipt = true
                scope.launch {
                    delay(3500)
                    isAccessGranted = false
                    showReceipt = false
                    statusMessage = "Ожидание касания..."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = AppStrings.get("terminal_title", currentLanguage),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Text(
            text = "Симулятор считывателей доступа, турникетов и касс",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Mode Switcher Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF16161A))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TerminalModeTab(
                label = AppStrings.get("terminal_intercom", currentLanguage),
                icon = Icons.Default.Key,
                isSelected = activeMode == TerminalMode.INTERCOM,
                onClick = { activeMode = TerminalMode.INTERCOM },
                modifier = Modifier.weight(1f)
            )
            TerminalModeTab(
                label = AppStrings.get("terminal_metro", currentLanguage),
                icon = Icons.Default.Subway,
                isSelected = activeMode == TerminalMode.METRO,
                onClick = { activeMode = TerminalMode.METRO },
                modifier = Modifier.weight(1f)
            )
            TerminalModeTab(
                label = AppStrings.get("terminal_pos", currentLanguage),
                icon = Icons.Default.CreditCard,
                isSelected = activeMode == TerminalMode.POS,
                onClick = { activeMode = TerminalMode.POS },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // === TERMINAL HARDWARE VISUALIZATION ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A1E), Color(0xFF101014))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
                .clickable { triggerTerminalTap() }
                .padding(22.dp)
                .testTag("terminal_hardware_target"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (activeMode) {
                    TerminalMode.INTERCOM -> IntercomHardwareDisplay(isAccessGranted, statusMessage)
                    TerminalMode.METRO -> MetroHardwareDisplay(isAccessGranted, statusMessage, fareDeducted)
                    TerminalMode.POS -> PosHardwareDisplay(isAccessGranted, statusMessage)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tap trigger button
                Button(
                    onClick = { triggerTerminalTap() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAccessGranted) Color(0xFF2E7D32) else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("terminal_tap_btn")
                ) {
                    Icon(
                        imageVector = if (isAccessGranted) Icons.Default.CheckCircle else Icons.Default.Nfc,
                        contentDescription = null,
                        tint = if (isAccessGranted) Color.White else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAccessGranted) "Сигнал передан" else AppStrings.get("terminal_tap_action", currentLanguage),
                        color = if (isAccessGranted) Color.White else Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Receipt animation if in POS mode
        AnimatedVisibility(
            visible = showReceipt && activeMode == TerminalMode.POS,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            ReceiptView(card = selectedCard)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card Selection Row
        Text(
            text = AppStrings.get("terminal_tap_prompt", currentLanguage),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(cards) { card ->
                val isSelected = card.id == selectedCardId
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(card.primaryColorHex))
                        .border(
                            width = if (isSelected) 2.5.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedCardId = card.id }
                        .padding(12.dp)
                        .testTag("select_terminal_card_${card.id}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = card.category.name.take(4),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = card.cardNumber.ifBlank { card.nfcUid },
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun TerminalModeTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IntercomHardwareDisplay(isUnlocked: Boolean, message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Intercom Brand / LED
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BAS-IP / SPUTNIK INTERCOM",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            // LED Light
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color(0xFF00E676) else Color(0xFFFF1744))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Center RFID Reader Target
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f))
                .border(
                    width = 2.dp,
                    color = if (isUnlocked) Color(0xFF00E676) else Color.White.copy(alpha = 0.25f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isUnlocked) Color(0xFF00E676) else Color.White,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // OLED Status Bar on Intercom
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0A0A0C))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = message,
                color = if (isUnlocked) Color(0xFF00E676) else Color(0xFFFFD700),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun MetroHardwareDisplay(isOpen: Boolean, message: String, fare: String?) {
    val flapRotation by animateFloatAsState(
        targetValue = if (isOpen) 60f else 0f,
        animationSpec = tween(350),
        label = "flap_rot"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ТУРНИКЕТ МЕТРО • ВХОД",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (fare != null) "СПИСАНИЕ $fare" else "ТАРИФ: СТАНДАРТ",
                color = if (fare != null) Color(0xFFFF5252) else Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Turnstile Gate with swing flaps
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0D0D10)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 3.dp.toPx()
                // Left turnstile pillar
                drawRect(
                    color = Color(0xFF2C2C2E),
                    topLeft = Offset(20.dp.toPx(), 10.dp.toPx()),
                    size = Size(35.dp.toPx(), size.height - 20.dp.toPx())
                )
                // Right turnstile pillar
                drawRect(
                    color = Color(0xFF2C2C2E),
                    topLeft = Offset(size.width - 55.dp.toPx(), 10.dp.toPx()),
                    size = Size(35.dp.toPx(), size.height - 20.dp.toPx())
                )
                // Gate Flaps
                val gateColor = if (isOpen) Color(0xFF00E676) else Color(0xFFFF1744)
                if (!isOpen) {
                    // Closed barrier
                    drawLine(
                        color = gateColor,
                        start = Offset(55.dp.toPx(), size.height / 2),
                        end = Offset(size.width - 55.dp.toPx(), size.height / 2),
                        strokeWidth = stroke * 2
                    )
                } else {
                    // Open barrier angled
                    drawLine(
                        color = gateColor,
                        start = Offset(55.dp.toPx(), size.height / 2),
                        end = Offset(95.dp.toPx(), 15.dp.toPx()),
                        strokeWidth = stroke * 2
                    )
                    drawLine(
                        color = gateColor,
                        start = Offset(size.width - 55.dp.toPx(), size.height / 2),
                        end = Offset(size.width - 95.dp.toPx(), 15.dp.toPx()),
                        strokeWidth = stroke * 2
                    )
                }
            }

            // Central Status Indicator
            Icon(
                imageVector = if (isOpen) Icons.Default.CheckCircle else Icons.Default.Subway,
                contentDescription = null,
                tint = if (isOpen) Color(0xFF00E676) else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            color = if (isOpen) Color(0xFF00E676) else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PosHardwareDisplay(isPaid: Boolean, message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "POS TERMINAL • EMV PAYPASS",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "СУММА: 1 450 ₸",
                color = Color(0xFFFFD700),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // POS Contactless wave symbol
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(if (isPaid) Color(0xFF1B5E20).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f))
                .border(
                    width = 2.dp,
                    color = if (isPaid) Color(0xFF00E676) else Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Nfc,
                contentDescription = null,
                tint = if (isPaid) Color(0xFF00E676) else Color.White,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            color = if (isPaid) Color(0xFF00E676) else Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ReceiptView(card: WalletCard?) {
    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFDE7))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "••• ЭЛЕКТРОННЫЙ ЧЕК КАССЫ •••",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ReceiptLine("Магазин", "WalletAll Store #042")
            ReceiptLine("Дата / Время", dateStr)
            ReceiptLine("Карта", card?.cardNumber ?: "•••• 9012")
            ReceiptLine("Тип операции", "ОПЛАТА ПО NFC")
            ReceiptLine("AUTH CODE", "OK-892419")
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black.copy(alpha = 0.2f)))
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "ИТОГО К ОПЛАТЕ:", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(text = "1 450 ₸", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun ReceiptLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Black.copy(alpha = 0.7f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
    }
}
