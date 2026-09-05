package com.example.oneshotai.data.local.financial

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
        FinancialAccountEntity::class,
        FinancialTransactionEntity::class,
        InvoiceEntity::class,
        FinancialBudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinancialDatabase : RoomDatabase() {
    abstract fun financialDao(): FinancialDao

    companion object {
        @Volatile
        private var INSTANCE: FinancialDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinancialDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinancialDatabase::class.java,
                    "financial_data.db"
                )
                    .addCallback(FinancialDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class FinancialDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialFinancialData(database.financialDao())
                    }
                }
            }

            suspend fun populateInitialFinancialData(dao: FinancialDao) {
                val now = System.currentTimeMillis()
                val oneDay = 86400000L

                // 1. Initial Business / Merchant Accounts
                val accOperating = FinancialAccountEntity(
                    id = "acc_operating",
                    name = "Mercury Operating Checking",
                    type = "CHECKING",
                    balance = 124580.50,
                    currency = "USD",
                    institutionName = "Mercury Bank",
                    accountNumberLast4 = "4819",
                    isActive = true,
                    updatedAt = now
                )

                val accStripe = FinancialAccountEntity(
                    id = "acc_stripe",
                    name = "Stripe Merchant Settlement",
                    type = "MERCHANT_SETTLEMENT",
                    balance = 38920.75,
                    currency = "USD",
                    institutionName = "Stripe Treasury",
                    accountNumberLast4 = "9021",
                    isActive = true,
                    updatedAt = now
                )

                val accReserve = FinancialAccountEntity(
                    id = "acc_reserve",
                    name = "Treasury Tax & Yield Reserve",
                    type = "SAVINGS",
                    balance = 75000.00,
                    currency = "USD",
                    institutionName = "Silicon Valley Bank",
                    accountNumberLast4 = "1182",
                    isActive = true,
                    updatedAt = now
                )

                val accCorporateCard = FinancialAccountEntity(
                    id = "acc_corp_card",
                    name = "Brex Corporate Card",
                    type = "CREDIT_CARD",
                    balance = -8420.30,
                    currency = "USD",
                    institutionName = "Brex",
                    accountNumberLast4 = "7734",
                    isActive = true,
                    updatedAt = now
                )

                dao.insertAccounts(listOf(accOperating, accStripe, accReserve, accCorporateCard))

                // 2. Structured Financial Transactions
                val transactions = listOf(
                    FinancialTransactionEntity(
                        id = "tx_001",
                        title = "Enterprise Annual License - Acme Corp",
                        amount = 28500.00,
                        type = "INCOME",
                        category = "Sales Revenue",
                        accountId = "acc_operating",
                        status = "COMPLETED",
                        counterparty = "Acme Technologies",
                        notes = "Wire payment for annual multi-agent plan",
                        timestamp = now - (oneDay * 1),
                        isTaxDeductible = false
                    ),
                    FinancialTransactionEntity(
                        id = "tx_002",
                        title = "Stripe Daily Merchant Batch Payout",
                        amount = 14320.40,
                        type = "INCOME",
                        category = "Merchant Payout",
                        accountId = "acc_operating",
                        status = "COMPLETED",
                        counterparty = "Stripe Payments",
                        notes = "Settled batch payout for online subscriptions",
                        timestamp = now - (oneDay * 2),
                        isTaxDeductible = false
                    ),
                    FinancialTransactionEntity(
                        id = "tx_003",
                        title = "Google Cloud Platform Infrastructure",
                        amount = 3840.60,
                        type = "EXPENSE",
                        category = "SaaS & Cloud",
                        accountId = "acc_corp_card",
                        status = "COMPLETED",
                        counterparty = "Google Cloud",
                        notes = "Inference TPU clusters & Cloud Run deployment",
                        timestamp = now - (oneDay * 3),
                        isTaxDeductible = true
                    ),
                    FinancialTransactionEntity(
                        id = "tx_004",
                        title = "Anthropic & OpenAI API Usage",
                        amount = 2150.25,
                        type = "EXPENSE",
                        category = "SaaS & Cloud",
                        accountId = "acc_corp_card",
                        status = "COMPLETED",
                        counterparty = "AI Model Providers",
                        notes = "Monthly model tokens consumption",
                        timestamp = now - (oneDay * 4),
                        isTaxDeductible = true
                    ),
                    FinancialTransactionEntity(
                        id = "tx_005",
                        title = "Quarterly Payroll Distribution",
                        amount = 45000.00,
                        type = "EXPENSE",
                        category = "Payroll",
                        accountId = "acc_operating",
                        status = "COMPLETED",
                        counterparty = "Gusto Payroll",
                        notes = "Engineering and ops team bi-weekly compensation",
                        timestamp = now - (oneDay * 5),
                        isTaxDeductible = true
                    ),
                    FinancialTransactionEntity(
                        id = "tx_006",
                        title = "Stripe Processing Fees (2.9% + 30c)",
                        amount = 412.50,
                        type = "EXPENSE",
                        category = "Processing Fees",
                        accountId = "acc_stripe",
                        status = "COMPLETED",
                        counterparty = "Stripe Inc",
                        notes = "Interchange and merchant processing fees",
                        timestamp = now - (oneDay * 2),
                        isTaxDeductible = true
                    ),
                    FinancialTransactionEntity(
                        id = "tx_007",
                        title = "Growth & Search Acquisition Ads",
                        amount = 1850.00,
                        type = "EXPENSE",
                        category = "Marketing",
                        accountId = "acc_corp_card",
                        status = "COMPLETED",
                        counterparty = "Google Ads",
                        notes = "Customer acquisition campaign",
                        timestamp = now - (oneDay * 6),
                        isTaxDeductible = true
                    ),
                    FinancialTransactionEntity(
                        id = "tx_008",
                        title = "Refund - Order #8841 Cancelled",
                        amount = 499.00,
                        type = "REFUND",
                        category = "Sales Revenue",
                        accountId = "acc_stripe",
                        status = "COMPLETED",
                        counterparty = "Apex Global Ltd",
                        notes = "Customer downgrade credit note",
                        timestamp = now - (oneDay * 7),
                        isTaxDeductible = false
                    )
                )
                dao.insertTransactions(transactions)

                // 3. Client & Merchant Invoices
                val invoices = listOf(
                    InvoiceEntity(
                        id = "inv_101",
                        invoiceNumber = "INV-2026-0089",
                        customerName = "Vertex Media Dynamics",
                        customerEmail = "billing@vertexmedia.io",
                        amount = 18500.00,
                        currency = "USD",
                        status = "PENDING",
                        issueDate = now - (oneDay * 5),
                        dueDate = now + (oneDay * 10),
                        itemsSummary = "Dedicated DeepFind Domain Intelligence & Workflow Automation Engine",
                        notes = "Net-15 payment terms"
                    ),
                    InvoiceEntity(
                        id = "inv_102",
                        invoiceNumber = "INV-2026-0088",
                        customerName = "Cascade AI Research",
                        customerEmail = "accounts@cascade-ai.org",
                        amount = 32000.00,
                        currency = "USD",
                        status = "PAID",
                        issueDate = now - (oneDay * 20),
                        dueDate = now - (oneDay * 5),
                        paidAt = now - (oneDay * 6),
                        itemsSummary = "Multi-Agent Orchestration & Enterprise Sandbox Deployment",
                        notes = "Paid via Fedwire"
                    ),
                    InvoiceEntity(
                        id = "inv_103",
                        invoiceNumber = "INV-2026-0090",
                        customerName = "NovaTech Solutions",
                        customerEmail = "procure@novatech.co",
                        amount = 9400.00,
                        currency = "USD",
                        status = "PENDING",
                        issueDate = now - (oneDay * 2),
                        dueDate = now + (oneDay * 12),
                        itemsSummary = "Custom Agent Tooling & Knowledge Base Integration",
                        notes = "Due upon milestone delivery"
                    ),
                    InvoiceEntity(
                        id = "inv_104",
                        invoiceNumber = "INV-2026-0085",
                        customerName = "Helios Capital Partners",
                        customerEmail = "finance@helioscap.com",
                        amount = 12500.00,
                        currency = "USD",
                        status = "OVERDUE",
                        issueDate = now - (oneDay * 45),
                        dueDate = now - (oneDay * 15),
                        itemsSummary = "Market Data Intelligence Dashboard & Real-time Feeds",
                        notes = "Second reminder sent"
                    )
                )
                dao.insertInvoices(invoices)

                // 4. Budget Tracking Categories
                val budgets = listOf(
                    FinancialBudgetEntity(
                        id = "bg_cloud",
                        category = "SaaS & Cloud",
                        allocatedAmount = 8000.00,
                        spentAmount = 5990.85,
                        currency = "USD",
                        period = "MONTHLY",
                        colorHex = "#2196F3"
                    ),
                    FinancialBudgetEntity(
                        id = "bg_marketing",
                        category = "Marketing",
                        allocatedAmount = 5000.00,
                        spentAmount = 1850.00,
                        currency = "USD",
                        period = "MONTHLY",
                        colorHex = "#E05520"
                    ),
                    FinancialBudgetEntity(
                        id = "bg_payroll",
                        category = "Payroll",
                        allocatedAmount = 50000.00,
                        spentAmount = 45000.00,
                        currency = "USD",
                        period = "MONTHLY",
                        colorHex = "#4CAF50"
                    ),
                    FinancialBudgetEntity(
                        id = "bg_fees",
                        category = "Processing Fees",
                        allocatedAmount = 1000.00,
                        spentAmount = 412.50,
                        currency = "USD",
                        period = "MONTHLY",
                        colorHex = "#FF9800"
                    )
                )
                dao.insertBudgets(budgets)
            }
        }
    }
}
