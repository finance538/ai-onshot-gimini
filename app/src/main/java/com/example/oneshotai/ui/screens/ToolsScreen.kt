package com.example.oneshotai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.R
import com.example.oneshotai.model.ComputerHost
import com.example.oneshotai.model.ToolItem
import com.example.oneshotai.ui.components.ComputerControlConsole
import com.example.oneshotai.ui.theme.AccentGreen
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.UiState

@Composable
fun ToolsScreen(
    state: UiState,
    onToggleTool: (String) -> Unit,
    onOpenConsole: (ToolItem) -> Unit,
    onCloseConsole: () -> Unit,
    onSelectHost: (ComputerHost) -> Unit,
    onMouseAction: (String, Int, Int) -> Unit,
    onKeyboardAction: (String, Boolean) -> Unit,
    onBashCommand: (String) -> Unit,
    onRecipeRun: (String, String) -> Unit,
    onClearLogs: () -> Unit,
    onOpenBrandDialog: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = if (state.isArabic) {
        listOf("الكل", "التحكم بالنظام والأتمتة", "أدوات المطورين", "السحابة والإنتاجية", "الاستخبارات والبحث")
    } else {
        listOf("All", "OS & Automation", "Developer", "Cloud & Workspace", "Intelligence")
    }

    val filteredTools = state.toolsList.filter { tool ->
        when (selectedCategory) {
            "All", "الكل" -> true
            "OS & Automation", "التحكم بالنظام والأتمتة" -> tool.category == "OS & Automation"
            "Developer", "أدوات المطورين" -> tool.category == "Developer"
            "Cloud & Workspace", "السحابة والإنتاجية" -> tool.category == "Cloud & Workspace"
            "Intelligence", "الاستخبارات والبحث" -> tool.category == "Intelligence"
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = if (state.isArabic) "الأدوات والتحكم بالنظام" else "Tools & System Control",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (state.isArabic)
                    "التحكم بسطح المكتب، محاكاة المتصفح، الطرفية، وموصلات السحابة والذكاء الاصطناعي"
                else
                    "OS computer control, browser agents, isolated terminal sandboxes, and cloud connectors",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Category Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OneShotOrange.copy(alpha = 0.2f),
                        selectedLabelColor = OneShotOrange
                    )
                )
            }
        }

        // Tools List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Brand & AI Model Hero Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OneShotOrange.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenBrandDialog() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Thumbnails of both dark and white logos
                            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, OneShotOrange, CircleShape)
                                        .background(androidx.compose.ui.graphics.Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_oneshot_logo_dark),
                                        contentDescription = "Dark Logo",
                                        modifier = Modifier.size(32.dp).clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, OneShotOrange, CircleShape)
                                        .background(androidx.compose.ui.graphics.Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_oneshot_logo_light),
                                        contentDescription = "Light Logo",
                                        modifier = Modifier.size(32.dp).clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (state.isArabic) "شعارات OneShot الرسمية" else "OneShot Brand Logos",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = OneShotOrange
                                    ) {
                                        Text(
                                            text = "Gemini 3.8 Flash",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (state.isArabic) "استعرض النسخة الداكنة والنسخة الفاتحة للشعار وتبديلها" else "Official Black & White logos • Tap to view & switch",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Brand Dialog",
                            tint = OneShotOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            items(filteredTools) { tool ->
                val isControlTool = tool.id == "t_comp" || tool.id == "t_browser" || tool.id == "t_terminal" || tool.id == "t_vision"

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isControlTool && tool.connected) OneShotOrange.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    tonalElevation = if (isControlTool) 3.dp else 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (tool.connected) OneShotOrange.copy(alpha = 0.18f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when (tool.icon) {
                                        "computer" -> Icons.Default.Computer
                                        "browser" -> Icons.Default.Language
                                        "terminal" -> Icons.Default.Terminal
                                        "vision" -> Icons.Default.Visibility
                                        "database" -> Icons.Default.Storage
                                        "mail" -> Icons.Default.Email
                                        "calendar" -> Icons.Default.CalendarMonth
                                        "drive" -> Icons.Default.CloudQueue
                                        "github" -> Icons.Default.DataObject
                                        "slack" -> Icons.Default.Chat
                                        "hardware" -> Icons.Default.Memory
                                        else -> Icons.Default.Search
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (tool.connected) OneShotOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = tool.name,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (tool.connected) AccentGreen.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = if (tool.connected) {
                                                    if (state.isArabic) "نشط" else "Active"
                                                } else {
                                                    if (state.isArabic) "متاح" else "Ready"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (tool.connected) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = tool.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tool.hasInteractiveConsole) {
                                Button(
                                    onClick = {
                                        if (tool.id == "t_brand") {
                                            onOpenBrandDialog()
                                        } else {
                                            onOpenConsole(tool)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OneShotOrange,
                                        contentColor = androidx.compose.ui.graphics.Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (state.isArabic) "فتح وحدة التحكم" else "Open Console",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            OutlinedButton(
                                onClick = { onToggleTool(tool.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (tool.connected) MaterialTheme.colorScheme.onSurfaceVariant else OneShotOrange
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (tool.connected) {
                                        if (state.isArabic) "إيقاف" else "Disconnect"
                                    } else {
                                        if (state.isArabic) "ربط" else "Connect"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Computer Control Console Sheet
    if (state.selectedToolForConsole != null) {
        ComputerControlConsole(
            state = state,
            onSelectHost = onSelectHost,
            onMouseAction = onMouseAction,
            onKeyboardAction = onKeyboardAction,
            onBashCommand = onBashCommand,
            onRecipeRun = onRecipeRun,
            onClearLogs = onClearLogs,
            onDismiss = onCloseConsole
        )
    }
}
