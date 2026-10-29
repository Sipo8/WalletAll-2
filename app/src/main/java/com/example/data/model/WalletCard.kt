package com.example.data.model

import java.util.UUID

data class WalletCard(
    val id: String = UUID.randomUUID().toString(),
    val category: CardCategory,
    val title: String,
    val subtitle: String = "",
    val cardHolder: String = "",
    val cardNumber: String = "",
    val balance: Double? = null,
    val currency: String = "",
    val primaryColorHex: Long = 0xFF1C1C1E,
    val secondaryColorHex: Long = 0xFF2C2C2E,
    val textColorHex: Long = 0xFFFFFFFF,
    val barcodeValue: String = "",
    val barcodeFormat: BarcodeFormat = BarcodeFormat.NONE,
    val nfcTechType: String = "",
    val nfcUid: String = "",
    val facilityCode: String? = null,
    val isFavorite: Boolean = false,
    val visualType: CardVisualType = CardVisualType.APPLE_HOMEKEY,
    val expiryDate: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
