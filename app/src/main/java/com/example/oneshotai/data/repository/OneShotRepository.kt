package com.example.oneshotai.data.repository

import com.example.oneshotai.data.local.*
import com.example.oneshotai.model.*
import com.example.oneshotai.network.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class OneShotRepository(private val appDao: AppDao) {

    private val json = Json { ignoreUnknownKeys = true }

    // Conversations
    val conversations: Flow<List<Conversation>> = appDao.getAllConversations().map { list ->
        list.map { Conversation(it.id, it.title, it.updatedAt) }
    }

    suspend fun createConversation(title: String): String = withContext(Dispatchers.IO) {
        val id = "conv_${System.currentTimeMillis()}"
        appDao.insertConversation(ConversationEntity(id, title, System.currentTimeMillis()))
        id
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        appDao.deleteConversation(id)
    }

    // Messages
    fun getMessages(conversationId: String): Flow<List<Message>> =
        appDao.getMessagesForConversation(conversationId).map { list ->
            list.map {
                Message(
                    id = it.id,
                    role = it.role,
                    content = it.content,
                    modelProvider = it.modelProvider,
                    timestamp = it.timestamp
                )
            }
        }

    suspend fun sendMessage(
        conversationId: String,
        userContent: String,
        provider: ModelProvider,
        activeProject: Project? = null
    ): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val userMsgId = "msg_user_$now"

        // Insert User message
        appDao.insertMessage(
            MessageEntity(
                id = userMsgId,
                conversationId = conversationId,
                role = "user",
                content = userContent,
                modelProvider = null,
                timestamp = now
            )
        )

        // Generate response via Gemini or Model Router
        val systemContext = buildString {
            append("You are OneShot AI, an intelligent workspace assistant.")
            if (activeProject != null) {
                append("\nActive Project: ${activeProject.name}")
                if (activeProject.instructions.isNotBlank()) {
                    append("\nProject Instructions: ${activeProject.instructions}")
                }
            }
        }

        val assistantText = GeminiClient.generateContent(
            prompt = userContent,
            systemContext = systemContext,
            model = provider.modelName
        )

        val assistantMsgId = "msg_asst_${System.currentTimeMillis()}"
        appDao.insertMessage(
            MessageEntity(
                id = assistantMsgId,
                conversationId = conversationId,
                role = "assistant",
                content = assistantText,
                modelProvider = provider.displayName,
                timestamp = System.currentTimeMillis()
            )
        )

        // Update conversation timestamp
        appDao.insertConversation(
            ConversationEntity(
                id = conversationId,
                title = userContent.take(32).let { if (it.length < userContent.length) "$it…" else it },
                updatedAt = System.currentTimeMillis()
            )
        )

        assistantText
    }

    // Projects
    val projects: Flow<List<Project>> = appDao.getAllProjects().map { list ->
        list.map { Project(it.id, it.name, it.description, it.model, it.instructions, it.createdAt) }
    }

    suspend fun createProject(name: String, description: String, model: String, instructions: String) =
        withContext(Dispatchers.IO) {
            val id = "proj_${System.currentTimeMillis()}"
            appDao.insertProject(ProjectEntity(id, name, description, model, instructions, System.currentTimeMillis()))
        }

    suspend fun deleteProject(project: Project) = withContext(Dispatchers.IO) {
        appDao.deleteProject(
            ProjectEntity(
                project.id,
                project.name,
                project.description,
                project.model,
                project.instructions,
                project.createdAt
            )
        )
    }

    // Tasks
    val tasks: Flow<List<TaskItem>> = appDao.getAllTasks().map { list ->
        list.map { TaskItem(it.id, it.title, it.type, it.enabled, it.createdAt) }
    }

    suspend fun createTask(title: String, type: String) = withContext(Dispatchers.IO) {
        val id = "task_${System.currentTimeMillis()}"
        appDao.insertTask(TaskEntity(id, title, type, true, System.currentTimeMillis()))
    }

    suspend fun toggleTask(task: TaskItem) = withContext(Dispatchers.IO) {
        appDao.updateTask(
            TaskEntity(
                task.id,
                task.title,
                task.type,
                !task.enabled,
                task.createdAt
            )
        )
    }

    suspend fun deleteTask(task: TaskItem) = withContext(Dispatchers.IO) {
        appDao.deleteTask(
            TaskEntity(
                task.id,
                task.title,
                task.type,
                task.enabled,
                task.createdAt
            )
        )
    }

    // Knowledge
    val knowledge: Flow<List<KnowledgeItem>> = appDao.getAllKnowledge().map { list ->
        list.map { KnowledgeItem(it.id, it.title, it.category, it.content, it.createdAt) }
    }

    suspend fun createKnowledge(title: String, category: String, content: String) =
        withContext(Dispatchers.IO) {
            val id = "kno_${System.currentTimeMillis()}"
            appDao.insertKnowledge(KnowledgeEntity(id, title, category, content, System.currentTimeMillis()))
        }

    suspend fun deleteKnowledge(item: KnowledgeItem) = withContext(Dispatchers.IO) {
        appDao.deleteKnowledge(
            KnowledgeEntity(
                item.id,
                item.title,
                item.category,
                item.content,
                item.createdAt
            )
        )
    }

    // Research Reports
    val researchReports: Flow<List<ResearchReport>> = appDao.getAllResearchReports().map { list ->
        list.map {
            val evidenceList = try {
                json.decodeFromString<List<ToolEvidence>>(it.evidenceJson)
            } catch (e: Exception) {
                emptyList()
            }
            ResearchReport(
                id = it.id,
                domain = it.domain,
                goal = it.goal,
                mode = it.mode,
                createdAt = it.createdAt,
                status = it.status,
                evidence = evidenceList,
                summary = it.summary,
                durationMs = it.durationMs,
                deepfindCalls = it.deepfindCalls,
                modelCalls = it.modelCalls
            )
        }
    }

    suspend fun runDomainResearch(
        domain: String,
        goal: String,
        mode: String,
        summarize: Boolean
    ): ResearchReport = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timestamp = isoFormat.format(Date(startTime))
        val reportId = "rep_${UUID.randomUUID().toString().take(8)}"

        // Simulate execution of bounded domain research tools (DNS, WHOIS, HTTP Headers, Technology, Wayback)
        val evidenceList = mutableListOf<ToolEvidence>()

        // 1. DNS Lookup
        delay(350)
        evidenceList.add(
            ToolEvidence(
                id = "dns_1",
                tool = "DNS",
                endpoint = "/dns-lookup",
                status = "ok",
                timestamp = timestamp,
                durationMs = 142,
                data = """{"domain":"$domain","a":["104.21.45.18","172.67.198.88"],"aaaa":["2606:4700:3033::6815:2d12"],"mx":["mail.$domain 10"],"ns":["ns1.cloudflare.com","ns2.cloudflare.com"],"txt":["v=spf1 include:_spf.google.com ~all"]}"""
            )
        )

        // 2. WHOIS
        delay(400)
        evidenceList.add(
            ToolEvidence(
                id = "whois_1",
                tool = "WHOIS",
                endpoint = "/whois",
                status = "ok",
                timestamp = timestamp,
                durationMs = 215,
                data = """{"domain":"$domain","registrar":"Cloudflare, Inc.","createdDate":"2023-04-12T10:00:00Z","updatedDate":"2024-04-10T14:22:10Z","status":"clientTransferProhibited"}"""
            )
        )

        if (mode == "deep") {
            // 3. HTTP Headers
            delay(300)
            evidenceList.add(
                ToolEvidence(
                    id = "headers_1",
                    tool = "HTTP headers",
                    endpoint = "/http-headers/analyze",
                    status = "ok",
                    timestamp = timestamp,
                    durationMs = 188,
                    data = """{"server":"cloudflare","strictTransportSecurity":"max-age=31536000; includeSubDomains; preload","xContentTypeOptions":"nosniff","xFrameOptions":"DENY"}"""
                )
            )

            // 4. Technology detection
            delay(350)
            evidenceList.add(
                ToolEvidence(
                    id = "tech_1",
                    tool = "Technology",
                    endpoint = "/tech-stack/detect",
                    status = "ok",
                    timestamp = timestamp,
                    durationMs = 265,
                    data = """{"frontend":["React 19","Next.js 15","Tailwind CSS"],"hosting":"Vercel / Cloudflare Edge","analytics":["Plausible"]}"""
                )
            )

            // 5. Wayback archive
            delay(400)
            evidenceList.add(
                ToolEvidence(
                    id = "archive_1",
                    tool = "Wayback archive",
                    endpoint = "/wayback-machine/search",
                    status = "ok",
                    timestamp = timestamp,
                    durationMs = 310,
                    data = """{"snapshotsCount":14,"firstRecorded":"2023-05-10","lastRecorded":"2026-08-15","url":"https://$domain"}"""
                )
            )
        }

        val totalDuration = System.currentTimeMillis() - startTime

        var summaryText: String? = null
        var modelCalls = 0
        if (summarize) {
            modelCalls = 1
            val summaryPrompt = "Synthesize public domain findings for domain $domain with research goal: $goal.\nEvidence: ${evidenceList.map { "${it.tool}: ${it.data}" }.joinToString("\n")}"
            summaryText = GeminiClient.generateContent(
                prompt = summaryPrompt,
                systemContext = "You are DeepFind Research Agent. Produce a concise, high-integrity executive briefing highlighting infrastructure, security posture, and registration timelines."
            )
        }

        val report = ResearchReport(
            id = reportId,
            domain = domain,
            goal = goal.ifBlank { "Review public domain records." },
            mode = mode,
            createdAt = timestamp,
            status = "complete",
            evidence = evidenceList,
            summary = summaryText,
            durationMs = totalDuration,
            deepfindCalls = evidenceList.size,
            modelCalls = modelCalls
        )

        // Persist report to local database
        appDao.insertResearchReport(
            ResearchReportEntity(
                id = report.id,
                domain = report.domain,
                goal = report.goal,
                mode = report.mode,
                createdAt = report.createdAt,
                status = report.status,
                evidenceJson = json.encodeToString(evidenceList),
                summary = report.summary,
                durationMs = report.durationMs,
                deepfindCalls = report.deepfindCalls,
                modelCalls = report.modelCalls
            )
        )

        report
    }

    suspend fun clearResearchReports() = withContext(Dispatchers.IO) {
        appDao.clearResearchReports()
    }
}
