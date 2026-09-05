package com.example.oneshotai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.ScreenTab
import com.example.oneshotai.ui.viewmodel.UiState

data class AgentCardData(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val roleEn: String,
    val roleAr: String,
    val descEn: String,
    val descAr: String,
    val icon: ImageVector,
    val isFeatured: Boolean = false
)

val defaultAgents = listOf(
    AgentCardData(
        id = "deepfind",
        nameEn = "DeepFind Research Agent",
        nameAr = "وكيل البحث DeepFind",
        roleEn = "Domain Intelligence",
        roleAr = "استخبارات النطاقات",
        descEn = "Collect public domain evidence (DNS, WHOIS, HTTP headers, Technology, Wayback) and produce source-labelled reports.",
        descAr = "يجمع الأدلة العامة عن النطاق (DNS، WHOIS، الترويسات، التقنيات، الأرشيف) ويصدر تقريرًا موثقًا بمصادر واضحة.",
        icon = Icons.Default.Search,
        isFeatured = true
    ),
    AgentCardData(
        id = "gm",
        nameEn = "General Manager",
        nameAr = "المدير العام",
        roleEn = "Orchestrator",
        roleAr = "منسق العمليات",
        descEn = "Coordinates workflows across OneShot departments and delegates complex tasks.",
        descAr = "ينسق تدفقات العمل عبر أقسام OneShot ويوجه المهام المعقدة للوكلاء المختصين.",
        icon = Icons.Default.ManageAccounts
    ),
    AgentCardData(
        id = "email",
        nameEn = "Email Agent",
        nameAr = "وكيل البريد",
        roleEn = "Communications",
        roleAr = "الاتصالات والردود",
        descEn = "Drafts responses, classifies incoming communications, and manages automated follow-ups.",
        descAr = "يصيغ الردود، يصنف الرسائل الواردة، ويدير المتابعات البريدية الذكية.",
        icon = Icons.Default.Email
    ),
    AgentCardData(
        id = "dev",
        nameEn = "Developer Agent",
        nameAr = "وكيل التطوير",
        roleEn = "Engineering",
        roleAr = "الهندسة البرمجية",
        descEn = "Builds, tests, and maintains modern web and mobile applications.",
        descAr = "يبني ويختبر ويطور تطبيقات الويب والجوال المتقدمة.",
        icon = Icons.Default.Code
    ),
    AgentCardData(
        id = "ops",
        nameEn = "Operations Agent",
        nameAr = "وكيل العمليات",
        roleEn = "Logistics & Invoicing",
        roleAr = "الفواتير والعمليات",
        descEn = "Oversees payments, invoice reconciliations, and service uptime telemetry.",
        descAr = "يشرف على المدفوعات، مطابقة الفواتير، ومراقبة جاهزية الخدمات.",
        icon = Icons.Default.Insights
    ),
    AgentCardData(
        id = "marketing",
        nameEn = "Marketing Agent",
        nameAr = "وكيل التسويق",
        roleEn = "Growth & Content",
        roleAr = "النمو والمحتوى",
        descEn = "Crafts editorial campaigns, social updates, and analytics performance summaries.",
        descAr = "يصمم الحملات التسويقية، المنشورات الرقمية، وملخصات تحليلات الأداء.",
        icon = Icons.Default.Campaign
    )
)

@Composable
fun AgentsScreen(
    state: UiState,
    onNavigateTab: (ScreenTab) -> Unit,
    onStartAgentChat: (AgentCardData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = if (state.isArabic) "وكلاء الذكاء الاصطناعي" else "OneShot Agents",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (state.isArabic) "طاقم عمل متكامل من الوكلاء المتخصصين المربوطين بالأدوات" else "A coordinated staff of specialized AI agents with scoped tool access",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(defaultAgents) { agent ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (agent.isFeatured) OneShotOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    tonalElevation = if (agent.isFeatured) 4.dp else 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (agent.id == "deepfind") {
                                onNavigateTab(ScreenTab.DEEPFIND)
                            } else {
                                onStartAgentChat(agent)
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (agent.isFeatured) OneShotOrange.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = agent.icon,
                                    contentDescription = null,
                                    tint = if (agent.isFeatured) OneShotOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (agent.isFeatured) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = OneShotOrange
                                ) {
                                    Text(
                                        text = if (state.isArabic) "وكيل مميز" else "FEATURED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (state.isArabic) agent.nameAr else agent.nameEn,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (state.isArabic) agent.roleAr else agent.roleEn,
                            fontSize = 11.5.sp,
                            color = OneShotOrange,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (state.isArabic) agent.descAr else agent.descEn,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (agent.id == "deepfind") {
                                    onNavigateTab(ScreenTab.DEEPFIND)
                                } else {
                                    onStartAgentChat(agent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (agent.isFeatured) OneShotOrange else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (agent.isFeatured) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = if (agent.id == "deepfind") {
                                    if (state.isArabic) "فتح وكيل البحث" else "Open Research Agent"
                                } else {
                                    if (state.isArabic) "بدء جلسة" else "Start Session"
                                },
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
