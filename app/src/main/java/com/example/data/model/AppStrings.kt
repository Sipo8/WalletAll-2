package com.example.data.model

enum class Language(val code: String, val label: String, val flag: String) {
    KK("kk", "Қазақша", "🇰🇿"),
    RU("ru", "Русский", "🇷🇺"),
    EN("en", "English", "🇬🇧"),
    ES("es", "Español", "🇪🇸"),
    IT("it", "Italiano", "🇮🇹"),
    PT("pt", "Português", "🇵🇹")
}

object AppStrings {
    fun get(key: String, lang: Language): String {
        val dict = when (lang) {
            Language.KK -> kk
            Language.RU -> ru
            Language.EN -> en
            Language.ES -> es
            Language.IT -> it
            Language.PT -> pt
        }
        return dict[key] ?: ru[key] ?: key
    }

    private val ru = mapOf(
        "app_name" to "WalletAll",
        "tab_wallet" to "Кошелек",
        "tab_scanner" to "Сканер",
        "tab_terminal" to "Терминал",
        "tab_guide" to "База знаний",
        "tab_ai" to "Поддержка",

        "search_hint" to "Поиск карт и ключей...",
        "deck_mode" to "Стопка",
        "grid_mode" to "Сетка",
        "add_card" to "Добавить",
        "catalog" to "Каталог",
        "custom_card" to "Своя карта",
        "scan_barcode" to "Сканировать",
        "read_nfc" to "Считать NFC",
        "empty_cards" to "Ваш кошелек пуст",
        "empty_cards_sub" to "Добавьте карту из каталога или отсканируйте чип",
        "add_first_card" to "Добавить первую карту",

        "cat_all" to "Все",
        "cat_intercom" to "Домофоны",
        "cat_transit" to "Транспорт",
        "cat_bank" to "Банк",
        "cat_loyalty" to "Скидки",
        "cat_pass" to "Билеты",
        "cat_fav" to "Избранное",

        "hold_near_reader" to "Поднесите к считывателю",
        "ready_to_tap" to "Готов к передаче NFC",
        "double_click_pay" to "Дважды нажмите боковую кнопку",
        "access_granted" to "ДОСТУП РАЗРЕШЕН",
        "done" to "Готово",
        "max_brightness" to "Макс. яркость",
        "flip_card" to "Перевернуть",
        "top_up" to "Пополнить",
        "delete" to "Удалить",
        "close" to "Закрыть",
        "save" to "Сохранить",
        "cancel" to "Отмена",
        "favorite" to "В избранное",

        "terminal_title" to "Интерактивный NFC терминал",
        "terminal_intercom" to "Домофон / Замок",
        "terminal_metro" to "Турникет метро",
        "terminal_pos" to "POS Оплата",
        "terminal_tap_prompt" to "Выберите карту и приложите к терминалу",
        "terminal_tap_action" to "Приложить выбранную карту",
        "intercom_sound_desc" to "Электромагнитный щелчок замка",
        "metro_fare" to "Списание проезда",
        "pos_success" to "Оплата одобрена",
        "receipt" to "Электронный чек",

        "guide_title" to "Глобальная база технологий доступа",
        "guide_subtitle" to "Всё о NFC, RFID 125 кГц, FeliCa, Dallas, HomeKey и штрихкодах",

        "ai_title" to "WalletAll AI Ассистент",
        "ai_subtitle" to "Эксперт по чипам, NFC, клонированию и картам",
        "ai_photo_btn" to "Анализ фото карты",
        "ai_send_hint" to "Спросите про чипы, домофоны или карты..."
    )

