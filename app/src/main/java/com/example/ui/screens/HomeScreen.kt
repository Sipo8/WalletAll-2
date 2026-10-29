package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.AppStrings
import com.example.data.model.CardCategory
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.WalletCardView

enum class WalletViewMode {
    STACKED_DECK,
    GRID
}

@Composable
fun HomeScreen(
    cards: List<WalletCard>,
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    onCardClick: (WalletCard) -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenConstructor: () -> Unit,
    onOpenScanner: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    var viewMode by remember { mutableStateOf(WalletViewMode.STACKED_DECK) }
    var selectedCategory by remember { mutableStateOf(CardCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showLanguageMenu by remember { mutableStateOf(false) }

    val filteredCards = remember(cards, selectedCategory, searchQuery) {
        cards.filter { card ->
            val matchCategory = when (selectedCategory) {
                CardCategory.ALL -> true
                CardCategory.FAVORITES -> card.isFavorite
                else -> card.category == selectedCategory
            }
            val matchSearch = searchQuery.isBlank() ||
                    card.title.contains(searchQuery, ignoreCase = true) ||
                    card.subtitle.contains(searchQuery, ignoreCase = true) ||
                    card.cardNumber.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // === TOP BAR: TITLE, LANGUAGE SWITCHER & VIEW MODE TOGGLE ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = AppStrings.get("app_name", currentLanguage),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "${filteredCards.size} карт и ключей",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // View Mode Toggle (Deck vs Grid)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(2.dp)
                ) {
                    Row {
                        IconButton(
                            onClick = { viewMode = WalletViewMode.STACKED_DECK },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (viewMode == WalletViewMode.STACKED_DECK) Color.White else Color.Transparent)
                                .testTag("toggle_deck_mode")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewAgenda,
                                contentDescription = "Deck",
                                tint = if (viewMode == WalletViewMode.STACKED_DECK) Color.Black else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewMode = WalletViewMode.GRID },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (viewMode == WalletViewMode.GRID) Color.White else Color.Transparent)
                                .testTag("toggle_grid_mode")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Grid",
                                tint = if (viewMode == WalletViewMode.GRID) Color.Black else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Language Selector
                Box {
                    IconButton(
                        onClick = { showLanguageMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1C1E))
                            .testTag("language_selector_btn")
                    ) {
                        Text(text = currentLanguage.flag, fontSize = 16.sp)
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        modifier = Modifier.background(Color(0xFF1E1E24))
                    ) {
                        Language.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(lang.flag, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(lang.label, color = Color.White, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    onLanguageChange(lang)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(AppStrings.get("search_hint", currentLanguage), fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("search_cards_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        CategoryFilterRow(
            selectedCategory = selectedCategory,
            currentLanguage = currentLanguage,
            onCategorySelected = { selectedCategory = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Action Bar (Catalog, Custom Card, Scan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                label = AppStrings.get("catalog", currentLanguage),
                onClick = onOpenCatalog,
                modifier = Modifier.weight(1f),
                tag = "quick_catalog_btn"
            )
            QuickActionButton(
                label = AppStrings.get("custom_card", currentLanguage),
                onClick = onOpenConstructor,
                modifier = Modifier.weight(1f),
                tag = "quick_constructor_btn"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // === CARDS VIEW: STACKED DECK OR GRID ===
        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = AppStrings.get("empty_cards", currentLanguage),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = AppStrings.get("empty_cards_sub", currentLanguage),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onOpenCatalog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(AppStrings.get("add_first_card", currentLanguage), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (viewMode == WalletViewMode.STACKED_DECK) {
            // Apple Wallet Cascading Deck: Overlapping cards showing headers
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(filteredCards, key = { _, card -> card.id }) { index, card ->
                    // Offset each successive card to create Apple Wallet overlap
                    val isLast = index == filteredCards.size - 1
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (isLast) 120.dp else 0.dp)
                            .offset(y = if (index == 0) 0.dp else (-64 * index).dp)
                    ) {
                        WalletCardView(
                            card = card,
                            onClick = { onCardClick(card) },
                            isExpanded = true,
                            onToggleFavorite = { onToggleFavorite(card.id, !card.isFavorite) }
                        )
                    }
                }
            }
        } else {
            // Compact Grid Mode
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCards, key = { it.id }) { card ->
                    CompactGridCard(
                        card = card,
                        onClick = { onCardClick(card) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF16161A))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CompactGridCard(
    card: WalletCard,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(card.primaryColorHex))
            .border(0.8.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
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
                    text = card.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            if (card.balance != null) {
                Text(
                    text = "${"%,.0f".format(card.balance)} ${card.currency}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = card.subtitle.ifBlank { card.category.name },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Text(
                text = card.cardNumber.ifBlank { card.nfcUid },
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
