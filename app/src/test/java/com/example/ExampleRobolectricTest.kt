package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppStrings
import com.example.data.model.CardCategory
import com.example.data.model.CatalogTemplates
import com.example.data.model.Language
import com.example.data.model.TechGuideData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches WalletAll`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WalletAll", appName)
    }

    @Test
    fun `verify catalog templates contains required categories`() {
        val templates = CatalogTemplates.templates
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.any { it.category == CardCategory.INTERCOM })
        assertTrue(templates.any { it.category == CardCategory.TRANSIT })
        assertTrue(templates.any { it.category == CardCategory.BANK })
        assertTrue(templates.any { it.category == CardCategory.LOYALTY })
        assertTrue(templates.any { it.category == CardCategory.PASS })
    }

    @Test
    fun `verify tech guide encyclopedia has all 7 deep chapters`() {
        val articles = TechGuideData.articles
        assertEquals(7, articles.size)
        assertNotNull(articles.find { it.id == "felica" })
        assertNotNull(articles.find { it.id == "calypso" })
        assertNotNull(articles.find { it.id == "aztec" })
        assertNotNull(articles.find { it.id == "pdf417" })
        assertNotNull(articles.find { it.id == "homekey" })
        assertNotNull(articles.find { it.id == "hardware_limits" })
        assertNotNull(articles.find { it.id == "wiegand" })
    }

    @Test
    fun `verify localization supports all 6 languages`() {
        val languages = listOf(
            Language.KK,
            Language.RU,
            Language.EN,
            Language.ES,
            Language.IT,
            Language.PT
        )
        for (lang in languages) {
            val appTitle = AppStrings.get("app_name", lang)
            assertEquals("WalletAll", appTitle)
            val walletTab = AppStrings.get("tab_wallet", lang)
            assertTrue(walletTab.isNotBlank())
        }
    }
}
