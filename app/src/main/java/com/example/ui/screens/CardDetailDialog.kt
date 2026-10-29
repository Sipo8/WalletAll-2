package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStrings
import com.example.data.model.BarcodeFormat
import com.example.data.model.CardCategory
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.example.ui.components.BarcodeCanvas
import com.example.ui.components.WalletCardView

@Composable
fun CardDetailDialog(
    card: WalletCard,
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onUpdateBalance: (String, Double) -> Unit,
    onTapNearReader: (WalletCard) -> Unit
) {
    val context = LocalContext.current
    var isFlipped by remember { mutableStateOf(false) }
    var isMaxBrightness by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }

    // Control screen brightness for scanning
    DisposableEffect(isMaxBrightness) {
        val activity = context as? Activity
        val originalBrightness = activity?.window?.attributes?.screenBrightness
        if (isMaxBrightness && activity != null) {
            val layoutParams = activity.window.attributes
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            activity.window.attributes = layoutParams
        }
        onDispose {
            if (activity != null && originalBrightness != null) {
                val layoutParams = activity.window.attributes
                layoutParams.screenBrightness = originalBrightness
                activity.window.attributes = layoutParams
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Grab Handle & Close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onToggleFavorite(card.id, !card.isFavorite) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) Color(0xFFFFD700) else Color.White
                        )
                    }

                    // iOS Grab handle bar in center
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .testTag("card_detail_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === CARD SURFACE (FRONT OR BACK) ===
                if (!isFlipped) {
                    WalletCardView(
                        card = card,
                        onClick = { onTapNearReader(card) },
                        isExpanded = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CardBackView(card = card, onFlipBack = { isFlipped = false })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === ACTION BUTTONS ROW ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onTapNearReader(card) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hold_near_reader_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.get("hold_near_reader", currentLanguage),
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { isFlipped = !isFlipped },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("flip_card_btn"),
                        shape = RoundedCornerShape(24.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f))))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Flip",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // === BARCODE / QR SECTION ===
                if (card.barcodeFormat != BarcodeFormat.NONE && card.barcodeValue.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF141416))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = card.barcodeFormat.name.replace("_", " "),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                // Max Brightness Toggle Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isMaxBrightness) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f))
                                        .clickable { isMaxBrightness = !isMaxBrightness }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("max_brightness_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Brightness7,
                                        contentDescription = null,
                                        tint = if (isMaxBrightness) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.get("max_brightness", currentLanguage),
                                        color = if (isMaxBrightness) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            BarcodeCanvas(
                                value = card.barcodeValue,
                                format = card.barcodeFormat,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // === TECHNICAL SPECS CARD ===
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF141416))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Параметры доступа & СКУД",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (card.nfcTechType.isNotBlank()) {
                            SpecRow("Технология / Чип", card.nfcTechType)
                        }
                        if (card.nfcUid.isNotBlank()) {
                            SpecRow("UID метки (Hex)", card.nfcUid)
                        }
                        if (card.facilityCode != null) {
                            SpecRow("Facility Code (Wiegand)", card.facilityCode)
                        }
                        if (card.notes != null) {
                            SpecRow("Примечание", card.notes)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // === BALANCE TOP UP / DELETE ROW ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (card.balance != null) {
                        Button(
                            onClick = {
                                onUpdateBalance(card.id, (card.balance ?: 0.0) + 1000.0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("+1,000 ${card.currency} (Пополнить)", color = Color.White, fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    Button(
                        onClick = {
                            onDelete(card.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("delete_card_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.get("delete", currentLanguage), color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CardBackView(
    card: WalletCard,
    onFlipBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(208.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF1E1E22))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
            .clickable { onFlipBack() }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ОБРАТНАЯ СТОРОНА КАРТЫ",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Нажмите для возврата",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp
                )
            }

            // Magnetic stripe simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF0D0D0F))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CVV / КЛЮЧ",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                    Text(
                        text = "••• (NFC Secured)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "WalletAll Digital Secure Element",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
