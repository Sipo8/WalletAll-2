package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStrings
import com.example.data.model.BarcodeFormat
import com.example.data.model.CardCategory
import com.example.data.model.CardVisualType
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.example.ui.components.WalletCardView

@Composable
fun CardConstructorSheet(
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onSaveCard: (WalletCard) -> Unit,
    initialCard: WalletCard? = null
) {
    var title by remember { mutableStateOf(initialCard?.title ?: "") }
    var subtitle by remember { mutableStateOf(initialCard?.subtitle ?: "") }
    var holder by remember { mutableStateOf(initialCard?.cardHolder ?: "") }
    var numberOrUid by remember { mutableStateOf(initialCard?.cardNumber ?: "04:A2:8B:19") }
    var facilityCode by remember { mutableStateOf(initialCard?.facilityCode ?: "") }
    var barcodeValue by remember { mutableStateOf(initialCard?.barcodeValue ?: "") }
    var barcodeFormat by remember { mutableStateOf(initialCard?.barcodeFormat ?: BarcodeFormat.CODE_128) }
    var category by remember { mutableStateOf(initialCard?.category ?: CardCategory.INTERCOM) }
    var visualType by remember { mutableStateOf(initialCard?.visualType ?: CardVisualType.APPLE_HOMEKEY) }
    var balanceStr by remember { mutableStateOf(initialCard?.balance?.toString() ?: "") }
    var currency by remember { mutableStateOf(initialCard?.currency ?: "₸") }

    val colorPresets = listOf(
        Pair(0xFF141416L, 0xFF23242AL), // Obsidian Black
        Pair(0xFF003049L, 0xFF0D5C75L), // Deep Marine Blue
        Pair(0xFF005B52L, 0xFF00897BL), // Emerald Teal
        Pair(0xFF780016L, 0xFF9E2A2BL), // Ruby Red
        Pair(0xFF3D2645L, 0xFF832161L), // Velvet Purple
        Pair(0xFF2C2C2EL, 0xFF48484AL)  // Titanium Space Gray
    )
    var selectedColorIndex by remember { mutableStateOf(0) }

    val previewCard = WalletCard(
        id = initialCard?.id ?: "preview",
        category = category,
        title = title.ifBlank { "Новая карта" },
        subtitle = subtitle.ifBlank { "WalletAll Pass" },
        cardHolder = holder,
        cardNumber = numberOrUid,
        facilityCode = facilityCode.ifBlank { null },
        barcodeValue = barcodeValue,
        barcodeFormat = barcodeFormat,
        visualType = visualType,
        balance = balanceStr.toDoubleOrNull(),
        currency = currency,
        primaryColorHex = colorPresets[selectedColorIndex].first,
        secondaryColorHex = colorPresets[selectedColorIndex].second,
        nfcTechType = if (category == CardCategory.INTERCOM) "Mifare Classic 1K" else "NFC Contactless"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Конструктор карты / ключа",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Live Card Preview
                WalletCardView(
                    card = previewCard,
                    onClick = {},
                    isExpanded = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Color Presets Row
                Text(
                    text = "Цвет и текстура",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPresets.forEachIndexed { index, pair ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(pair.first))
                                .border(
                                    width = if (selectedColorIndex == index) 2.5.dp else 1.dp,
                                    color = if (selectedColorIndex == index) Color.White else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorIndex == index) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "Категория",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val catList = listOf(
                        CardCategory.INTERCOM to "Домофон",
                        CardCategory.TRANSIT to "Транспорт",
                        CardCategory.BANK to "Банк",
                        CardCategory.LOYALTY to "Скидки"
                    )
                    catList.forEach { (cat, label) ->
                        val isSel = category == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) Color.White else Color(0xFF1E1E22))
                                .clickable {
                                    category = cat
                                    visualType = when (cat) {
                                        CardCategory.INTERCOM -> CardVisualType.APPLE_HOMEKEY
                                        CardCategory.BANK -> CardVisualType.BANK_EMV
                                        CardCategory.TRANSIT -> CardVisualType.TRANSIT_PASS
                                        else -> CardVisualType.LOYALTY_PASS
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Fields
                DarkInputField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Название (например: Домофон Sputnik, Фитнес)",
                    tag = "input_title"
                )
                Spacer(modifier = Modifier.height(10.dp))

                DarkInputField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = "Подзаголовок / Модель чипа",
                    tag = "input_subtitle"
                )
                Spacer(modifier = Modifier.height(10.dp))

                DarkInputField(
                    value = numberOrUid,
                    onValueChange = { numberOrUid = it },
                    label = "Номер карты или NFC Hex UID (например: 04:A2:8B:19)",
                    tag = "input_uid"
                )
                Spacer(modifier = Modifier.height(10.dp))

                DarkInputField(
                    value = facilityCode,
                    onValueChange = { facilityCode = it },
                    label = "Facility Code (Wiegand-26, опционально)",
                    tag = "input_fc"
                )
                Spacer(modifier = Modifier.height(10.dp))

                DarkInputField(
                    value = holder,
                    onValueChange = { holder = it },
                    label = "Владелец / Номер квартиры",
                    tag = "input_holder"
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Barcode Format Selector
                Text(
                    text = "Формат штрихкода",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val formats = listOf(
                        BarcodeFormat.QR_CODE to "QR",
                        BarcodeFormat.CODE_128 to "Code128",
                        BarcodeFormat.EAN_13 to "EAN13",
                        BarcodeFormat.AZTEC to "Aztec",
                        BarcodeFormat.PDF417 to "PDF417"
                    )
                    formats.forEach { (fmt, label) ->
                        val isSel = barcodeFormat == fmt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color(0xFFFFD700) else Color(0xFF1E1E22))
                                .clickable { barcodeFormat = fmt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                DarkInputField(
                    value = barcodeValue,
                    onValueChange = { barcodeValue = it },
                    label = "Значение штрихкода",
                    tag = "input_barcode_val"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        val finalCard = previewCard.copy(
                            id = initialCard?.id ?: java.util.UUID.randomUUID().toString()
                        )
                        onSaveCard(finalCard)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_card_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(
                        text = "Сохранить карту в кошелек",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DarkInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    )
}
