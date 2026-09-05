package com.example.oneshotai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.oneshotai.data.repository.OneShotRepository
import com.example.oneshotai.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenTab(val titleEn: String, val titleAr: String) {
    CHAT("Chat", "المحادثة"),
    PROJECTS("Projects", "المشاريع"),
    AGENTS("Agents", "الوكلاء"),
    DEEPFIND("DeepFind", "وكيل البحث"),
    TASKS("Tasks", "المهام"),
    KNOWLEDGE("Knowledge", "المعرفة"),
    TOOLS("Tools", "الأدوات")
}

data class UiState(
    val activeTab: ScreenTab = ScreenTab.CHAT,
    val isArabic: Boolean = false,
    val isDarkTheme: Boolean = true,
    val preferredLogo: String = "auto", // "auto", "dark", "light"
    val showBrandDialog: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val activeConversationId: String? = null,
    val messages: List<Message> = emptyList(),
    val isSending: Boolean = false,
    val currentProvider: ModelProvider = ModelProvider.GEMINI_3_8_FLASH,
    val projects: List<Project> = emptyList(),
    val activeProject: Project? = null,
    val tasks: List<TaskItem> = emptyList(),
    val knowledgeList: List<KnowledgeItem> = emptyList(),
    val toolsList: List<ToolItem> = emptyList(),
    // Audio / Voice note states
    val isRecordingVoiceNote: Boolean = false,
    val isListeningLive: Boolean = false,
    val attachments: List<String> = emptyList(),
    // DeepFind Agent states
    val isDeepFindUnlocked: Boolean = false,
    val deepFindCode: String = "",
    val selectedDomain: String = "1shotcam.com",
    val allowedDomains: List<String> = listOf("1shotcam.com", "deepfind.me", "oneshot.ai", "finance538.org"),
    val researchMode: String = "quick", // "quick" or "deep"
    val researchGoal: String = "",
    val summarizeWithAi: Boolean = true,
    val isAuthorizedDomain: Boolean = false,
    val isResearching: Boolean = false,
    val currentReport: ResearchReport? = null,
    val savedReports: List<ResearchReport> = emptyList(),
    val errorMessage: String? = null,
    // Computer Control & Interactive Tool Console states
    val selectedToolForConsole: ToolItem? = null,
    val computerHosts: List<ComputerHost> = emptyList(),
    val selectedHost: ComputerHost? = null,
    val cursorPosition: Pair<Int, Int> = Pair(640, 420),
    val activeDesktopWindow: String = "VS Code — OneShot Workspace",
    val computerLogs: List<ComputerActionLog> = emptyList(),
    val terminalOutput: List<String> = emptyList(),
    val isExecutingComputerAction: Boolean = false
) {
    val activeLogoRes: Int
        get() = when (preferredLogo) {
            "dark" -> com.example.oneshotai.R.drawable.ic_oneshot_logo_dark
            "light" -> com.example.oneshotai.R.drawable.ic_oneshot_logo_light
            else -> if (isDarkTheme) com.example.oneshotai.R.drawable.ic_oneshot_logo_dark else com.example.oneshotai.R.drawable.ic_oneshot_logo_light
        }
}

