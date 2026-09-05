package com.example.oneshotai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.ui.components.ComposerBar
import com.example.oneshotai.ui.components.MessageBubble
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.ScreenTab
import com.example.oneshotai.ui.viewmodel.UiState

@Composable
fun ChatScreen(
    state: UiState,
    onSendMessage: (String) -> Unit,
    onAddAttachment: (String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onToggleVoiceNote: () -> Unit,
    onToggleLiveVoice: () -> Unit,
    onDismissError: () -> Unit,
    onNavigateTab: (ScreenTab) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll on new messages
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val quickPrompts = if (state.isArabic) {
        listOf(
            "فحص سجلات النطاق 1shotcam.com",
            "صياغة تقرير تحديث أسبوعي للعملاء",
            "تحليل إعدادات أمان الترويسات HTTP",
            "عرض قائمة وكلاء الذكاء الاصطناعي"
        )
    } else {
        listOf(
            "Run DeepFind on 1shotcam.com",
            "Draft weekly client update",
            "Analyze HTTP security headers",
            "List specialized AI agents"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Error Banner
        AnimatedVisibility(visible = state.errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissError) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Active project indicator if set
        if (state.activeProject != null) {
            Surface(
                color = OneShotOrange.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, OneShotOrange.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.isArabic) "المشروع النشط: ${state.activeProject.name}" else "Active Project: ${state.activeProject.name}",
                        fontSize = 11.5.sp,
                        color = OneShotOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Messages list or Empty Hero
        if (state.messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(OneShotOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "OneShot AI",
                        tint = OneShotOrange,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "OneShot AI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (state.isArabic) "مساحة واحدة. كل النماذج. كل الوكلاء." else "One workspace. Every model. Every agent.",
                    fontSize = 13.5.sp,
                    color = OneShotOrange,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = if (state.isArabic)
                        "محادثاتك ومشاريعك ووكلاؤك ومهامك ومعرفة الشركة كلها تحت نظام OneShot."
                    else
                        "Your conversations, projects, agents, tasks and company knowledge — under the OneShot system.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
                    lineHeight = 18.sp
                )

                // Quick Prompt Chips
                Text(
                    text = if (state.isArabic) "اقتراحات سريعة" else "QUICK ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickPrompts.forEach { prompt ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (prompt.contains("DeepFind") || prompt.contains("1shotcam")) {
                                        onNavigateTab(ScreenTab.DEEPFIND)
                                    } else {
                                        onSendMessage(prompt)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = OneShotOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prompt,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.messages) { message ->
                    MessageBubble(
                        message = message,
                        isArabic = state.isArabic
                    )
                }

                if (state.isSending) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = OneShotOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isArabic) "جارٍ التفكير بواسطة ${state.currentProvider.displayName}…" else "Thinking with ${state.currentProvider.displayName}…",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Composer Input Bar
        ComposerBar(
            isArabic = state.isArabic,
            isSending = state.isSending,
            isRecordingVoiceNote = state.isRecordingVoiceNote,
            isListeningLive = state.isListeningLive,
            attachments = state.attachments,
            onSendMessage = onSendMessage,
            onAddAttachment = onAddAttachment,
            onRemoveAttachment = onRemoveAttachment,
            onToggleVoiceNote = onToggleVoiceNote,
            onToggleLiveVoice = onToggleLiveVoice
        )
    }
}
