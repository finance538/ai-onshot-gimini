package com.example.oneshotai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.model.TaskItem
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.UiState

@Composable
fun TasksScreen(
    state: UiState,
    onCreateTask: (title: String, type: String) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("Scheduled") }
    var selectedFilter by remember { mutableStateOf("All") }

    val taskTypes = listOf("Immediate", "Scheduled", "Recurring", "Watch")
    val filterTypes = listOf("All") + taskTypes

    val filteredTasks = state.tasks.filter {
        selectedFilter == "All" || it.type == selectedFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (state.isArabic) "مهام الأتمتة" else "Tasks & Automations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (state.isArabic) "تنفيذ تلقائي للمهام الفورية والمجدولة والدورية" else "Autonomous execution for immediate, scheduled and recurring jobs",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OneShotOrange,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (state.isArabic) "مهمة جديدة" else "New", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterTypes) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OneShotOrange.copy(alpha = 0.2f),
                        selectedLabelColor = OneShotOrange
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isArabic) "لا توجد مهام مطابقة." else "No matching tasks found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTasks) { task ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = task.type,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OneShotOrange,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = if (task.enabled) {
                                            if (state.isArabic) "مفعّل" else "Active"
                                        } else {
                                            if (state.isArabic) "متوقف" else "Paused"
                                        },
                                        fontSize = 11.sp,
                                        color = if (task.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = task.enabled,
                                    onCheckedChange = { onToggleTask(task) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                        checkedTrackColor = OneShotOrange
                                    )
                                )

                                IconButton(onClick = { onDeleteTask(task) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (state.isArabic) "إنشاء مهمة جديدة" else "Create Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(if (state.isArabic) "عنوان المهمة" else "Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (state.isArabic) "نوع المهمة" else "Task Type",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(taskTypes) { type ->
                            FilterChip(
                                selected = newType == type,
                                onClick = { newType = type },
                                label = { Text(type, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OneShotOrange,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onCreateTask(newTitle, newType)
                            newTitle = ""
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OneShotOrange)
                ) {
                    Text(if (state.isArabic) "إنشاء" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(if (state.isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}
