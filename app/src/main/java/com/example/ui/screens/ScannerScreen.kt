package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.AppStrings
import com.example.data.model.BarcodeFormat
import com.example.data.model.CardCategory
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.example.nfc.NfcManager
import com.example.nfc.NfcScanResult
import com.example.nfc.SoundManager

enum class ScannerTab {
    BARCODE,
    NFC
}

@Composable
fun ScannerScreen(
    currentLanguage: Language,
    onAddScannedCard: (WalletCard) -> Unit,
    lastScannedNfcResult: NfcScanResult? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val soundManager = remember { SoundManager(context) }
    val nfcManager = remember { NfcManager(context) }

    var activeTab by remember { mutableStateOf(ScannerTab.BARCODE) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var detectedCardResult by remember { mutableStateOf<NfcScanResult?>(lastScannedNfcResult) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(lastScannedNfcResult) {
        if (lastScannedNfcResult != null) {
            detectedCardResult = lastScannedNfcResult
            soundManager.playIntercomUnlockSound()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF16161A))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeTab == ScannerTab.BARCODE) Color.White else Color.Transparent)
                    .clickable { activeTab = ScannerTab.BARCODE }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = if (activeTab == ScannerTab.BARCODE) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Камера (Штрихкод/QR)",
                        color = if (activeTab == ScannerTab.BARCODE) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeTab == ScannerTab.NFC) Color.White else Color.Transparent)
                    .clickable { activeTab = ScannerTab.NFC }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        tint = if (activeTab == ScannerTab.NFC) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Считыватель NFC",
                        color = if (activeTab == ScannerTab.NFC) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeTab == ScannerTab.BARCODE) {
            // === CAMERA VIEW / SCANNER WITH LASER ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF101014))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Laser Scanning Overlay with animated beam
                LaserScannerOverlay()

                // Bottom floating quick test bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EAN-13 • Code 128 • QR • Aztec • PDF417",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SimulateBarcodeButton("QR Код", "KASPI-QR-PAY-4819") { code ->
                            soundManager.playPosBeep()
                            val card = WalletCard(
                                category = CardCategory.LOYALTY,
                                title = "Kaspi QR Скан",
                                subtitle = "QR Оплата",
                                barcodeValue = code,
                                barcodeFormat = BarcodeFormat.QR_CODE,
                                cardNumber = "QR 4819"
                            )
                            onAddScannedCard(card)
                        }
                        SimulateBarcodeButton("Aztec Билет", "KTZ-PASS-AZTEC-042") { code ->
                            soundManager.playMetroDing()
                            val card = WalletCard(
                                category = CardCategory.PASS,
                                title = "Ж/Д Билет Aztec",
                                subtitle = "КТЖ Экспресс",
                                barcodeValue = code,
                                barcodeFormat = BarcodeFormat.AZTEC,
                                cardNumber = "Билет #042"
                            )
                            onAddScannedCard(card)
                        }
                    }
                }
            }
        } else {
            // === NFC HARDWARE READER & SIMULATOR ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Reader Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF16161A))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (nfcManager.isNfcSupported) "NFC Антенна активна • Поднесите метку к задней крышке" else "NFC в режиме эмуляции (используйте кнопки ниже)",
                        color = if (nfcManager.isNfcSupported) Color(0xFF00E676) else Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Central NFC Pulsing Circle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(2.dp, Brush.radialGradient(listOf(Color(0xFF00E676), Color.Transparent)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC Reader",
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }

                // Detected Tag Info Card (if any scanned)
                if (detectedCardResult != null) {
                    val result = detectedCardResult!!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1A1A22))
                            .border(1.dp, Color(0xFF00E676), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ЧИП ОБНАРУЖЕН",
                                    color = Color(0xFF00E676),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = result.cardTypeGuessed,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Hex UID: ${result.uidHex}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Dec UID: ${result.uidDec} • [${result.techList.joinToString(", ")}]",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val card = nfcManager.createCardFromScan(result, "")
                                    onAddScannedCard(card)
                                    detectedCardResult = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Добавить ключ в кошелек", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Simulator Presets
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Быстрое тестирование чипов (Симулятор):",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                soundManager.playIntercomUnlockSound()
                                detectedCardResult = nfcManager.generateSimulatedScan("mifare")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Mifare 1K", color = Color.White, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                soundManager.playMetroDing()
                                detectedCardResult = nfcManager.generateSimulatedScan("felica")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sony FeliCa", color = Color.White, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                soundManager.playIntercomUnlockSound()
                                detectedCardResult = nfcManager.generateSimulatedScan("homekey")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apple HomeKey", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulateBarcodeButton(label: String, value: String, onScan: (String) -> Unit) {
    Button(
        onClick = { onScan(value) },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Animated Laser Beam Scanner Overlay
 */
@Composable
private fun LaserScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val targetSize = minOf(size.width * 0.72f, size.height * 0.55f)
        val left = (size.width - targetSize) / 2f
        val top = (size.height - targetSize) / 2f
        val right = left + targetSize
        val bottom = top + targetSize
        val bracketLen = 26.dp.toPx()
        val stroke = 3.dp.toPx()

        // Corner Brackets
        val bracketColor = Color(0xFF00E5FF)
        // Top-left
        drawLine(bracketColor, Offset(left, top), Offset(left + bracketLen, top), stroke)
        drawLine(bracketColor, Offset(left, top), Offset(left, top + bracketLen), stroke)
        // Top-right
        drawLine(bracketColor, Offset(right, top), Offset(right - bracketLen, top), stroke)
        drawLine(bracketColor, Offset(right, top), Offset(right, top + bracketLen), stroke)
        // Bottom-left
        drawLine(bracketColor, Offset(left, bottom), Offset(left + bracketLen, bottom), stroke)
        drawLine(bracketColor, Offset(left, bottom), Offset(left, bottom - bracketLen), stroke)
        // Bottom-right
        drawLine(bracketColor, Offset(right, bottom), Offset(right - bracketLen, bottom), stroke)
        drawLine(bracketColor, Offset(right, bottom), Offset(right, bottom - bracketLen), stroke)

        // Pulsing Red Laser Line
        val currentLaserY = top + (targetSize * laserY)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color(0xFFFF1744), Color.White, Color(0xFFFF1744), Color.Transparent),
                startX = left,
                endX = right
            ),
            start = Offset(left, currentLaserY),
            end = Offset(right, currentLaserY),
            strokeWidth = 3.5.dp.toPx()
        )
    }
}
