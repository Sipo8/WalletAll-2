package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun CategoryFilterRow(
    selectedCategory: CardCategory,
    currentLanguage: Language,
    onCategorySelected: (CardCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CardCategory.ALL to "cat_all",
        CardCategory.INTERCOM to "cat_intercom",
        CardCategory.TRANSIT to "cat_transit",
        CardCategory.BANK to "cat_bank",
        CardCategory.LOYALTY to "cat_loyalty",
        CardCategory.PASS to "cat_pass",
        CardCategory.FAVORITES to "cat_fav"
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (category, stringKey) ->
            val isSelected = selectedCategory == category
            val label = AppStrings.get(stringKey, currentLanguage)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) Color.White else Color(0xFF1C1C1E)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("filter_chip_${category.name.lowercase()}")
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
