package com.example.oneshotai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.model.Project
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.ScreenTab
import com.example.oneshotai.ui.viewmodel.UiState

@Composable
fun ProjectsScreen(
    state: UiState,
    onCreateProject: (name: String, description: String, model: String, instructions: String) -> Unit,
    onDeleteProject: (Project) -> Unit,
    onSetActiveProject: (Project?) -> Unit,
    onNavigateTab: (ScreenTab) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newModel by remember { mutableStateOf("⚡ Auto Router") }
    var newInstructions by remember { mutableStateOf("") }

    val models = listOf("⚡ Auto Router", "Gemini 3.5 Flash", "Claude 3.5 Sonnet", "OpenAI GPT-5", "Llama 3 Local")

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
                    text = if (state.isArabic) "المشاريع" else "Projects",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (state.isArabic) "مساحات عمل مخصصة بتعليمات ونماذج محددة" else "Dedicated workspaces with custom prompts and models",
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
                Text(text = if (state.isArabic) "مشروع جديد" else "New", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isArabic) "لا توجد مشاريع بعد. أنشئ مشروعك الأول." else "No projects yet. Create your first project.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.projects) { project ->
                    val isActive = state.activeProject?.id == project.id

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActive) OneShotOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = OneShotOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = project.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(project.model, fontSize = 11.sp) }
                                )
                            }

                            if (project.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = project.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (project.instructions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = project.instructions,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp),
                                        maxLines = 2
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { onDeleteProject(project) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedButton(
                                    onClick = {
                                        if (isActive) {
                                            onSetActiveProject(null)
                                        } else {
                                            onSetActiveProject(project)
                                            onNavigateTab(ScreenTab.CHAT)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (isActive) OneShotOrange else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = if (isActive) {
                                            if (state.isArabic) "إلغاء التنشيط" else "Deactivate"
                                        } else {
                                            if (state.isArabic) "فتح في المحادثة" else "Open in Chat"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Project Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (state.isArabic) "إنشاء مشروع جديد" else "Create New Project",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(if (state.isArabic) "اسم المشروع" else "Project Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text(if (state.isArabic) "الوصف" else "Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newInstructions,
                        onValueChange = { newInstructions = it },
                        label = { Text(if (state.isArabic) "تعليمات النظام (System Prompt)" else "System Instructions") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onCreateProject(newName, newDesc, newModel, newInstructions)
                            newName = ""
                            newDesc = ""
                            newInstructions = ""
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