    private val kk = mapOf(
        "app_name" to "WalletAll",
        "tab_wallet" to "Әмиян",
        "tab_scanner" to "Сканер",
        "tab_terminal" to "Терминал",
        "tab_guide" to "Білім қоры",
        "tab_ai" to "AI Көмекші",

        "search_hint" to "Карталар мен кілттерді іздеу...",
        "deck_mode" to "Қатпар",
        "grid_mode" to "Тор",
        "add_card" to "Қосу",
        "catalog" to "Каталог",
        "custom_card" to "Өз картаң",
        "scan_barcode" to "Сканерлеу",
        "read_nfc" to "NFC оқу",
        "empty_cards" to "Әмияныңыз бос",
        "empty_cards_sub" to "Каталогтан карта қосыңыз немесе чипті сканерлеңіз",
        "add_first_card" to "Алғашқы картаны қосу",

        "cat_all" to "Барлығы",
        "cat_intercom" to "Домофондар",
        "cat_transit" to "Көлік",
        "cat_bank" to "Банк",
        "cat_loyalty" to "Жеңілдіктер",
        "cat_pass" to "Билеттер",
        "cat_fav" to "Таңдаулы",

        "hold_near_reader" to "Оқу құрылғысына жақындатыңыз",
        "ready_to_tap" to "NFC беруге дайын",
        "double_click_pay" to "Төлем үшін екі рет басыңыз",
        "access_granted" to "РҰҚСАТ БЕРІЛДІ",
        "done" to "Дайын",
        "max_brightness" to "Макс. жарықтық",
        "flip_card" to "Аудару",
        "top_up" to "Толықтыру",
        "delete" to "Жою",
        "close" to "Жабу",
        "save" to "Сақтау",
        "cancel" to "Бас тарту",
        "favorite" to "Таңдаулыға",

        "terminal_title" to "Интерактивті NFC терминалы",
        "terminal_intercom" to "Домофон / Құлып",
        "terminal_metro" to "Метро турникеті",
        "terminal_pos" to "POS Төлем",
        "terminal_tap_prompt" to "Картаны таңдап, терминалға тигізіңіз",
        "terminal_tap_action" to "Таңдалған картаны тигізу",
        "intercom_sound_desc" to "Электромагниттік құлып дыбысы",
        "metro_fare" to "Жол ақысын ұстау",
        "pos_success" to "Төлем мақұлданды",
        "receipt" to "Электрондық түбіртек",

        "guide_title" to "Ғаламдық қолжетімділік технологиялары",
        "guide_subtitle" to "NFC, RFID 125 кГц, FeliCa, Dallas, HomeKey туралы",

        "ai_title" to "WalletAll AI Көмекші",
        "ai_subtitle" to "Чиптер мен карталар бойынша сарапшы",
        "ai_photo_btn" to "Карта фотосын талдау",
        "ai_send_hint" to "Чиптер немесе карталар туралы сұраңыз..."
    )

    private val en = mapOf(
        "app_name" to "WalletAll",
        "tab_wallet" to "Wallet",
        "tab_scanner" to "Scanner",
        "tab_terminal" to "Terminal",
        "tab_guide" to "Tech Guide",
        "tab_ai" to "AI Assistant",

        "search_hint" to "Search cards & keys...",
        "deck_mode" to "Deck",
        "grid_mode" to "Grid",
        "add_card" to "Add",
        "catalog" to "Catalog",
        "custom_card" to "Custom Card",
        "scan_barcode" to "Scan Barcode",
        "read_nfc" to "Read NFC",
        "empty_cards" to "Your wallet is empty",
        "empty_cards_sub" to "Add a card from catalog or scan a real NFC chip",
        "add_first_card" to "Add your first card",

        "cat_all" to "All",
        "cat_intercom" to "Intercoms",
        "cat_transit" to "Transit",
        "cat_bank" to "Bank",
        "cat_loyalty" to "Loyalty",
        "cat_pass" to "Passes",
        "cat_fav" to "Favorites",

        "hold_near_reader" to "Hold Near Reader",
        "ready_to_tap" to "Ready to transmit NFC",
        "double_click_pay" to "Double-click to pay or unlock",
        "access_granted" to "ACCESS GRANTED",
        "done" to "Done",
        "max_brightness" to "Max Brightness",
        "flip_card" to "Flip Card",
        "top_up" to "Top Up",
        "delete" to "Delete",
        "close" to "Close",
        "save" to "Save",
        "cancel" to "Cancel",
        "favorite" to "Favorite",

        "terminal_title" to "Interactive NFC Terminal",
        "terminal_intercom" to "Intercom / Lock",
        "terminal_metro" to "Metro Gate",
        "terminal_pos" to "POS Payment",
        "terminal_tap_prompt" to "Select a card and tap on terminal",
        "terminal_tap_action" to "Simulate Contactless Tap",
        "intercom_sound_desc" to "Electromagnetic lock click sound",
        "metro_fare" to "Transit fare deducted",
        "pos_success" to "Payment Approved",
        "receipt" to "Digital Receipt",

        "guide_title" to "Global Access Tech Encyclopedia",
        "guide_subtitle" to "Comprehensive guide on NFC, 125 kHz RFID, FeliCa, Dallas & HomeKey",

        "ai_title" to "WalletAll AI Assistant",
        "ai_subtitle" to "Smart advisor for RFID chips, NFC keys and passes",
        "ai_photo_btn" to "Analyze Card Photo",
        "ai_send_hint" to "Ask about RFID chips, intercoms, or transit passes..."
    )

