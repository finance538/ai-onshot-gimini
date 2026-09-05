package com.example.oneshotai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.ui.theme.AccentGreen
import com.example.oneshotai.ui.theme.AccentRed
import com.example.oneshotai.ui.theme.OneShotOrange

@Composable
fun ComposerBar(
    isArabic: Boolean,
    isSending: Boolean,
    isRecordingVoiceNote: Boolean,
    isListeningLive: Boolean,
    attachments: List<String>,
    onSendMessage: (String) -> Unit,
    onAddAttachment: (String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onToggleVoiceNote: () -> Unit,
    onToggleLiveVoice: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Attachment chips
            AnimatedVisibility(visible = attachments.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(attachments) { fileName ->
                        InputChip(
                            selected = true,
                            onClick = {},
                            label = { Text(fileName, fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { onRemoveAttachment(fileName) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Status Banner for Recording or Listening
            AnimatedVisibility(visible = isRecordingVoiceNote || isListeningLive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isRecordingVoiceNote) AccentRed.copy(alpha = 0.15f)
                            else AccentGreen.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isRecordingVoiceNote) AccentRed else AccentGreen)
                        )
                        Text(
                            text = if (isRecordingVoiceNote) {
                                if (isArabic) "جارٍ تسجيل الرسالة الصوتية…" else "Recording voice note…"
                            } else {
                                if (isArabic) "أستمع إليك مباشرة…" else "Listening live to speech…"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isRecordingVoiceNote) AccentRed else AccentGreen
                        )
                    }

                    TextButton(
                        onClick = {
                            if (isRecordingVoiceNote) onToggleVoiceNote() else onToggleLiveVoice()
                        }
                    ) {
                        Text(
                            text = if (isArabic) "إيقاف" else "Stop",
                            fontSize = 12.sp,
                            color = if (isRecordingVoiceNote) AccentRed else AccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Attach file button
                IconButton(
                    onClick = {
                        onAddAttachment("document_${attachments.size + 1}.pdf")
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AttachFile,
                        contentDescription = "Attach Document",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Voice Note toggle button
                IconButton(
                    onClick = onToggleVoiceNote,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isRecordingVoiceNote) Icons.Filled.Mic else Icons.Outlined.Mic,
                        contentDescription = "Voice Note",
                        tint = if (isRecordingVoiceNote) AccentRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Live speech recognition toggle
                IconButton(
                    onClick = onToggleLiveVoice,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isListeningLive) Icons.Filled.Hearing else Icons.Outlined.Hearing,
                        contentDescription = "Live Speech",
                        tint = if (isListeningLive) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Text field
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = if (isArabic) "اكتب رسالتك إلى OneShot AI…" else "Message OneShot AI…",
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OneShotOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    maxLines = 4
                )

                // Send FAB
                FloatingActionButton(
                    onClick = {
                        if (textInput.isNotBlank() && !isSending) {
                            val msg = textInput
                            textInput = ""
                            onSendMessage(msg)
                        }
                    },
                    containerColor = OneShotOrange,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("send_button"),
                    shape = CircleShape
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
