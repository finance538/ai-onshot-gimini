package com.example.oneshotai.data.local.financial

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a business or merchant financial account (checking, settlement, reserve, etc.).
 */
@Entity(
    tableName = "financial_accounts"
)
data class FinancialAccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // CHECKING, SAVINGS, MERCHANT_SETTLEMENT, CREDIT_CARD, ESCROW
    val balance: Double,
    val currency: String = "USD",
    val institutionName: String,
    val accountNumberLast4: String,
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Represents a structured financial transaction with classification, type, and relational links.
 */
@Entity(
    tableName = "financial_transactions",
    foreignKeys = [
        ForeignKey(
            entity = FinancialAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("accountId"),
        Index("timestamp"),
        Index("type"),
        Index("category")
    ]
)
data class FinancialTransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Double,
    val type: String, // INCOME, EXPENSE, TRANSFER, REFUND, PAYOUT
    val category: String, // Sales Revenue, Processing Fees, Inventory, Payroll, SaaS & Cloud, Marketing
    val accountId: String?,
    val currency: String = "USD",
    val status: String = "COMPLETED", // COMPLETED, PENDING, FAILED
    val counterparty: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isTaxDeductible: Boolean = false
)

/**
 * Represents client invoices, payment status, and receivables tracking.
 */
@Entity(
    tableName = "invoices",
    indices = [
        Index("status"),
        Index("dueDate"),
        Index("invoiceNumber", unique = true)
    ]
)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val customerName: String,
    val customerEmail: String,
    val amount: Double,
    val currency: String = "USD",
    val status: String, // DRAFT, PENDING, PAID, OVERDUE, CANCELLED
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long,
    val paidAt: Long? = null,
    val itemsSummary: String = "",
    val notes: String? = null
)

/**
 * Represents budget limits and category expenditure caps.
 */
@Entity(
    tableName = "financial_budgets",
    indices = [
        Index("category", unique = true)
    ]
)
data class FinancialBudgetEntity(
    @PrimaryKey val id: String,
    val category: String,
    val allocatedAmount: Double,
    val spentAmount: Double = 0.0,
    val currency: String = "USD",
    val period: String = "MONTHLY", // MONTHLY, QUARTERLY, ANNUAL
    val colorHex: String = "#E05520"
)

/**
 * Query projection for category spending aggregations.
 */
data class CategorySpendingSummary(
    val category: String,
    val totalAmount: Double,
    val transactionCount: Int
)
