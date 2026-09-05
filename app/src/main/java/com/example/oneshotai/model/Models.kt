package com.example.oneshotai.model

import kotlinx.serialization.Serializable

enum class ModelProvider(val id: String, val displayName: String, val modelName: String) {
    AUTO("auto", "⚡ Auto Router (3.8 Flash)", "gemini-3.8-flash"),
    GEMINI_3_8_FLASH("gemini-3.8-flash", "Gemini 3.8 Flash", "gemini-3.8-flash"),
    GOOGLE("google", "Gemini 3.5 Flash", "gemini-3.5-flash"),
    ANTHROPIC("anthropic", "Claude 3.5 Sonnet", "claude-3-5-sonnet"),
    OPENAI("openai", "OpenAI GPT-5", "gpt-5"),
    OLLAMA_LLAMA3("ollama-llama3", "Llama 3 Local", "llama3:latest"),
    OLLAMA_HERMES3("ollama-hermes3", "Hermes 3 Local", "hermes3:latest")
}

data class Message(
    val id: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val modelProvider: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val attachments: List<String> = emptyList()
)

data class Conversation(
    val id: String,
    val title: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val model: String = "Auto Router",
    val instructions: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class TaskItem(
    val id: String,
    val title: String,
    val type: String, // "Immediate", "Scheduled", "Recurring", "Watch"
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class KnowledgeItem(
    val id: String,
    val title: String,
    val category: String, // "Brand", "Company", "Engineering", "Operations"
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class AgentItem(
    val id: String,
    val code: String,
    val nameEn: String,
    val nameAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val isAvailable: Boolean = true
)

data class ToolItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: String = "Cloud & Workspace",
    val connected: Boolean = false,
    val hasInteractiveConsole: Boolean = false
)

data class ComputerHost(
    val id: String,
    val name: String,
    val os: String,
    val status: String,
    val ip: String,
    val resolution: String
)

@Serializable
data class ComputerActionLog(
    val id: String,
    val actionType: String,
    val parameters: String,
    val result: String,
    val status: String, // "success", "running", "error"
    val timestamp: Long = System.currentTimeMillis()
)

// DeepFind Domain Research Models
@Serializable
data class ToolEvidence(
    val id: String,
    val tool: String,
    val endpoint: String,
    val status: String,
    val timestamp: String,
    val durationMs: Long,
    val data: String? = null,
    val error: String? = null
)

@Serializable
data class ResearchReport(
    val id: String,
    val domain: String,
    val goal: String,
    val mode: String, // "quick" or "deep"
    val createdAt: String,
    val status: String, // "complete", "partial", "failed"
    val evidence: List<ToolEvidence>,
    val summary: String? = null,
    val durationMs: Long = 0,
    val deepfindCalls: Int = 0,
    val modelCalls: Int = 0
)
