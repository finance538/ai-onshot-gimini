package com.example.oneshotai.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneshotai.model.ResearchReport
import com.example.oneshotai.model.ToolEvidence
import com.example.oneshotai.ui.theme.AccentGreen
import com.example.oneshotai.ui.theme.OneShotOrange
import com.example.oneshotai.ui.viewmodel.ScreenTab
import com.example.oneshotai.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepFindScreen(
    state: UiState,
    onUnlock: (String) -> Unit,
    onLock: () -> Unit,
    onSelectDomain: (String) -> Unit,
    onSelectMode: (String) -> Unit,
    onSetGoal: (String) -> Unit,
    onSetSummarize: (Boolean) -> Unit,
    onSetAuthorized: (Boolean) -> Unit,
    onRunResearch: () -> Unit,
    onSelectSavedReport: (ResearchReport) -> Unit,
    onClearSavedReports: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var accessCodeInput by remember { mutableStateOf("") }
    var domainMenuExpanded by remember { mutableStateOf(false) }
    var modeMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column {
                        Text(
                            text = if (state.isArabic) "وكيل البحث DeepFind" else "DeepFind Research Agent",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "ONESHOT / RESEARCH AGENT / v1",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OneShotOrange,
                            letterSpacing = 1.sp
                        )
                    }
                }

                if (state.isDeepFindUnlocked) {
                    TextButton(onClick = onLock) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (state.isArabic) "قفل" else "Lock", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Subtitle / Intro
        item {
            Text(
                text = if (state.isArabic)
                    "يجمع الأدلة العامة عن النطاق (DNS، WHOIS، الترويسات، التقنيات، الأرشيف) ويصدر تقريرًا موثقًا بمصادر واضحة."
                else
                    "Collect public domain evidence and produce one source-labelled report.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        // UNLOCKED STATE VS LOCKED STATE
        if (!state.isDeepFindUnlocked) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = OneShotOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isArabic) "دخول مساحة عمل البحث الخاصة" else "Private workspace access",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (state.isArabic)
                                "أدخل رمز دخول وكيل البحث لفتح أدوات التحري."
                            else
                                "Enter the research access code to unlock domain inspection tools.",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = accessCodeInput,
                            onValueChange = { accessCodeInput = it },
                            label = { Text(if (state.isArabic) "رمز الدخول" else "Access code") },
                            placeholder = { Text("e.g. 1shot or admin") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onUnlock(accessCodeInput.ifBlank { "1shot" }) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OneShotOrange)
                        ) {
                            Text(
                                text = if (state.isArabic) "فتح الوكيل" else "Unlock agent",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Unlocked Form
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Domain & Mode row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Domain Select
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedCard(
                                    onClick = { domainMenuExpanded = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (state.isArabic) "النطاق المعتمد" else "Approved domain",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = state.selectedDomain,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = domainMenuExpanded,
                                    onDismissRequest = { domainMenuExpanded = false }
                                ) {
                                    state.allowedDomains.forEach { domain ->
                                        DropdownMenuItem(
                                            text = { Text(domain) },
                                            onClick = {
                                                onSelectDomain(domain)
                                                domainMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Mode Select
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedCard(
                                    onClick = { modeMenuExpanded = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (state.isArabic) "وضع البحث" else "Research mode",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (state.researchMode == "quick") {
                                                if (state.isArabic) "سريع (أداتان)" else "Quick (2 tools)"
                                            } else {
                                                if (state.isArabic) "موسع (5 أدوات)" else "Deep (5 tools)"
                                            },
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = modeMenuExpanded,
                                    onDismissRequest = { modeMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (state.isArabic) "سريع - أداتان (DNS, WHOIS)" else "Quick - 2 tools (DNS, WHOIS)") },
                                        onClick = {
                                            onSelectMode("quick")
                                            modeMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (state.isArabic) "موسع - حتى 5 أدوات" else "Deep - up to 5 tools") },
                                        onClick = {
                                            onSelectMode("deep")
                                            modeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Goal input
                        OutlinedTextField(
                            value = state.researchGoal,
                            onValueChange = onSetGoal,
                            label = { Text(if (state.isArabic) "ما الذي يركز عليه التقرير؟" else "What should the report focus on?") },
                            placeholder = { Text(if (state.isArabic) "سجلات النطاق والبنية العامة وتاريخ الموقع" else "Domain records, public infrastructure and site history") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tool Plan Steps preview
                        Text(
                            text = if (state.isArabic) "خطة الأدوات" else "EXECUTION PLAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            val steps = if (state.researchMode == "quick") {
                                listOf("1. DNS", "2. WHOIS")
                            } else {
                                listOf("1. DNS", "2. WHOIS", "3. HTTP headers", "4. Technology", "5. Wayback (10)")
                            }
                            items(steps) { step ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = step,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Checkboxes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onSetSummarize(!state.summarizeWithAi) }
                        ) {
                            Checkbox(
                                checked = state.summarizeWithAi,
                                onCheckedChange = onSetSummarize,
                                colors = CheckboxDefaults.colors(checkedColor = OneShotOrange)
                            )
                            Text(
                                text = if (state.isArabic) "تلخيص التقرير بواسطة Gemini 3.8 Flash" else "Summarize with Gemini AI (gemini-3.8-flash)",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onSetAuthorized(!state.isAuthorizedDomain) }
                        ) {
                            Checkbox(
                                checked = state.isAuthorizedDomain,
                                onCheckedChange = onSetAuthorized,
                                colors = CheckboxDefaults.colors(checkedColor = OneShotOrange)
                            )
                            Text(
                                text = if (state.isArabic) "لدي تصريح للبحث عن هذا النطاق." else "I am authorized to research this domain.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Run Agent Button
                        Button(
                            onClick = onRunResearch,
                            enabled = !state.isResearching && state.isAuthorizedDomain,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OneShotOrange,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            )
                        ) {
                            if (state.isResearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.isArabic) "جارٍ جمع الأدلة العامة…" else "Collecting public domain evidence…")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.isArabic) "تشغيل الوكيل" else "Run agent", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // REPORT SECTION
            if (state.currentReport != null) {
                val report = state.currentReport
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OneShotOrange.copy(alpha = 0.5f)),
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Report Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (state.isArabic) "تقرير البحث" else "Research Report",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${report.domain} • ${report.status} • ${report.durationMs / 1000}s",
                                        fontSize = 12.sp,
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "OneShot Research Report: ${report.domain}\nSummary: ${report.summary ?: "N/A"}")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Report"))
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (state.isArabic) "مشاركة" else "Share JSON", fontSize = 12.sp)
                                }
                            }

                            // Summary Box
                            if (!report.summary.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = OneShotOrange.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OneShotOrange.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = OneShotOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (state.isArabic) "الملخص التنفيذي بالذكاء الاصطناعي" else "Gemini Executive Briefing",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OneShotOrange
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = report.summary,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Evidence items breakdown
                            Text(
                                text = if (state.isArabic) "الأدلة وسجلات الفحص (${report.evidence.size})" else "PUBLIC EVIDENCE (${report.evidence.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            report.evidence.forEach { ev ->
                                EvidenceCard(evidence = ev)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // SAVED REPORTS LIST
            if (state.savedReports.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (state.isArabic) "التقارير المحفوظة (${state.savedReports.size})" else "Saved Reports (${state.savedReports.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onClearSavedReports) {
                            Text(if (state.isArabic) "مسح" else "Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                items(state.savedReports) { saved ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSavedReport(saved) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = saved.domain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${saved.mode.uppercase()} • ${saved.status}",
                                    fontSize = 11.sp,
                                    color = OneShotOrange
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvidenceCard(evidence: ToolEvidence) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[${evidence.tool}] ${evidence.endpoint}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${evidence.durationMs}ms",
                    fontSize = 11.sp,
                    color = AccentGreen
                )
            }

            if (expanded && evidence.data != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = evidence.data,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
