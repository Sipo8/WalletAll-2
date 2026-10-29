package com.example.data.model

enum class CardCategory {
    ALL,
    INTERCOM,   // Домофонные чипы и СКУД
    TRANSIT,    // Транспортные карты
    BANK,       // Банковские карты
    LOYALTY,    // Бонусные и скидочные
    PASS,       // Билеты и пропуска
    FAVORITES   // Избранные
}

enum class BarcodeFormat {
    NONE,
    QR_CODE,
    CODE_128,
    EAN_13,
    AZTEC,
    PDF417,
    DATA_MATRIX
}

enum class CardVisualType {
    APPLE_HOMEKEY, // Titanium/Obsidian matte with concentric NFC rings
    BANK_EMV,      // Metallic card with gold EMV chip and contactless wave
    TRANSIT_PASS,  // Transit card with network branding and balance
    LOYALTY_PASS,  // Clean loyalty card with prominent barcode
    DALLAS_IBUTTON,// Realistic Dallas DS1990A metallic tablet
    PLASTIC_FOB    // Teardrop plastic fob with steel key ring
}
