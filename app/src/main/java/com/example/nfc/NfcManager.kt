package com.example.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcF
import android.os.Build
import com.example.data.model.BarcodeFormat
import com.example.data.model.CardCategory
import com.example.data.model.CardVisualType
import com.example.data.model.WalletCard

data class NfcScanResult(
    val uidHex: String,
    val uidDec: String,
    val techList: List<String>,
    val cardTypeGuessed: String,
    val ndefPayload: String? = null,
    val suggestedCategory: CardCategory = CardCategory.INTERCOM
)

class NfcManager(private val context: Context) {
    val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(context)
    }

    val isNfcSupported: Boolean
        get() = nfcAdapter != null

    val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    fun enableReaderMode(activity: Activity, onTagDiscovered: (NfcScanResult) -> Unit) {
        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        nfcAdapter?.enableReaderMode(
            activity,
            { tag ->
                val result = parseTag(tag)
                activity.runOnUiThread {
                    onTagDiscovered(result)
                }
            },
            flags,
            null
        )
    }

    fun disableReaderMode(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }

    fun parseIntent(intent: Intent): NfcScanResult? {
        val action = intent.action ?: return null
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            return tag?.let { parseTag(it) }
        }
        return null
    }

    fun parseTag(tag: Tag): NfcScanResult {
        val idBytes = tag.id ?: byteArrayOf()
        val hexUid = idBytes.joinToString(":") { "%02X".format(it) }
        val decUid = if (idBytes.isNotEmpty()) {
            var result = 0L
            for (b in idBytes) {
                result = (result shl 8) or (b.toLong() and 0xFF)
            }
            result.toString()
        } else "0"

        val techNames = tag.techList.map { it.substringAfterLast(".") }
        var typeGuessed = "NFC Tag (ISO 14443)"
        var category = CardCategory.INTERCOM

        if (techNames.contains("NfcF")) {
            typeGuessed = "Sony FeliCa (JIS X 6319-4)"
            category = CardCategory.TRANSIT
        } else if (techNames.contains("MifareClassic")) {
            typeGuessed = "Mifare Classic 1K/4K (13.56 MHz)"
            category = CardCategory.INTERCOM
        } else if (techNames.contains("MifareUltralight")) {
            typeGuessed = "Mifare Ultralight (Транспорт / Билет)"
            category = CardCategory.TRANSIT
        } else if (techNames.contains("Ndef")) {
            typeGuessed = "NDEF Smart Tag"
            category = CardCategory.LOYALTY
        }

        // Try reading NDEF payload
        var ndefContent: String? = null
        try {
            val ndef = Ndef.get(tag)
            ndef?.let {
                it.connect()
                val message = it.ndefMessage
                ndefContent = message?.records?.joinToString("; ") { record ->
                    String(record.payload)
                }
                it.close()
            }
        } catch (_: Exception) {}

        return NfcScanResult(
            uidHex = hexUid,
            uidDec = decUid,
            techList = techNames,
            cardTypeGuessed = typeGuessed,
            ndefPayload = ndefContent,
            suggestedCategory = category
        )
    }

    /**
     * Convert an NFC Scan result into a ready-to-save WalletCard
     */
    fun createCardFromScan(result: NfcScanResult, title: String): WalletCard {
        val visual = when (result.suggestedCategory) {
            CardCategory.INTERCOM -> CardVisualType.APPLE_HOMEKEY
            CardCategory.TRANSIT -> CardVisualType.TRANSIT_PASS
            CardCategory.BANK -> CardVisualType.BANK_EMV
            else -> CardVisualType.LOYALTY_PASS
        }

        return WalletCard(
            category = result.suggestedCategory,
            title = title.ifBlank { "NFC Ключ ${result.uidHex.take(8)}" },
            subtitle = result.cardTypeGuessed,
            cardHolder = "NFC Access Tag",
            cardNumber = "UID ${result.uidHex}",
            nfcTechType = "${result.cardTypeGuessed} • [${result.techList.joinToString(", ")}]",
            nfcUid = result.uidHex,
            barcodeValue = result.ndefPayload ?: result.uidHex,
            barcodeFormat = if (result.ndefPayload != null) BarcodeFormat.QR_CODE else BarcodeFormat.NONE,
            visualType = visual,
            primaryColorHex = 0xFF14171F,
            secondaryColorHex = 0xFF232836,
            notes = "Считано через NFC антенну устройства. Dec UID: ${result.uidDec}"
        )
    }

    /**
     * Mock simulator for generating realistic NFC scans in emulator
     */
    fun generateSimulatedScan(preset: String): NfcScanResult {
        return when (preset) {
            "mifare" -> NfcScanResult(
                uidHex = "3A:F1:8C:02",
                uidDec = "988916738",
                techList = listOf("NfcA", "MifareClassic", "NdefFormatable"),
                cardTypeGuessed = "Mifare Classic 1K (13.56 МГц)",
                suggestedCategory = CardCategory.INTERCOM
            )
            "felica" -> NfcScanResult(
                uidHex = "01:2E:4B:91:83:A0:5C:21",
                uidDec = "850901239851",
                techList = listOf("NfcF"),
                cardTypeGuessed = "Sony FeliCa (JIS X 6319-4)",
                suggestedCategory = CardCategory.TRANSIT
            )
            "homekey" -> NfcScanResult(
                uidHex = "04:A2:33:5F:91:20:80",
                uidDec = "1308492049182",
                techList = listOf("NfcA", "IsoDep"),
                cardTypeGuessed = "Apple HomeKey ECP (Aliro)",
                suggestedCategory = CardCategory.INTERCOM
            )
            else -> NfcScanResult(
                uidHex = "04:6A:B2:3F:81",
                uidDec = "1884920194",
                techList = listOf("NfcA", "MifareUltralight"),
                cardTypeGuessed = "Mifare Ultralight C",
                suggestedCategory = CardCategory.TRANSIT
            )
        }
    }
}
