package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BarcodeFormat
import com.example.data.model.CardCategory
import com.example.data.model.CardVisualType
import com.example.data.model.WalletCard

@Composable
fun WalletCardView(
    card: WalletCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
    onToggleFavorite: (() -> Unit)? = null
) {
    val cornerRadius = 22.dp
    val primaryColor = Color(card.primaryColorHex)
    val secondaryColor = Color(card.secondaryColorHex)
    val textColor = Color(card.textColorHex)

    val elevation by animateFloatAsState(
        targetValue = if (isExpanded) 12f else 6f,
        animationSpec = tween(250),
        label = "card_elevation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isExpanded) 208.dp else 90.dp)
            .shadow(elevation.dp, RoundedCornerShape(cornerRadius), ambientColor = Color.Black, spotColor = primaryColor.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 600f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable { onClick() }
            .testTag("wallet_card_${card.id}")
    ) {
        // Subtle background visual patterns
        when (card.visualType) {
            CardVisualType.APPLE_HOMEKEY -> {
                HomeKeyBackgroundConcentricRings()
            }
            CardVisualType.BANK_EMV -> {
                BankCardBackgroundPattern()
            }
            CardVisualType.DALLAS_IBUTTON -> {
                DallasButtonBackground()
            }
            CardVisualType.PLASTIC_FOB -> {
                PlasticFobRingHole()
            }
            else -> {
                // Transit / Loyalty subtle light shimmer
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // === HEADER ROW ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryBadgeIcon(card.category, card.visualType)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = card.title,
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (card.subtitle.isNotBlank()) {
                            Text(
                                text = card.subtitle,
                                color = textColor.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // NFC Contactless Wave icon
                    if (card.nfcTechType.isNotBlank() || card.category == CardCategory.INTERCOM || card.category == CardCategory.TRANSIT || card.category == CardCategory.BANK) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "NFC Enabled",
                            tint = textColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (onToggleFavorite != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (card.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Favorite",
                                tint = if (card.isFavorite) Color(0xFFFFD700) else textColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // === EXPANDED BODY ===
            if (isExpanded) {
                // Middle row: Chip, Barcode badge, or Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (card.visualType == CardVisualType.BANK_EMV) {
                        EmvGoldChip()
                    } else if (card.visualType == CardVisualType.APPLE_HOMEKEY) {
                        HomeKeyCentralGlyph()
                    } else if (card.barcodeFormat != BarcodeFormat.NONE) {
                        BarcodeFormatBadge(card.barcodeFormat)
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    if (card.balance != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Баланс",
                                color = textColor.copy(alpha = 0.65f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${"%,.0f".format(card.balance)} ${card.currency}",
                                color = textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bottom row: Card Number / UID and Holder Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = card.cardNumber.ifBlank { "UID ${card.nfcUid}" },
                            color = textColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        if (card.cardHolder.isNotBlank()) {
                            Text(
                                text = card.cardHolder.uppercase(),
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    if (card.expiryDate != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EXP",
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 9.sp
                            )
                            Text(
                                text = card.expiryDate,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBadgeIcon(category: CardCategory, visualType: CardVisualType) {
    val bgBrush = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.1f))
    )
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bgBrush)
            .border(0.8.dp, Color.White.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val icon = when {
            category == CardCategory.INTERCOM -> Icons.Default.Key
            category == CardCategory.TRANSIT -> Icons.Default.Subway
            category == CardCategory.BANK -> Icons.Default.CreditCard
            category == CardCategory.PASS -> Icons.Default.QrCode
            else -> Icons.Default.CreditCard
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Apple HomeKey concentric pulsing NFC rings in background
 */
@Composable
private fun HomeKeyBackgroundConcentricRings() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.8f, size.height * 0.5f)
        val maxRadius = size.height * 0.9f
        val rings = 5
        for (i in 1..rings) {
            val r = (maxRadius / rings) * i
            drawCircle(
                color = Color.White.copy(alpha = 0.04f + (i * 0.015f)),
                radius = r,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

/**
 * Bank card holographic wave background
 */
@Composable
private fun BankCardBackgroundPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 1.dp.toPx()
        for (i in 0..4) {
            drawCircle(
                color = Color.White.copy(alpha = 0.035f),
                radius = (size.width * 0.4f) + (i * 35f),
                center = Offset(size.width * 0.95f, size.height * 0.1f),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

/**
 * Dallas iButton metallic circular appearance
 */
@Composable
private fun DallasButtonBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.82f, size.height * 0.5f)
        val radius = 42.dp.toPx()
        // Metallic circular bezel
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE0E0E0), Color(0xFF757575), Color(0xFF424242)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.4f),
            radius = radius * 0.6f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Hole with steel key ring for plastic fobs
 */
@Composable
private fun PlasticFobRingHole() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.88f, 26.dp.toPx())
        // Outer steel ring
        drawCircle(
            brush = Brush.linearGradient(
                listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E), Color(0xFFEEEEEE))
            ),
            radius = 14.dp.toPx(),
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        // Inner cutout hole
        drawCircle(
            color = Color.Black.copy(alpha = 0.7f),
            radius = 6.dp.toPx(),
            center = center
        )
    }
}

/**
 * Realistic Gold EMV Smart Chip
 */
@Composable
private fun EmvGoldChip() {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFA000))
                )
            )
            .border(1.dp, Color(0xFFB78103), RoundedCornerShape(6.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 1.dp.toPx()
            val chipColor = Color(0xFF9E7002)
            // Center horizontal contact groove
            drawLine(
                color = chipColor,
                start = Offset(0f, size.height * 0.5f),
                end = Offset(size.width, size.height * 0.5f),
                strokeWidth = stroke
            )
            // Left contact line
            drawLine(
                color = chipColor,
                start = Offset(size.width * 0.35f, 0f),
                end = Offset(size.width * 0.35f, size.height),
                strokeWidth = stroke
            )
            // Right contact line
            drawLine(
                color = chipColor,
                start = Offset(size.width * 0.65f, 0f),
                end = Offset(size.width * 0.65f, size.height),
                strokeWidth = stroke
            )
            // Center micro-pad
            drawRoundRect(
                color = chipColor,
                topLeft = Offset(size.width * 0.35f, size.height * 0.3f),
                size = Size(size.width * 0.3f, size.height * 0.4f),
                cornerRadius = CornerRadius(2.dp.toPx()),
                style = Stroke(width = stroke)
            )
        }
    }
}

@Composable
private fun HomeKeyCentralGlyph() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Key,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "HOMEKEY",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun BarcodeFormatBadge(format: BarcodeFormat) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = format.name.replace("_", " "),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
