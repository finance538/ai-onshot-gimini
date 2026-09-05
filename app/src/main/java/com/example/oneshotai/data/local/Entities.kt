package com.example.oneshotai.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val updatedAt: Long
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val modelProvider: String?,
    val timestamp: Long
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val model: String,
    val instructions: String,
    val createdAt: Long
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val enabled: Boolean,
    val createdAt: Long
)

@Entity(tableName = "knowledge")
data class KnowledgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val content: String,
    val createdAt: Long
)

@Entity(tableName = "research_reports")
data class ResearchReportEntity(
    @PrimaryKey val id: String,
    val domain: String,
    val goal: String,
    val mode: String,
    val createdAt: String,
    val status: String,
    val evidenceJson: String,
    val summary: String?,
    val durationMs: Long,
    val deepfindCalls: Int,
    val modelCalls: Int
)
