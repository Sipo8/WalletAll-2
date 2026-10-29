package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BarcodeFormat
import com.example.data.model.CardCategory
import com.example.data.model.CardVisualType
import com.example.data.model.WalletCard

@Entity(tableName = "wallet_cards")
data class WalletCardEntity(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val cardHolder: String,
    val cardNumber: String,
    val balance: Double?,
    val currency: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val textColorHex: Long,
    val barcodeValue: String,
    val barcodeFormat: String,
    val nfcTechType: String,
    val nfcUid: String,
    val facilityCode: String?,
    val isFavorite: Boolean,
    val visualType: String,
    val expiryDate: String?,
    val notes: String?,
    val createdAt: Long
) {
    fun toDomain(): WalletCard = WalletCard(
        id = id,
        category = try { CardCategory.valueOf(category) } catch (_: Exception) { CardCategory.ALL },
        title = title,
        subtitle = subtitle,
        cardHolder = cardHolder,
        cardNumber = cardNumber,
        balance = balance,
        currency = currency,
        primaryColorHex = primaryColorHex,
        secondaryColorHex = secondaryColorHex,
        textColorHex = textColorHex,
        barcodeValue = barcodeValue,
        barcodeFormat = try { BarcodeFormat.valueOf(barcodeFormat) } catch (_: Exception) { BarcodeFormat.NONE },
        nfcTechType = nfcTechType,
        nfcUid = nfcUid,
        facilityCode = facilityCode,
        isFavorite = isFavorite,
        visualType = try { CardVisualType.valueOf(visualType) } catch (_: Exception) { CardVisualType.APPLE_HOMEKEY },
        expiryDate = expiryDate,
        notes = notes,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(card: WalletCard): WalletCardEntity = WalletCardEntity(
            id = card.id,
            category = card.category.name,
            title = card.title,
            subtitle = card.subtitle,
            cardHolder = card.cardHolder,
            cardNumber = card.cardNumber,
            balance = card.balance,
            currency = card.currency,
            primaryColorHex = card.primaryColorHex,
            secondaryColorHex = card.secondaryColorHex,
            textColorHex = card.textColorHex,
            barcodeValue = card.barcodeValue,
            barcodeFormat = card.barcodeFormat.name,
            nfcTechType = card.nfcTechType,
            nfcUid = card.nfcUid,
            facilityCode = card.facilityCode,
            isFavorite = card.isFavorite,
            visualType = card.visualType.name,
            expiryDate = card.expiryDate,
            notes = card.notes,
            createdAt = card.createdAt
        )
    }
}
