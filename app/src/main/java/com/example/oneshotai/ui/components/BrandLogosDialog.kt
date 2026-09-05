package com.example.oneshotai.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.oneshotai.R
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.UiState

@Composable
fun BrandLogosDialog(
    state: UiState,
    onDismiss: () -> Unit,
    onSelectLogo: (String) -> Unit, // "auto", "dark", "light"
    onToggleTheme: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(OneShotOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = state.activeLogoRes),
                                contentDescription = "OneShot Logo",
                                modifier = Modifier.size(36.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column {
                            Text(
                                text = if (state.isArabic) "هوية وشعارات OneShot AI" else "OneShot Brand Logos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.isArabic) "النسخة الداكنة والنسخة الفاتحة • محرك Gemini 3.8 Flash" else "Official Dark & Light Editions • Gemini 3.8 Flash",
                                fontSize = 11.5.sp,
                                color = OneShotOrange
                            )
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

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Scrollable Content showcasing both logos
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Status Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Text(
                                    text = if (state.isArabic) "الموديل النشط: Gemini 3.8 Flash" else "Active AI Model: Gemini 3.8 Flash",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = onToggleTheme,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = OneShotOrange
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.isDarkTheme) "Switch to Light" else "Switch to Dark",
                                    fontSize = 11.5.sp,
                                    color = OneShotOrange
                                )
                            }
                        }
                    }

                    // Card 1: The Black OneShot Logo (Dark Edition)
                    LogoEditionCard(
                        title = if (state.isArabic) "الشعار الداكن (الأسود)" else "The Black OneShot Logo",
                        subtitle = if (state.isArabic) "النسخة الداكنة الرسمية • خلفية سوداء وشفرات عدسة بيضاء مع نقطة تصويب برتقالية" else "Dark Edition • Deep carbon background, white shutter blades, and vivid orange crosshair target",
                        drawableRes = R.drawable.ic_oneshot_logo_dark,
                        isSelected = state.preferredLogo == "dark" || (state.preferredLogo == "auto" && state.isDarkTheme),
                        isAuto = state.preferredLogo == "auto",
                        themeLabel = if (state.isArabic) "الوضع الداكن" else "Dark Mode Native",
                        onSelect = { onSelectLogo("dark") }
                    )

                    // Card 2: The White OneShot Logo (Light Edition)
                    LogoEditionCard(
                        title = if (state.isArabic) "الشعار الفاتح (الأبيض)" else "The White OneShot Logo",
                        subtitle = if (state.isArabic) "النسخة الفاتحة الرسمية • خلفية بيضاء نقية وشفرات عدسة سوداء مع نقطة تصويب برتقالية" else "Light Edition • Clean white canvas, glossy obsidian shutter blades, and vivid orange crosshair target",
                        drawableRes = R.drawable.ic_oneshot_logo_light,
                        isSelected = state.preferredLogo == "light" || (state.preferredLogo == "auto" && !state.isDarkTheme),
                        isAuto = state.preferredLogo == "auto",
                        themeLabel = if (state.isArabic) "الوضع الفاتح" else "Light Mode Native",
                        onSelect = { onSelectLogo("light") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onSelectLogo("auto") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (state.preferredLogo == "auto") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoMode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (state.preferredLogo == "auto") OneShotOrange else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isArabic) "تلقائي مع الثيم" else "Auto Theme Match",
                            fontSize = 12.sp,
                            fontWeight = if (state.preferredLogo == "auto") FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OneShotOrange)
                    ) {
                        Text(
                            text = if (state.isArabic) "تم" else "Done",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoEditionCard(
    title: String,
    subtitle: String,
    drawableRes: Int,
    isSelected: Boolean,
    isAuto: Boolean,
    themeLabel: String,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) OneShotOrange else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Title, Badge and Radio/Check indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = OneShotOrange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = themeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OneShotOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(OneShotOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Large Visual Image Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onSelect,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isSelected) "Active in App" else "Apply This Logo",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) OneShotOrange else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) OneShotOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
