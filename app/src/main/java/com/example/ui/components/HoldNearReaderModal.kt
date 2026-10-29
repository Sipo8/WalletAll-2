package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStrings
import com.example.data.model.CardCategory
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.example.nfc.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HoldNearReaderModal(
    card: WalletCard,
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val scope = rememberCoroutineScope()
    var isSuccess by remember { mutableStateOf(false) }

    fun triggerTap() {
        if (isSuccess) return
        isSuccess = true
        when (card.category) {
            CardCategory.INTERCOM -> soundManager.playIntercomUnlockSound()
            CardCategory.TRANSIT -> soundManager.playMetroDing()
            else -> soundManager.playPosBeep()
        }
        scope.launch {
            delay(1600)
            onSuccess?.invoke()
            onDismiss()
        }
    }

    // Auto-trigger tap simulation after 2 seconds for smooth demo
    LaunchedEffect(Unit) {
        delay(1800)
        if (!isSuccess) {
            triggerTap()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .padding(24.dp)
        ) {
            // Close Button top-right
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .testTag("hold_reader_close")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top prompt text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isSuccess) AppStrings.get("done", currentLanguage) else AppStrings.get("hold_near_reader", currentLanguage),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSuccess) AppStrings.get("access_granted", currentLanguage) else AppStrings.get("ready_to_tap", currentLanguage),
                        color = if (isSuccess) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.65f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Center animated NFC reader rings or Success checkmark
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { triggerTap() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!isSuccess) {
                        PulsingNfcRings()
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "NFC Wave",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(68.dp)
                            )
                        }
                    }
                }

                // Card preview at the bottom
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WalletCardView(
                        card = card,
                        onClick = { triggerTap() },
                        isExpanded = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!isSuccess) {
                        Button(
                            onClick = { triggerTap() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("simulate_tap_button")
                        ) {
                            Text(
                                text = "Имитировать касание NFC",
                                color = Color.White,
                                fontSize = 14.sp,
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
private fun PulsingNfcRings() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.width / 2f

        // Ring 1
        drawCircle(
            color = Color.White.copy(alpha = (1f - pulse1) * 0.4f),
            radius = maxRadius * pulse1,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        // Ring 2
        drawCircle(
            color = Color.White.copy(alpha = (1f - pulse2) * 0.35f),
            radius = maxRadius * pulse2,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