class OneShotViewModel(private val repository: OneShotRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Collect conversations
        viewModelScope.launch {
            repository.conversations.collect { convList ->
                _uiState.update { current ->
                    val activeId = current.activeConversationId ?: convList.firstOrNull()?.id
                    current.copy(
                        conversations = convList,
                        activeConversationId = activeId
                    )
                }
                if (_uiState.value.activeConversationId != null) {
                    observeMessages(_uiState.value.activeConversationId!!)
                }
            }
        }

        // Collect projects
        viewModelScope.launch {
            repository.projects.collect { projList ->
                _uiState.update { it.copy(projects = projList) }
            }
        }

        // Collect tasks
        viewModelScope.launch {
            repository.tasks.collect { taskList ->
                _uiState.update { it.copy(tasks = taskList) }
            }
        }

        // Collect knowledge
        viewModelScope.launch {
            repository.knowledge.collect { knoList ->
                _uiState.update { it.copy(knowledgeList = knoList) }
            }
        }

        // Collect research reports
        viewModelScope.launch {
            repository.researchReports.collect { repList ->
                _uiState.update { it.copy(savedReports = repList) }
            }
        }

        // Initialize Computer Hosts
        val hosts = listOf(
            ComputerHost("h_mac", "MacBook Pro M3 (macOS 14.5)", "macOS", "Connected", "127.0.0.1:5900", "2560x1440"),
            ComputerHost("h_ubuntu", "Ubuntu 24.04 LTS (Sandbox)", "Ubuntu", "Connected", "sandbox.internal:8080", "1920x1080"),
            ComputerHost("h_win", "Windows 11 Pro (VM)", "Windows", "Standby", "192.168.1.140:3389", "1920x1080")
        )

        val initialLogs = listOf(
            ComputerActionLog("log_1", "shortcut", "⌘ + Space", "Opened Spotlight Search", "success", System.currentTimeMillis() - 60000),
            ComputerActionLog("log_2", "type", "Terminal.app", "Launched Terminal application", "success", System.currentTimeMillis() - 50000),
            ComputerActionLog("log_3", "bash", "git status --short", "Clean working tree (main branch)", "success", System.currentTimeMillis() - 20000)
        )

        val initialTerminal = listOf(
            "oneshot@sandbox-m3:~$ uptime",
            " 02:58:14 up 14 days,  6:42,  1 user,  load average: 0.28, 0.35, 0.31",
            "oneshot@sandbox-m3:~$ ps aux | grep node",
            "node      1428  0.4  2.1 1420800 178220 ?  Sl   Aug28  24:12 node server.js",
            "oneshot@sandbox-m3:~$ echo 'Computer Control bridge active.'"
        )

        // Initialize Tools list
        _uiState.update {
            it.copy(
                computerHosts = hosts,
                selectedHost = hosts.first(),
                computerLogs = initialLogs,
                terminalOutput = initialTerminal,
                toolsList = listOf(
                    ToolItem("t_comp", "Computer Control (OS Automation)", "Execute mouse clicks, keystrokes, window control, and shell processes across connected OS instances", "computer", category = "OS & Automation", connected = true, hasInteractiveConsole = true),
                    ToolItem("t_browser", "Browser Agent (Playwright)", "Automate headless Chromium browsing, DOM inspection, form submissions, and multi-page web extractions", "browser", category = "OS & Automation", connected = true, hasInteractiveConsole = true),
                    ToolItem("t_terminal", "Terminal & Shell Sandbox", "Isolated Linux container execution environment with persistent filesystems and package managers", "terminal", category = "Developer", connected = true, hasInteractiveConsole = true),
                    ToolItem("t_vision", "Screen Vision & OCR Inspector", "Multi-modal visual UI coordinate mapping, reading on-screen elements, and verifying visual UI state", "vision", category = "OS & Automation", connected = true, hasInteractiveConsole = true),
                    ToolItem("t_db", "Database Studio (SQL & Vector)", "Direct query runner for PostgreSQL, MySQL, SQLite, and vector embeddings indexes", "database", category = "Developer", connected = true, hasInteractiveConsole = true),
                    ToolItem("t_1", "Gmail", "Access inbox, send drafts, manage threads", "mail", category = "Cloud & Workspace", connected = false),
                    ToolItem("t_2", "Google Calendar", "Create meetings, read schedules, sync agendas", "calendar", category = "Cloud & Workspace", connected = true),
                    ToolItem("t_3", "Google Drive", "Read docs, spreadsheets and cloud assets", "drive", category = "Cloud & Workspace", connected = false),
                    ToolItem("t_4", "GitHub", "Review pull requests, commit changes, issue triage", "github", category = "Developer", connected = true),
                    ToolItem("t_5", "Slack", "Notify channels, monitor alert feeds", "slack", category = "Cloud & Workspace", connected = false),
                    ToolItem("t_6", "Local Ollama Bridge", "Outbound inference connection to local hardware", "hardware", category = "Developer", connected = true),
                    ToolItem("t_7", "DeepFind API", "Public domain DNS, WHOIS, and HTTP headers", "search", category = "Intelligence", connected = true)
                )
            )
        }
    }

    private var messagesJob: kotlinx.coroutines.Job? = null

    private fun observeMessages(convId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessages(convId).collect { msgList ->
                _uiState.update { it.copy(messages = msgList) }
            }
        }
    }

    fun selectConversation(id: String) {
        _uiState.update { it.copy(activeConversationId = id) }
        observeMessages(id)
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val title = if (_uiState.value.isArabic) "محادثة جديدة" else "New Conversation"
            val newId = repository.createConversation(title)
            selectConversation(newId)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_uiState.value.activeConversationId == id) {
                val remaining = _uiState.value.conversations.filter { it.id != id }
                val next = remaining.firstOrNull()?.id
                _uiState.update { it.copy(activeConversationId = next) }
                if (next != null) observeMessages(next) else _uiState.update { it.copy(messages = emptyList()) }
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val convId = _uiState.value.activeConversationId ?: run {
            startNewConversation()
            return
        }

        _uiState.update { it.copy(isSending = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                repository.sendMessage(
                    conversationId = convId,
                    userContent = trimmed,
                    provider = _uiState.value.currentProvider,
                    activeProject = _uiState.value.activeProject
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to send message") }
            } finally {
                _uiState.update { it.copy(isSending = false, attachments = emptyList()) }
            }
        }
    }

    fun setScreenTab(tab: ScreenTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setModelProvider(provider: ModelProvider) {
        _uiState.update { it.copy(currentProvider = provider) }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isArabic = !it.isArabic) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun setPreferredLogo(mode: String) { // "auto", "dark", "light"
        _uiState.update { it.copy(preferredLogo = mode) }
    }

    fun toggleBrandDialog(show: Boolean) {
        _uiState.update { it.copy(showBrandDialog = show) }
    }

    fun addAttachment(fileName: String) {
        _uiState.update { it.copy(attachments = it.attachments + fileName) }
    }

    fun removeAttachment(fileName: String) {
        _uiState.update { it.copy(attachments = it.attachments - fileName) }
    }

    fun toggleVoiceNoteRecording() {
        _uiState.update {
            val newState = !it.isRecordingVoiceNote
            it.copy(
                isRecordingVoiceNote = newState,
                isListeningLive = false
            )
        }
    }

    fun toggleLiveListening() {
        _uiState.update {
            val newState = !it.isListeningLive
            it.copy(
                isListeningLive = newState,
                isRecordingVoiceNote = false
            )
        }
    }

    // Projects
    fun createProject(name: String, description: String, model: String, instructions: String) {
        viewModelScope.launch {
            repository.createProject(name, description, model, instructions)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun setActiveProject(project: Project?) {
        _uiState.update { it.copy(activeProject = project) }
    }

    // Tasks
    fun createTask(title: String, type: String) {
        viewModelScope.launch {
            repository.createTask(title, type)
        }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Knowledge
    fun createKnowledge(title: String, category: String, content: String) {
        viewModelScope.launch {
            repository.createKnowledge(title, category, content)
        }
    }

    fun deleteKnowledge(item: KnowledgeItem) {
        viewModelScope.launch {
            repository.deleteKnowledge(item)
        }
    }

    // DeepFind Research
    fun unlockDeepFind(code: String) {
        if (code.isNotBlank()) {
            _uiState.update { it.copy(isDeepFindUnlocked = true, deepFindCode = "", errorMessage = null) }
        } else {
            _uiState.update { it.copy(errorMessage = "Please enter an access code") }
        }
    }

    fun lockDeepFind() {
        _uiState.update { it.copy(isDeepFindUnlocked = false, currentReport = null) }
    }

    fun setDeepFindDomain(domain: String) {
        _uiState.update { it.copy(selectedDomain = domain) }
    }

    fun setDeepFindMode(mode: String) {
        _uiState.update { it.copy(researchMode = mode) }
    }

    fun setDeepFindGoal(goal: String) {
        _uiState.update { it.copy(researchGoal = goal) }
    }

    fun setSummarizeWithAi(value: Boolean) {
        _uiState.update { it.copy(summarizeWithAi = value) }
    }

    fun setAuthorizedDomain(value: Boolean) {
        _uiState.update { it.copy(isAuthorizedDomain = value) }
    }

    fun runDeepFindResearch() {
        val state = _uiState.value
        if (!state.isAuthorizedDomain) {
            _uiState.update { it.copy(errorMessage = "Authorization confirmation is required.") }
            return
        }

        _uiState.update { it.copy(isResearching = true, errorMessage = null, currentReport = null) }

        viewModelScope.launch {
            try {
                val report = repository.runDomainResearch(
                    domain = state.selectedDomain,
                    goal = state.researchGoal,
                    mode = state.researchMode,
                    summarize = state.summarizeWithAi
                )
                _uiState.update { it.copy(currentReport = report, isResearching = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isResearching = false,
                        errorMessage = e.message ?: "Domain research execution failed"
                    )
                }
            }
        }
    }

    fun selectSavedReport(report: ResearchReport) {
        _uiState.update { it.copy(currentReport = report) }
    }

    fun clearSavedReports() {
        viewModelScope.launch {
            repository.clearResearchReports()
        }
    }

    fun toggleToolConnection(toolId: String) {
        _uiState.update { state ->
            val updated = state.toolsList.map { tool ->
                if (tool.id == toolId) tool.copy(connected = !tool.connected) else tool
            }
            state.copy(toolsList = updated)
        }
    }

    // Computer Control Console methods
    fun openToolConsole(tool: ToolItem) {
        _uiState.update { it.copy(selectedToolForConsole = tool) }
    }

    fun closeToolConsole() {
        _uiState.update { it.copy(selectedToolForConsole = null) }
    }

    fun selectComputerHost(host: ComputerHost) {
        _uiState.update { it.copy(selectedHost = host) }
    }

    fun executeComputerMouseAction(action: String, x: Int, y: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExecutingComputerAction = true,
                    cursorPosition = Pair(x, y)
                )
            }
            kotlinx.coroutines.delay(200)

            val actionDesc = when (action) {
                "click" -> "Left click at ($x, $y)"
                "double_click" -> "Double click at ($x, $y)"
                "right_click" -> "Right click at ($x, $y)"
                "move" -> "Cursor moved to ($x, $y)"
                else -> "$action at ($x, $y)"
            }

            val newLog = ComputerActionLog(
                id = "log_${System.currentTimeMillis()}",
                actionType = "mouse",
                parameters = "($x, $y)",
                result = actionDesc,
                status = "success"
            )

            _uiState.update {
                it.copy(
                    isExecutingComputerAction = false,
                    computerLogs = listOf(newLog) + it.computerLogs.take(49)
                )
            }
        }
    }

    fun executeComputerKeyboardAction(text: String, isShortcut: Boolean) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExecutingComputerAction = true) }
            kotlinx.coroutines.delay(250)

            val newLog = ComputerActionLog(
                id = "log_${System.currentTimeMillis()}",
                actionType = if (isShortcut) "shortcut" else "type",
                parameters = text,
                result = if (isShortcut) "Executed keyboard shortcut $text" else "Typed \"$text\"",
                status = "success"
            )

            _uiState.update {
                it.copy(
                    isExecutingComputerAction = false,
                    computerLogs = listOf(newLog) + it.computerLogs.take(49)
                )
            }
        }
    }

    fun executeComputerBashCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExecutingComputerAction = true,
                    terminalOutput = it.terminalOutput + "oneshot@sandbox:~$ $trimmed"
                )
            }
            kotlinx.coroutines.delay(350)

            val output = when {
                trimmed.startsWith("git") -> "On branch main\nYour branch is up to date with 'origin/main'.\nnothing to commit, working tree clean"
                trimmed.startsWith("ls") -> "app/  build.gradle.kts  gradle/  metadata.json  README.md  settings.gradle.kts"
                trimmed.startsWith("whoami") -> "oneshot (uid=1000, gid=1000, groups=1000(oneshot),27(sudo),999(docker))"
                trimmed.startsWith("docker") -> "CONTAINER ID   IMAGE                COMMAND                  STATUS          PORTS\n8f3a9e102bc4   oneshot/sandbox:v2   \"/entrypoint.sh\"        Up 3 hours      0.0.0.0:8080->8080/tcp"
                trimmed.startsWith("uptime") -> " 03:01:22 up 14 days,  6:45,  1 user,  load average: 0.18, 0.22, 0.19"
                trimmed.startsWith("echo") -> trimmed.removePrefix("echo").trim().removeSurrounding("\"").removeSurrounding("'")
                else -> "Command dispatched to host '${_uiState.value.selectedHost?.name ?: "host"}': execution exit code 0."
            }

            val newLines = output.lines()
            val newLog = ComputerActionLog(
                id = "log_${System.currentTimeMillis()}",
                actionType = "bash",
                parameters = trimmed,
                result = output.lines().firstOrNull() ?: "Success",
                status = "success"
            )

            _uiState.update {
                it.copy(
                    isExecutingComputerAction = false,
                    terminalOutput = (it.terminalOutput + newLines).takeLast(60),
                    computerLogs = listOf(newLog) + it.computerLogs.take(49)
                )
            }
        }
    }

    fun executeComputerRecipe(recipeName: String, prompt: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExecutingComputerAction = true,
                    terminalOutput = it.terminalOutput + "oneshot@sandbox:~$ [RECIPE RUNNER] $recipeName..."
                )
            }

            kotlinx.coroutines.delay(400)
            _uiState.update {
                it.copy(terminalOutput = it.terminalOutput + "  -> Inspecting active window coordinates...")
            }
            kotlinx.coroutines.delay(400)
            _uiState.update {
                it.copy(terminalOutput = it.terminalOutput + "  -> Dispatched action sequence to ${_uiState.value.selectedHost?.name ?: "active host"}")
            }
            kotlinx.coroutines.delay(300)

            val newLog = ComputerActionLog(
                id = "log_${System.currentTimeMillis()}",
                actionType = "recipe",
                parameters = recipeName,
                result = "Recipe completed successfully with 0 errors",
                status = "success"
            )

            _uiState.update {
                it.copy(
                    isExecutingComputerAction = false,
                    terminalOutput = it.terminalOutput + "  -> [COMPLETED] $recipeName execution finished.",
                    computerLogs = listOf(newLog) + it.computerLogs.take(49)
                )
            }
        }
    }

    fun clearComputerLogs() {
        _uiState.update { it.copy(computerLogs = emptyList(), terminalOutput = emptyList()) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class OneShotViewModelFactory(private val repository: OneShotRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OneShotViewModel::class.java)) {
            return OneShotViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
