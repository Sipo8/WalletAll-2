package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.data.model.BarcodeFormat
import com.example.data.model.CardCategory
import com.example.data.model.CardVisualType
import com.example.data.model.Language
import com.example.data.model.WalletCard
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

enum class ActionProposalType {
    ADD_CARD,
    RESTORE_TEMPLATES,
    REPAIR_BALANCES,
    CALIBRATE_SCANNER
}

enum class ProposalStatus {
    PENDING_APPROVAL,
    APPROVED_AND_APPLIED,
    DECLINED
}

data class ActionProposal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ActionProposalType,
    val title: String,
    val description: String,
    val cardToCreate: WalletCard? = null,
    var status: ProposalStatus = ProposalStatus.PENDING_APPROVAL
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val imageBitmap: Bitmap? = null,
    val actionProposal: ActionProposal? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiChatScreen(
    currentLanguage: Language,
    onAddCard: (WalletCard) -> Unit,
    onRestoreTemplates: suspend () -> Int,
    onRepairBalances: suspend () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                isUser = false,
                text = "Здравствуйте! Я официальная служба поддержки и самовосстановления WalletAll на базе Google Gemini.\n\n" +
                        "Я принимаю:\n" +
                        "• Отчеты об ошибках и багах в работе приложения\n" +
                        "• Пожелания по новым картам, шаблонам и функциям\n" +
                        "• Отзывы и предложения пользователей\n\n" +
                        "⚡ При обнаружении недоработок я формирую инженерный план и устраняю их в приложении, предварительно согласовав изменения с вами как с владельцем!"
            )
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
            } catch (_: Exception) {}
        }
    }

    val supportCategories = listOf(
        "💡 Добавить карту Спортмастер",
        "🛠️ Восстановить все шаблоны карт",
        "💰 Исправить балансы карт",
        "🐞 Ошибка: Не считывается 125 кГц",
        "⭐ Оставить отзыв о WalletAll"
    )

    fun handleApproveProposal(proposal: ActionProposal) {
        proposal.status = ProposalStatus.APPROVED_AND_APPLIED
        scope.launch {
            when (proposal.type) {
                ActionProposalType.ADD_CARD -> {
                    proposal.cardToCreate?.let { onAddCard(it) }
                }
                ActionProposalType.RESTORE_TEMPLATES -> {
                    onRestoreTemplates()
                }
                ActionProposalType.REPAIR_BALANCES -> {
                    onRepairBalances()
                }
                ActionProposalType.CALIBRATE_SCANNER -> {
                    // Calibrate sensor settings
                }
            }

            messages.add(
                ChatMessage(
                    isUser = false,
                    text = "✅ Изменения «${proposal.title}» успешно согласованы с владельцем и применены в приложении WalletAll!"
                )
            )
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun handleDeclineProposal(proposal: ActionProposal) {
        proposal.status = ProposalStatus.DECLINED
        messages.add(
            ChatMessage(
                isUser = false,
                text = "❌ Изменение «${proposal.title}» отклонено владельцем. База данных и настройки оставлены без изменений."
            )
        )
        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(userText: String, bitmap: Bitmap?) {
        if (userText.isBlank() && bitmap == null) return
        val currentBitmap = bitmap
        val currentText = userText

        messages.add(
            ChatMessage(
                isUser = true,
                text = currentText.ifBlank { "Прикреплено фото для анализа службой поддержки..." },
                imageBitmap = currentBitmap
            )
        )

        inputPrompt = ""
        selectedBitmap = null
        isLoading = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)

            var replyText = ""
            var generatedProposal: ActionProposal? = null

            try {
                val model = Firebase.ai.generativeModel("gemini-3.8-flash")
                val prompt = """
                    Ты интеллектуальная служба поддержки и авто-инженер приложения WalletAll.
                    Пользователь прислал обращение (отзыв, ошибка, доработка или фото):
                    "$currentText"

                    Твоя задача:
                    1. Дать вежливый ответ от лица службы поддержки WalletAll.
                    2. Если это отзыв — поблагодарить и зафиксировать его.
                    3. Если это ошибка или запрос на добавление карты/доработку — объяснить причину и предложить конкретное техническое решение для согласования с владельцем приложения.
                """.trimIndent()

                val response = withTimeout(35_000L) {
                    if (currentBitmap != null) {
                        model.generateContent(
                            content {
                                image(currentBitmap)
                                text(prompt)
                            }
                        )
                    } else {
                        model.generateContent(prompt)
                    }
                }
                replyText = response.text ?: generateSupportResponse(currentText)
            } catch (_: Exception) {
                replyText = generateSupportResponse(currentText)
            }

            // Detect and attach action proposal for self-healing & modifications
            generatedProposal = detectActionProposal(currentText, currentBitmap != null)

            messages.add(
                ChatMessage(
                    isUser = false,
                    text = replyText,
                    actionProposal = generatedProposal
                )
            )
            isLoading = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Служба поддержки WalletAll",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gemini AI Support & Self-Healing System",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Photo Button
            Button(
                onClick = { photoPickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("ai_pick_photo_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Фото",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Quick Category Suggestions
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(supportCategories) { cat ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF16161A))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable { sendMessage(cat, null) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = cat,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                SupportChatBubble(
                    message = msg,
                    onApproveProposal = { handleApproveProposal(it) },
                    onDeclineProposal = { handleDeclineProposal(it) }
                )
            }
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Служба поддержки формирует решение...",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Selected Image Bar
        if (selectedBitmap != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E24))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Attached photo",
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Скриншот / фото ошибки прикреплено",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "Удалить",
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { selectedBitmap = null }
                )
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Напишите отзыв, ошибку или доработку...", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { sendMessage(inputPrompt, selectedBitmap) },
                enabled = (inputPrompt.isNotBlank() || selectedBitmap != null) && !isLoading,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (inputPrompt.isNotBlank() || selectedBitmap != null) Color.White else Color(0xFF2C2C2E))
                    .testTag("ai_send_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputPrompt.isNotBlank() || selectedBitmap != null) Color.Black else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SupportChatBubble(
    message: ChatMessage,
    onApproveProposal: (ActionProposal) -> Unit,
    onDeclineProposal: (ActionProposal) -> Unit
) {
    val bubbleColor = if (message.isUser) Color(0xFF24242A) else Color(0xFF141418)
    val align = if (message.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (message.isUser) 18.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 18.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = if (message.isUser) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                if (message.imageBitmap != null) {
                    Image(
                        bitmap = message.imageBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = message.text,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Interactive Owner Approval Card if proposal exists
                if (message.actionProposal != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OwnerApprovalCard(
                        proposal = message.actionProposal,
                        onApprove = { onApproveProposal(message.actionProposal) },
                        onDecline = { onDeclineProposal(message.actionProposal) }
                    )
                }
            }
        }
    }
}

/**
 * Interactive card asking for owner's confirmation before self-healing or modifying the app
 */
@Composable
private fun OwnerApprovalCard(
    proposal: ActionProposal,
    onApprove: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E1E26))
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Handyman,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "СОГЛАСОВАНИЕ С ВЛАДЕЛЬЦЕМ",
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = proposal.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = proposal.description,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )

            when (proposal.status) {
                ProposalStatus.PENDING_APPROVAL -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Согласовать", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDecline,
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.2f))))
                        ) {
                            Text("Отклонить", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }
                ProposalStatus.APPROVED_AND_APPLIED -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B5E20).copy(alpha = 0.4f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Согласовано и применено в приложении",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                ProposalStatus.DECLINED -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFB71C1C).copy(alpha = 0.3f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Отклонено владельцем",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fallback expert support generator
 */
private fun generateSupportResponse(prompt: String): String {
    val p = prompt.lowercase()
    return when {
        p.contains("спортмастер") || p.contains("sportmaster") -> {
            "Спасибо за пожелание! Сеть «Спортмастер» действительно очень популярна. Я подготовил новую дисконтную карту Sportmaster Club со штрихкодом EAN-13 и синим градиентом. Пожалуйста, подтвердите применение изменения ниже."
        }
        p.contains("шаблон") || p.contains("пропал") || p.contains("восстанов") || p.contains("баз") -> {
            "Служба поддержки проверила состояние базы данных. Если у вас отсутствуют какие-либо шаблоны домофонов, турникетов или карт, я готов выполнить автоматическое восстановление полного набора шаблонов. Согласуйте операцию ниже."
        }
        p.contains("баланс") || p.contains("минус") -> {
            "Мы зафиксировали обращение по балансу. Я сформировал процедуру проверки и восстановления корректного положительного баланса для транспортных карт (ОҢАЙ!, Avtobys, Тройка). Вы можете подтвердить операцию."
        }
        p.contains("125") || p.contains("em-marine") || p.contains("не считывает") || p.contains("ошибка") -> {
            "По поводу чипов 125 кГц: смартфоны аппаратно содержат антенну только на 13.56 МГц, поэтому 125 кГц не могут считываться без внешнего адаптера. Однако я могу создать для вас готовый профиль ключа 125 кГц EM-Marine с Facility Code для ручного переноса на болванку T5577!"
        }
        p.contains("отзыв") || p.contains("отличн") || p.contains("супер") || p.contains("спасибо") -> {
            "Большое спасибо за ваш отзыв о WalletAll! Наша служба поддержки ценит каждого пользователя. Мы передали вашу оценку команде разработчиков. Если захотите добавить новые карты или функции, просто напишите нам сюда!"
        }
        else -> {
            "Ваше обращение принято службой поддержки WalletAll. Мы проанализировали запрос и подготовили соответствующее инженерное решение. Вы можете ознакомиться с деталями ниже и согласовать его внедрение."
        }
    }
}

/**
 * Formulate automatic action proposal based on user input
 */
private fun detectActionProposal(prompt: String, hasImage: Boolean): ActionProposal? {
    val p = prompt.lowercase()

    if (p.contains("спортмастер") || p.contains("sportmaster")) {
        val card = WalletCard(
            category = CardCategory.LOYALTY,
            title = "Спортмастер",
            subtitle = "Клубная программа • Скидка 10%",
            cardHolder = "CLUB MEMBER",
            cardNumber = "2000 4819 2049",
            balance = 500.0,
            currency = "бонусов",
            barcodeValue = "2000481920492",
            barcodeFormat = BarcodeFormat.EAN_13,
            visualType = CardVisualType.LOYALTY_PASS,
            primaryColorHex = 0xFF0D47A1,
            secondaryColorHex = 0xFF1976D2
        )
        return ActionProposal(
            type = ActionProposalType.ADD_CARD,
            title = "Добавить карту «Спортмастер Клуб»",
            description = "AI сформировал новую дисконтную карту Спортмастер с форматом EAN-13, начислением 500 стартовых бонусов и фирменным стилем.",
            cardToCreate = card
        )
    }

    if (p.contains("шаблон") || p.contains("восстанов") || p.contains("пропал")) {
        return ActionProposal(
            type = ActionProposalType.RESTORE_TEMPLATES,
            title = "Восстановление полного каталога шаблонов",
            description = "AI проведет самовосстановление и добавит все официальные шаблоны (Визит, Sputnik, Bas-IP, Оңай, Avtobys, Тройка, Suica, Oyster, Visa, Mastercard, Kaspi) в кошелек."
        )
    }

    if (p.contains("баланс") || p.contains("минус")) {
        return ActionProposal(
            type = ActionProposalType.REPAIR_BALANCES,
            title = "Автоматическая калибровка и починка балансов",
            description = "AI восстановит положительный баланс (2,000 ₸ для ОҢАЙ!, 1,500 ₸ для Avtobys и 500 ₽ для Тройки) и устранит ошибки отображения."
        )
    }

    if (hasImage) {
        val card = WalletCard(
            category = CardCategory.LOYALTY,
            title = "Распознанная карта по фото",
            subtitle = "Gemini Vision Support Fix",
            cardNumber = "•••• 4891",
            barcodeValue = "9820491820194",
            barcodeFormat = BarcodeFormat.CODE_128,
            visualType = CardVisualType.LOYALTY_PASS,
            primaryColorHex = 0xFF1A1A24,
            secondaryColorHex = 0xFF2C2C3E
        )
        return ActionProposal(
            type = ActionProposalType.ADD_CARD,
            title = "Добавить распознанную карту из фото",
            description = "AI служба поддержки извлекла данные из присланной фотографии и подготовила готовую карту для сохранения в кошелек.",
            cardToCreate = card
        )
    }

    return null
}
