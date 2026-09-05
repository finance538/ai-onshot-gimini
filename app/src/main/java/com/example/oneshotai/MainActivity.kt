package com.example.oneshotai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneshotai.data.local.AppDatabase
import com.example.oneshotai.data.local.financial.FinancialDatabase
import com.example.oneshotai.data.repository.OneShotRepository
import com.example.oneshotai.ui.components.BrandLogosDialog
import com.example.oneshotai.ui.components.ConversationDrawer
import com.example.oneshotai.ui.components.OneShotBottomBar
import com.example.oneshotai.ui.components.OneShotTopBar
import com.example.oneshotai.ui.screens.*
import com.example.oneshotai.ui.theme.OneShotTheme
import com.example.oneshotai.ui.viewmodel.OneShotViewModel
import com.example.oneshotai.ui.viewmodel.OneShotViewModelFactory
import com.example.oneshotai.ui.viewmodel.ScreenTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this, CoroutineScope(Dispatchers.IO))
        FinancialDatabase.getDatabase(this, CoroutineScope(Dispatchers.IO))
        val repository = OneShotRepository(db.appDao())
        val factory = OneShotViewModelFactory(repository)

        setContent {
            val viewModel: OneShotViewModel = viewModel(factory = factory)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            val layoutDirection = if (state.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                OneShotTheme(darkTheme = state.isDarkTheme) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ConversationDrawer(
                                conversations = state.conversations,
                                activeId = state.activeConversationId,
                                isArabic = state.isArabic,
                                onSelectConversation = { id ->
                                    viewModel.selectConversation(id)
                                    viewModel.setScreenTab(ScreenTab.CHAT)
                                },
                                onNewConversation = {
                                    viewModel.startNewConversation()
                                    viewModel.setScreenTab(ScreenTab.CHAT)
                                },
                                onDeleteConversation = { id ->
                                    viewModel.deleteConversation(id)
                                },
                                onCloseDrawer = {
                                    scope.launch { drawerState.close() }
                                },
                                logoRes = state.activeLogoRes,
                                onOpenBrandDialog = { viewModel.toggleBrandDialog(true) }
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                OneShotTopBar(
                                    state = state,
                                    onModelSelected = { viewModel.setModelProvider(it) },
                                    onToggleLanguage = { viewModel.toggleLanguage() },
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onOpenBrandDialog = { viewModel.toggleBrandDialog(true) }
                                )
                            },
                            bottomBar = {
                                OneShotBottomBar(
                                    selectedTab = state.activeTab,
                                    isArabic = state.isArabic,
                                    onTabSelected = { viewModel.setScreenTab(it) }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (state.activeTab) {
                                    ScreenTab.CHAT -> ChatScreen(
                                        state = state,
                                        onSendMessage = { viewModel.sendMessage(it) },
                                        onAddAttachment = { viewModel.addAttachment(it) },
                                        onRemoveAttachment = { viewModel.removeAttachment(it) },
                                        onToggleVoiceNote = { viewModel.toggleVoiceNoteRecording() },
                                        onToggleLiveVoice = { viewModel.toggleLiveListening() },
                                        onDismissError = { viewModel.dismissError() },
                                        onNavigateTab = { viewModel.setScreenTab(it) }
                                    )
                                    ScreenTab.PROJECTS -> ProjectsScreen(
                                        state = state,
                                        onCreateProject = { name, desc, model, inst ->
                                            viewModel.createProject(name, desc, model, inst)
                                        },
                                        onDeleteProject = { viewModel.deleteProject(it) },
                                        onSetActiveProject = { viewModel.setActiveProject(it) },
                                        onNavigateTab = { viewModel.setScreenTab(it) }
                                    )
                                    ScreenTab.AGENTS -> AgentsScreen(
                                        state = state,
                                        onNavigateTab = { viewModel.setScreenTab(it) },
                                        onStartAgentChat = { agent ->
                                            viewModel.startNewConversation()
                                            viewModel.setScreenTab(ScreenTab.CHAT)
                                            viewModel.sendMessage(
                                                if (state.isArabic) "بدء جلسة مع ${agent.nameAr} (${agent.roleAr})"
                                                else "Starting session with ${agent.nameEn} (${agent.roleEn})"
                                            )
                                        }
                                    )
                                    ScreenTab.DEEPFIND -> DeepFindScreen(
                                        state = state,
                                        onUnlock = { viewModel.unlockDeepFind(it) },
                                        onLock = { viewModel.lockDeepFind() },
                                        onSelectDomain = { viewModel.setDeepFindDomain(it) },
                                        onSelectMode = { viewModel.setDeepFindMode(it) },
                                        onSetGoal = { viewModel.setDeepFindGoal(it) },
                                        onSetSummarize = { viewModel.setSummarizeWithAi(it) },
                                        onSetAuthorized = { viewModel.setAuthorizedDomain(it) },
                                        onRunResearch = { viewModel.runDeepFindResearch() },
                                        onSelectSavedReport = { viewModel.selectSavedReport(it) },
                                        onClearSavedReports = { viewModel.clearSavedReports() },
                                        onNavigateBack = { viewModel.setScreenTab(ScreenTab.AGENTS) }
                                    )
                                    ScreenTab.TASKS -> TasksScreen(
                                        state = state,
                                        onCreateTask = { title, type ->
                                            viewModel.createTask(title, type)
                                        },
                                        onToggleTask = { viewModel.toggleTask(it) },
                                        onDeleteTask = { viewModel.deleteTask(it) }
                                    )
                                    ScreenTab.KNOWLEDGE -> KnowledgeScreen(
                                        state = state,
                                        onCreateKnowledge = { title, cat, content ->
                                            viewModel.createKnowledge(title, cat, content)
                                        },
                                        onDeleteKnowledge = { viewModel.deleteKnowledge(it) }
                                    )
                                    ScreenTab.TOOLS -> ToolsScreen(
                                        state = state,
                                        onToggleTool = { viewModel.toggleToolConnection(it) },
                                        onOpenConsole = { viewModel.openToolConsole(it) },
                                        onCloseConsole = { viewModel.closeToolConsole() },
                                        onSelectHost = { viewModel.selectComputerHost(it) },
                                        onMouseAction = { action, x, y -> viewModel.executeComputerMouseAction(action, x, y) },
                                        onKeyboardAction = { text, isShortcut -> viewModel.executeComputerKeyboardAction(text, isShortcut) },
                                        onBashCommand = { viewModel.executeComputerBashCommand(it) },
                                        onRecipeRun = { name, code -> viewModel.executeComputerRecipe(name, code) },
                                        onClearLogs = { viewModel.clearComputerLogs() },
                                        onOpenBrandDialog = { viewModel.toggleBrandDialog(true) }
                                    )
                                }
                            }
                        }
                    }

                    if (state.showBrandDialog) {
                        BrandLogosDialog(
                            state = state,
                            onDismiss = { viewModel.toggleBrandDialog(false) },
                            onSelectLogo = { viewModel.setPreferredLogo(it) },
                            onToggleTheme = { viewModel.toggleTheme() }
                        )
                    }
                }
            }
        }
    }
}