    private val es = mapOf(
        "app_name" to "WalletAll",
        "tab_wallet" to "Billetera",
        "tab_scanner" to "Escáner",
        "tab_terminal" to "Terminal",
        "tab_guide" to "Guía Técnica",
        "tab_ai" to "Asistente AI",

        "search_hint" to "Buscar tarjetas y llaves...",
        "deck_mode" to "Mazo",
        "grid_mode" to "Cuadrícula",
        "add_card" to "Añadir",
        "catalog" to "Catálogo",
        "custom_card" to "Tarjeta personalizada",
        "scan_barcode" to "Escanear código",
        "read_nfc" to "Leer NFC",
        "empty_cards" to "Tu billetera está vacía",
        "empty_cards_sub" to "Añade una tarjeta del catálogo o escanea un chip",
        "add_first_card" to "Añadir primera tarjeta",

        "cat_all" to "Todas",
        "cat_intercom" to "Porteros",
        "cat_transit" to "Transporte",
        "cat_bank" to "Banco",
        "cat_loyalty" to "Fidelidad",
        "cat_pass" to "Pases",
        "cat_fav" to "Favoritos",

        "hold_near_reader" to "Acercar al lector",
        "ready_to_tap" to "Listo para transmitir NFC",
        "double_click_pay" to "Doble clic para pagar",
        "access_granted" to "ACCESO PERMITIDO",
        "done" to "Listo",
        "max_brightness" to "Brillo Máximo",
        "flip_card" to "Girar",
        "top_up" to "Recargar",
        "delete" to "Eliminar",
        "close" to "Cerrar",
        "save" to "Guardar",
        "cancel" to "Cancelar",
        "favorite" to "Favorito",

        "terminal_title" to "Terminal NFC interactiva",
        "terminal_intercom" to "Intercomunicador / Cerradura",
        "terminal_metro" to "Torniquete de Metro",
        "terminal_pos" to "Pago POS",
        "terminal_tap_prompt" to "Selecciona una tarjeta y acércala",
        "terminal_tap_action" to "Acercar tarjeta seleccionada",
        "intercom_sound_desc" to "Sonido de cerradura electromagnética",
        "metro_fare" to "Tarifa deducida",
        "pos_success" to "Pago Aprobado",
        "receipt" to "Recibo Digital",

        "guide_title" to "Enciclopedia Global de Acceso",
        "guide_subtitle" to "Todo sobre NFC, 125 kHz RFID, FeliCa, Dallas y HomeKey",

        "ai_title" to "WalletAll Asistente AI",
        "ai_subtitle" to "Experto en chips RFID, llaves NFC y tarjetas",
        "ai_photo_btn" to "Analizar foto de tarjeta",
        "ai_send_hint" to "Pregunta sobre chips, porteros o tarjetas..."
    )

