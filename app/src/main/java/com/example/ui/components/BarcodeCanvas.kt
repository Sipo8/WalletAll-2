package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BarcodeFormat
import kotlin.math.abs

@Composable
fun BarcodeCanvas(
    value: String,
    format: BarcodeFormat,
    modifier: Modifier = Modifier,
    barColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    showValueText: Boolean = true
) {
    if (format == BarcodeFormat.NONE || value.isBlank()) return

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (format) {
                BarcodeFormat.QR_CODE -> {
                    QrCodeVector(value = value, barColor = barColor, modifier = Modifier.height(130.dp).fillMaxWidth())
                }
                BarcodeFormat.AZTEC -> {
                    AztecCodeVector(value = value, barColor = barColor, modifier = Modifier.height(130.dp).fillMaxWidth())
                }
                BarcodeFormat.PDF417 -> {
                    Pdf417Vector(value = value, barColor = barColor, modifier = Modifier.height(75.dp).fillMaxWidth())
                }
                else -> {
                    LinearBarcodeVector(value = value, barColor = barColor, modifier = Modifier.height(75.dp).fillMaxWidth())
                }
            }

            if (showValueText) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    color = barColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun QrCodeVector(
    value: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizePx = minOf(size.width, size.height)
        val leftOffset = (size.width - sizePx) / 2f
        val topOffset = (size.height - sizePx) / 2f
        val grid = 25
        val cellSize = sizePx / grid

        // Draw deterministic matrix from value hash
        val seed = abs(value.hashCode())
        val pseudoRandom = java.util.Random(seed.toLong())

        for (r in 0 until grid) {
            for (c in 0 until grid) {
                val isFinderTopLeft = (r < 7 && c < 7)
                val isFinderTopRight = (r < 7 && c >= grid - 7)
                val isFinderBottomLeft = (r >= grid - 7 && c < 7)

                var isBlack = false
                if (isFinderTopLeft || isFinderTopRight || isFinderBottomLeft) {
                    val localR = if (r >= grid - 7) r - (grid - 7) else r
                    val localC = if (c >= grid - 7) c - (grid - 7) else c
                    // Draw 7x7 finder pattern (outer box, gap, inner 3x3)
                    if (localR == 0 || localR == 6 || localC == 0 || localC == 6 || (localR in 2..4 && localC in 2..4)) {
                        isBlack = true
                    }
                } else if (r == 6 || c == 6) {
                    // Timing pattern
                    isBlack = ((r + c) % 2 == 0)
                } else {
                    // Data modules
                    isBlack = pseudoRandom.nextBoolean()
                }

                if (isBlack) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(leftOffset + c * cellSize, topOffset + r * cellSize),
                        size = Size(cellSize + 0.5f, cellSize + 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AztecCodeVector(
    value: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizePx = minOf(size.width, size.height)
        val leftOffset = (size.width - sizePx) / 2f
        val topOffset = (size.height - sizePx) / 2f
        val grid = 23
        val cellSize = sizePx / grid
        val center = grid / 2

        val seed = abs(value.hashCode())
        val random = java.util.Random(seed.toLong())

        for (r in 0 until grid) {
            for (c in 0 until grid) {
                val dr = abs(r - center)
                val dc = abs(c - center)
                val maxDist = maxOf(dr, dc)

                var isBlack = false
                // Central Bullseye: nested squares
                if (maxDist == 0 || maxDist == 2 || maxDist == 4) {
                    isBlack = true
                } else if (maxDist == 1 || maxDist == 3) {
                    isBlack = false
                } else {
                    // Outer data layer with orientation bits
                    isBlack = random.nextBoolean()
                }

                if (isBlack) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(leftOffset + c * cellSize, topOffset + r * cellSize),
                        size = Size(cellSize + 0.5f, cellSize + 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LinearBarcodeVector(
    value: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val seed = abs(value.hashCode())
        val random = java.util.Random(seed.toLong())
        val totalBars = 65
        val barWidth = size.width / totalBars

        for (i in 0 until totalBars) {
            // Guard bars at start, center and end
            val isGuard = (i < 3 || i in (totalBars / 2 - 1)..(totalBars / 2 + 1) || i >= totalBars - 3)
            val isBar = if (isGuard) {
                (i % 2 == 0)
            } else {
                random.nextBoolean()
            }

            if (isBar) {
                val barHeight = if (isGuard) size.height else size.height * 0.88f
                drawRect(
                    color = barColor,
                    topLeft = Offset(i * barWidth, 0f),
                    size = Size(barWidth * 0.95f, barHeight)
                )
            }
        }
    }
}

@Composable
private fun Pdf417Vector(
    value: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val seed = abs(value.hashCode())
        val random = java.util.Random(seed.toLong())
        val rows = 12
        val cols = 70
        val cellW = size.width / cols
        val cellH = size.height / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val isStartPattern = (c < 5)
                val isStopPattern = (c >= cols - 5)
                val isBlack = when {
                    isStartPattern -> (c % 2 == 0)
                    isStopPattern -> (c % 2 == 1)
                    else -> random.nextInt(10) > 4
                }

                if (isBlack) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(c * cellW, r * cellH),
                        size = Size(cellW + 0.5f, cellH + 0.5f)
                    )
                }
            }
        }
    }
}
