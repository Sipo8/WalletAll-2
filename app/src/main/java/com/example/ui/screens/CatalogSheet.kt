package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.CatalogTemplates
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.example.ui.components.CategoryFilterRow
import com.example.data.model.CardCategory

@Composable
fun CatalogSheet(
    existingCardTitles: Set<String>,
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onAddCard: (WalletCard) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(CardCategory.ALL) }
    var addedIds by remember { mutableStateOf(setOf<String>()) }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == CardCategory.ALL) {
            CatalogTemplates.templates
        } else {
            CatalogTemplates.templates.filter { it.category == selectedCategory }
        }
    }

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
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Каталог шаблонов карт",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Добавление в 1 клик • Казахстан, СНГ и Мир",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .testTag("catalog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Category Chips
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    currentLanguage = currentLanguage,
                    onCategorySelected = { selectedCategory = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                // Templates List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTemplates, key = { it.id }) { template ->
                        val isAlreadyAdded = existingCardTitles.contains(template.title) || addedIds.contains(template.id)

                        CatalogItemRow(
                            template = template,
                            isAdded = isAlreadyAdded,
                            onAdd = {
                                if (!isAlreadyAdded) {
                                    addedIds = addedIds + template.id
                                    onAddCard(template.copy(id = java.util.UUID.randomUUID().toString()))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogItemRow(
    template: WalletCard,
    isAdded: Boolean,
    onAdd: () -> Unit
) {
    val primaryColor = Color(template.primaryColorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF16161A))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .clickable(enabled = !isAdded) { onAdd() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini colored card preview indicator
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(primaryColor)
                    .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = template.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = template.subtitle,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
                if (template.nfcTechType.isNotBlank()) {
                    Text(
                        text = template.nfcTechType,
                        color = Color(0xFFFFD700).copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Add Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isAdded) Color(0xFF2E7D32).copy(alpha = 0.3f) else Color.White)
                .clickable(enabled = !isAdded) { onAdd() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("catalog_add_${template.id}"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isAdded) Color(0xFF81C784) else Color.Black,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAdded) "Добавлено" else "Добавить",
                    color = if (isAdded) Color(0xFF81C784) else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
