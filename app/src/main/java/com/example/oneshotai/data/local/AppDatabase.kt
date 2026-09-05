package com.example.oneshotai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        KnowledgeEntity::class,
        ResearchReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oneshot_ai.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: AppDao) {
                val now = System.currentTimeMillis()
                val convId = "conv_default"

                dao.insertConversation(
                    ConversationEntity(
                        id = convId,
                        title = "OneShot System Overview",
                        updatedAt = now
                    )
                )

                dao.insertMessage(
                    MessageEntity(
                        id = "msg_1",
                        conversationId = convId,
                        role = "assistant",
                        content = "Welcome to OneShot AI — the cloud-first AI workspace. Connect models, manage specialized agents, and access unified company memory.",
                        modelProvider = "⚡ Auto Router",
                        timestamp = now
                    )
                )

                dao.insertProject(
                    ProjectEntity(
                        id = "proj_1",
                        name = "DeepFind Research",
                        description = "Automated domain intelligence gathering and threat verification.",
                        model = "⚡ Auto Router",
                        instructions = "Maintain strict factual accuracy and cite public domain DNS/WHOIS records.",
                        createdAt = now - 3600000
                    )
                )

                dao.insertProject(
                    ProjectEntity(
                        id = "proj_2",
                        name = "OneShot Workspace",
                        description = "Centralized multi-agent coordination hub.",
                        model = "Claude 3.5 Sonnet",
                        instructions = "Focus on clean architecture and responsive multi-language support.",
                        createdAt = now - 7200000
                    )
                )

                dao.insertTask(
                    TaskEntity(
                        id = "task_1",
                        title = "Audit public domain DNS & WHOIS records",
                        type = "Scheduled",
                        enabled = true,
                        createdAt = now - 5000000
                    )
                )

                dao.insertTask(
                    TaskEntity(
                        id = "task_2",
                        title = "Summarize weekly customer inquiries",
                        type = "Recurring",
                        enabled = true,
                        createdAt = now - 8000000
                    )
                )

                dao.insertKnowledge(
                    KnowledgeEntity(
                        id = "kno_1",
                        title = "OneShot Brand System / 2026",
                        category = "Brand",
                        content = "Primary Accent: Copper Orange (#E05520, #FB8656). Dark Panel (#0B0D0F, #121416). High contrast typography and clean borders.",
                        createdAt = now - 10000000
                    )
                )

                dao.insertKnowledge(
                    KnowledgeEntity(
                        id = "kno_2",
                        title = "Company Knowledge Base",
                        category = "Company",
                        content = "OneShot operates provider-independent model routing with persistent memory across conversation, project, and agent scopes.",
                        createdAt = now - 12000000
                    )
                )
            }
        }
    }
}
