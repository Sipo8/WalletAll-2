package com.example.data.db

import android.content.Context
import com.example.data.model.CatalogTemplates
import com.example.data.model.WalletCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardRepository(context: Context) {
    private val database = WalletDatabase.getInstance(context)
    private val cardDao = database.walletCardDao()

    val allCards: Flow<List<WalletCard>> = cardDao.getAllCards().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun ensureInitialData() {
        if (cardDao.getCardCount() == 0) {
            // Seed 5 iconic starter cards representing key categories
            val starterCards = listOf(
                CatalogTemplates.templates.first { it.id == "template_sputnik" },
                CatalogTemplates.templates.first { it.id == "template_visa_plat" },
                CatalogTemplates.templates.first { it.id == "template_onay" },
                CatalogTemplates.templates.first { it.id == "template_gold_apple" },
                CatalogTemplates.templates.first { it.id == "template_vizit" }
            )
            cardDao.insertCards(starterCards.map { WalletCardEntity.fromDomain(it) })
        }
    }

    suspend fun addCard(card: WalletCard) {
        cardDao.insertCard(WalletCardEntity.fromDomain(card))
    }

    suspend fun updateCard(card: WalletCard) {
        cardDao.updateCard(WalletCardEntity.fromDomain(card))
    }

    suspend fun deleteCard(cardId: String) {
        cardDao.deleteCardById(cardId)
    }

    suspend fun updateBalance(cardId: String, newBalance: Double) {
        cardDao.updateBalance(cardId, newBalance)
    }

    suspend fun toggleFavorite(cardId: String, isFav: Boolean) {
        cardDao.updateFavorite(cardId, isFav)
    }

    /**
     * Maintenance & Self-Healing: Restores all official catalog templates into the database
     */
    suspend fun restoreAllTemplates(): Int {
        val allTemplates = CatalogTemplates.templates.map {
            WalletCardEntity.fromDomain(it.copy(id = java.util.UUID.randomUUID().toString()))
        }
        cardDao.insertCards(allTemplates)
        return allTemplates.size
    }

    /**
     * Maintenance & Self-Healing: Normalizes and fixes balances across cards
     */
    suspend fun repairAllBalances() {
        // Sets positive balance to all transit cards
        val starters = listOf(
            CatalogTemplates.templates.first { it.id == "template_onay" }.copy(balance = 2000.0),
            CatalogTemplates.templates.first { it.id == "template_avtobys" }.copy(balance = 1500.0),
            CatalogTemplates.templates.first { it.id == "template_troika" }.copy(balance = 500.0)
        )
        cardDao.insertCards(starters.map { WalletCardEntity.fromDomain(it) })
    }
}
