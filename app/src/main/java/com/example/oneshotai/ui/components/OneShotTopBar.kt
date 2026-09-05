package com.example.oneshotai.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.R
import com.example.oneshotai.model.ModelProvider
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneShotTopBar(
    state: UiState,
    onModelSelected: (ModelProvider) -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenBrandDialog: () -> Unit = {}
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Menu & Brand Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Brand Logo (Clickable to showcase both Dark & White logos)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .clickable { onOpenBrandDialog() }
                        .background(OneShotOrange.copy(alpha = 0.15f))
                        .border(1.dp, OneShotOrange.copy(alpha = 0.6f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = state.activeLogoRes),
                        contentDescription = "OneShot AI Brand Logo",
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier.clickable { onOpenBrandDialog() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "OneShot AI",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Brand Verified",
                            tint = OneShotOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = if (state.isArabic) "Gemini 3.8 Flash • مساحة العمل" else "Gemini 3.8 Flash",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = OneShotOrange
                    )
                }
            }

            // Right: Model Selector & Quick Toggles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Model Dropdown Chip
                Box {
                    AssistChip(
                        onClick = { modelMenuExpanded = true },
                        label = {
                            Text(
                                text = state.currentProvider.displayName.take(15),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Model",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false }
                    ) {
                        ModelProvider.values().forEach { provider ->
                            val isGemini38 = provider == ModelProvider.GEMINI_3_8_FLASH
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (state.currentProvider == provider) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = OneShotOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = provider.displayName,
                                            fontSize = 13.5.sp,
                                            fontWeight = if (isGemini38) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isGemini38) OneShotOrange else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isGemini38) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = OneShotOrange
                                            ) {
                                                Text(
                                                    text = "LATEST",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = androidx.compose.ui.graphics.Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    onModelSelected(provider)
                                    modelMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Language Toggle (EN / AR)
                IconButton(onClick = onToggleLanguage) {
                    Text(
                        text = if (state.isArabic) "EN" else "عربي",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Theme Toggle
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (state.isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