    private val it = mapOf(
        "app_name" to "WalletAll",
        "tab_wallet" to "Portafoglio",
        "tab_scanner" to "Scanner",
        "tab_terminal" to "Terminale",
        "tab_guide" to "Guida Tecnica",
        "tab_ai" to "Assistente AI",

        "search_hint" to "Cerca carte e chiavi...",
        "deck_mode" to "Mazzo",
        "grid_mode" to "Griglia",
        "add_card" to "Aggiungi",
        "catalog" to "Catalogo",
        "custom_card" to "Carta personalizzata",
        "scan_barcode" to "Scansiona codice",
        "read_nfc" to "Leggi NFC",
        "empty_cards" to "Il tuo portafoglio è vuoto",
        "empty_cards_sub" to "Aggiungi una carta dal catalogo o scansiona un chip",
        "add_first_card" to "Aggiungi prima carta",

        "cat_all" to "Tutte",
        "cat_intercom" to "Citofoni",
        "cat_transit" to "Trasporti",
        "cat_bank" to "Banca",
        "cat_loyalty" to "Fedeltà",
        "cat_pass" to "Biglietti",
        "cat_fav" to "Preferiti",

        "hold_near_reader" to "Avvicina al lettore",
        "ready_to_tap" to "Pronto per trasmettere NFC",
        "double_click_pay" to "Doppio clic per pagare",
        "access_granted" to "ACCESSO CONSENTITO",
        "done" to "Fatto",
        "max_brightness" to "Luminosità Massima",
        "flip_card" to "Capovolgi",
        "top_up" to "Ricarica",
        "delete" to "Elimina",
        "close" to "Chiudi",
        "save" to "Salva",
        "cancel" to "Annulla",
        "favorite" to "Preferito",

        "terminal_title" to "Terminale NFC Interattivo",
        "terminal_intercom" to "Citofono / Serratura",
        "terminal_metro" to "Tornello Metro",
        "terminal_pos" to "Pagamento POS",
        "terminal_tap_prompt" to "Seleziona una carta e avvicinala al terminale",
        "terminal_tap_action" to "Avvicina la carta selezionata",
        "intercom_sound_desc" to "Scatto della serratura elettromagnetica",
        "metro_fare" to "Tariffa detratta",
        "pos_success" to "Pagamento Approvato",
        "receipt" to "Scontrino Digitale",

        "guide_title" to "Enciclopedia Globale di Accesso",
        "guide_subtitle" to "Tutto su NFC, 125 kHz RFID, FeliCa, Dallas e HomeKey",

        "ai_title" to "WalletAll Assistente AI",
        "ai_subtitle" to "Esperto di chip RFID, chiavi NFC e carte",
        "ai_photo_btn" to "Analizza foto carta",
        "ai_send_hint" to "Chiedi info su chip, citofoni o carte..."
    )

    private val pt = mapOf(
        "app_name" to "WalletAll",
        "tab_wallet" to "Carteira",
        "tab_scanner" to "Leitor",
        "tab_terminal" to "Terminal",
        "tab_guide" to "Guia Técnico",
        "tab_ai" to "Assistente AI",

        "search_hint" to "Pesquisar cartões e chaves...",
        "deck_mode" to "Pilha",
        "grid_mode" to "Grelha",
        "add_card" to "Adicionar",
        "catalog" to "Catálogo",
        "custom_card" to "Cartão personalizado",
        "scan_barcode" to "Ler código de barras",
        "read_nfc" to "Ler NFC",
        "empty_cards" to "A sua carteira está vazia",
        "empty_cards_sub" to "Adicione um cartão do catálogo ou leia um chip",
        "add_first_card" to "Adicionar primeiro cartão",

        "cat_all" to "Todos",
        "cat_intercom" to "Interfones",
        "cat_transit" to "Transporte",
        "cat_bank" to "Banco",
        "cat_loyalty" to "Fidelidade",
        "cat_pass" to "Bilhetes",
        "cat_fav" to "Favoritos",

        "hold_near_reader" to "Aproxime do leitor",
        "ready_to_tap" to "Pronto para transmitir NFC",
        "double_click_pay" to "Duplo clique para pagar",
        "access_granted" to "ACESSO PERMITIDO",
        "done" to "Concluído",
        "max_brightness" to "Brilho Máximo",
        "flip_card" to "Virar",
        "top_up" to "Recarregar",
        "delete" to "Eliminar",
        "close" to "Fechar",
        "save" to "Guardar",
        "cancel" to "Cancelar",
        "favorite" to "Favorito",

        "terminal_title" to "Terminal NFC Interativo",
        "terminal_intercom" to "Interfone / Fechadura",
        "terminal_metro" to "Torniquete Metro",
        "terminal_pos" to "Pagamento POS",
        "terminal_tap_prompt" to "Selecione um cartão e encoste ao terminal",
        "terminal_tap_action" to "Encostar cartão selecionado",
        "intercom_sound_desc" to "Som de trinco eletromagnético",
        "metro_fare" to "Tarifa deduzida",
        "pos_success" to "Pagamento Aprovado",
        "receipt" to "Recibo Digital",

        "guide_title" to "Enciclopédia Global de Acesso",
        "guide_subtitle" to "Tudo sobre NFC, 125 kHz RFID, FeliCa, Dallas e HomeKey",

        "ai_title" to "WalletAll Assistente AI",
        "ai_subtitle" to "Especialista em chips RFID, chaves NFC e cartões",
        "ai_photo_btn" to "Analisar foto do cartão",
        "ai_send_hint" to "Pergunte sobre chips, interfones ou passes..."
    )
}
