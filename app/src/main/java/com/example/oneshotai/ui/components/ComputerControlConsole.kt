package com.example.oneshotai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.oneshotai.model.ComputerHost
import com.example.oneshotai.ui.theme.AccentGreen
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComputerControlConsole(
    state: UiState,
    onSelectHost: (ComputerHost) -> Unit,
    onMouseAction: (String, Int, Int) -> Unit,
    onKeyboardAction: (String, Boolean) -> Unit,
    onBashCommand: (String) -> Unit,
    onRecipeRun: (String, String) -> Unit,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Mouse/Screen, 1: Keyboard, 2: Terminal, 3: Recipes, 4: Logs
    var textInput by remember { mutableStateOf("") }
    var bashInput by remember { mutableStateOf("") }
    var hostMenuExpanded by remember { mutableStateOf(false) }

    val subTabs = if (state.isArabic) {
        listOf("الشاشة والماوس", "لوحة المفاتيح", "الطرفية", "الوصفات", "السجل")
    } else {
        listOf("Screen & Mouse", "Keyboard", "Terminal", "Recipes", "Live Log")
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OneShotOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (state.isArabic) "وحدة التحكم بالحاسوب" else "Computer Control Console",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen)
                                )
                                Text(
                                    text = state.selectedHost?.name ?: "Local Sandbox",
                                    fontSize = 11.5.sp,
                                    color = OneShotOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Host Switcher & Resolution Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { hostMenuExpanded = true }
                            ) {
                                Text(
                                    text = state.selectedHost?.name ?: "Select Host",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = hostMenuExpanded,
                                onDismissRequest = { hostMenuExpanded = false }
                            ) {
                                state.computerHosts.forEach { host ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(host.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${host.os} • ${host.ip} • ${host.resolution}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            onSelectHost(host)
                                            hostMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${state.selectedHost?.resolution ?: "1920x1080"} • ${state.selectedHost?.ip ?: "127.0.0.1"}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sub Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedSubTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = OneShotOrange,
                    divider = {}
                ) {
                    subTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSubTab == index,
                            onClick = { selectedSubTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Contents
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedSubTab) {
                        0 -> {
                            // 0: Screen & Mouse
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Simulated Screen Canvas
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0F1215),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3238)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures { offset ->
                                                val x = (offset.x * 2.5).toInt()
                                                val y = (offset.y * 2.5).toInt()
                                                onMouseAction("click", x, y)
                                            }
                                        }
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        // Desktop Titlebar
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF1B1E22))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                                            }
                                            Text(
                                                text = state.activeDesktopWindow,
                                                fontSize = 11.sp,
                                                color = Color.LightGray,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "Active",
                                                fontSize = 9.sp,
                                                color = AccentGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Virtual desktop content representation
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(top = 30.dp, start = 12.dp, end = 12.dp, bottom = 12.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Terminal,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tap anywhere on this screen to move cursor & click",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                            Text(
                                                text = "Cursor: (${state.cursorPosition.first}, ${state.cursorPosition.second})",
                                                fontSize = 11.sp,
                                                color = OneShotOrange,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Virtual crosshair indicator
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 120.dp, y = 80.dp)
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(OneShotOrange.copy(alpha = 0.3f))
                                                .border(1.dp, OneShotOrange, CircleShape)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Mouse Actions
                                Text(
                                    text = if (state.isArabic) "إجراءات الفأرة السريعة" else "MOUSE COMMANDS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onMouseAction("click", state.cursorPosition.first, state.cursorPosition.second) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (state.isArabic) "نقرة يسار" else "Left Click", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { onMouseAction("double_click", state.cursorPosition.first, state.cursorPosition.second) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (state.isArabic) "نقرة مزدوجة" else "Double Click", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { onMouseAction("right_click", state.cursorPosition.first, state.cursorPosition.second) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (state.isArabic) "نقرة يمين" else "Right Click", fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onMouseAction("scroll_up", state.cursorPosition.first, state.cursorPosition.second) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (state.isArabic) "تمرير لأعلى" else "Scroll Up", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { onMouseAction("scroll_down", state.cursorPosition.first, state.cursorPosition.second) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (state.isArabic) "تمرير لأسفل" else "Scroll Down", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        1 -> {
                            // 1: Keyboard & Shortcuts
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    label = { Text(if (state.isArabic) "اكتب نصًا للإرسال إلى النافذة النشطة" else "Type string to send to active window") },
                                    placeholder = { Text("e.g. git checkout -b feat/computer-control") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                if (textInput.isNotBlank()) {
                                                    onKeyboardAction(textInput, false)
                                                    textInput = ""
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Send", tint = OneShotOrange)
                                        }
                                    }
                                )

                                Text(
                                    text = if (state.isArabic) "اختصارات لوحة المفاتيح الشائعة" else "KEYBOARD SHORTCUTS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )

                                val shortcuts = listOf("⌘ + Space", "Enter", "Ctrl + C", "Ctrl + V", "Esc", "Tab", "Alt + Tab", "⌘ + W", "⌘ + S")

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(shortcuts) { shortcut ->
                                        SuggestionChip(
                                            onClick = { onKeyboardAction(shortcut, true) },
                                            label = { Text(shortcut, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (state.isArabic) "نصيحة الأتمتة:" else "Automation Tip:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OneShotOrange
                                        )
                                        Text(
                                            text = if (state.isArabic)
                                                "يتم إرسال المفاتيح مباشرة إلى عملية النظام المستهدفة عبر جسر التحكم، مع الحفاظ على عزل الحاوية."
                                            else
                                                "Keystrokes are injected directly into the target OS display server via the secure outbound WebSocket bridge.",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // 2: Terminal & Shell
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Terminal window display
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0C0E11),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282C32)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(state.terminalOutput) { line ->
                                            Text(
                                                text = line,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = if (line.startsWith("oneshot@")) Color(0xFF56B6C2)
                                                else if (line.contains("error", ignoreCase = true)) Color(0xFFE06C75)
                                                else Color(0xFFABB2BF),
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Preset commands
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    val presets = listOf("uptime", "whoami", "git status", "docker ps", "ls -la")
                                    items(presets) { cmd ->
                                        AssistChip(
                                            onClick = { onBashCommand(cmd) },
                                            label = { Text(cmd, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace) }
                                        )
                                    }
                                }

                                // Command input
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = bashInput,
                                        onValueChange = { bashInput = it },
                                        placeholder = { Text("e.g. npm run build or docker logs", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (bashInput.isNotBlank()) {
                                                onBashCommand(bashInput)
                                                bashInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OneShotOrange)
                                    ) {
                                        Text(if (state.isArabic) "تشغيل" else "Exec")
                                    }
                                }
                            }
                        }

                        3 -> {
                            // 3: Recipes
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val recipes = listOf(
                                    Triple(
                                        "Audit System Telemetry & Processes",
                                        "Collects CPU load, memory usage, open ports, and Docker containers across connected nodes.",
                                        "audit_system"
                                    ),
                                    Triple(
                                        "Launch Headless Browser & Screenshot",
                                        "Opens Chromium, navigates to target URL, evaluates DOM readiness, and captures full-page PNG.",
                                        "browser_screenshot"
                                    ),
                                    Triple(
                                        "Clean Temporary Build Artifacts",
                                        "Scans disk for node_modules/.cache, Gradle daemon cache, and orphaned container volumes.",
                                        "clean_cache"
                                    ),
                                    Triple(
                                        "Lock Screen & Suspend Background Bridges",
                                        "Instantly locks active screen session and suspends idle inbound network tunnels.",
                                        "lock_session"
                                    )
                                )

                                items(recipes) { (name, desc, code) ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(desc, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Button(
                                                onClick = { onRecipeRun(name, code) },
                                                colors = ButtonDefaults.buttonColors(containerColor = OneShotOrange),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(if (state.isArabic) "تنفيذ" else "Run", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        4 -> {
                            // 4: Live Logs
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (state.isArabic) "سجل الأحداث المباشر (${state.computerLogs.size})" else "Live Event Stream (${state.computerLogs.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(onClick = onClearLogs) {
                                        Text(if (state.isArabic) "مسح السجل" else "Clear Logs", fontSize = 11.sp)
                                    }
                                }

                                if (state.computerLogs.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (state.isArabic) "لا توجد أحداث مسجلة بعد." else "No events logged yet.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(state.computerLogs) { log ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "[${log.actionType.uppercase()}] ${log.parameters}",
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = OneShotOrange
                                                        )
                                                        Text(
                                                            text = log.result,
                                                            fontSize = 10.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    Text(
                                                        text = timeFormat.format(Date(log.timestamp)),
                                                        fontSize = 9.sp,
                                                        color = AccentGreen
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
