package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletCardDao {
    @Query("SELECT * FROM wallet_cards ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllCards(): Flow<List<WalletCardEntity>>

    @Query("SELECT * FROM wallet_cards WHERE id = :cardId LIMIT 1")
    suspend fun getCardById(cardId: String): WalletCardEntity?

    @Query("SELECT COUNT(*) FROM wallet_cards")
    suspend fun getCardCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: WalletCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<WalletCardEntity>)

    @Update
    suspend fun updateCard(card: WalletCardEntity)

    @Delete
    suspend fun deleteCard(card: WalletCardEntity)

    @Query("DELETE FROM wallet_cards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: String)

    @Query("UPDATE wallet_cards SET balance = :newBalance WHERE id = :cardId")
    suspend fun updateBalance(cardId: String, newBalance: Double)

    @Query("UPDATE wallet_cards SET isFavorite = :isFav WHERE id = :cardId")
    suspend fun updateFavorite(cardId: String, isFav: Boolean)
}
