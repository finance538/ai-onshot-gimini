package com.example.oneshotai.data.local.financial

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class FinancialRepository(private val financialDao: FinancialDao) {

    // --- Flow Streams (Reactive UI / ViewModel) ---
    val allTransactions: Flow<List<FinancialTransactionEntity>> = financialDao.getAllTransactions()
    val allAccounts: Flow<List<FinancialAccountEntity>> = financialDao.getAllAccounts()
    val activeAccounts: Flow<List<FinancialAccountEntity>> = financialDao.getActiveAccounts()
    val allInvoices: Flow<List<InvoiceEntity>> = financialDao.getAllInvoices()
    val allBudgets: Flow<List<FinancialBudgetEntity>> = financialDao.getAllBudgets()

    val totalNetWorth: Flow<Double?> = financialDao.getTotalNetWorth()
    val totalIncome: Flow<Double?> = financialDao.getTotalIncome()
    val totalExpense: Flow<Double?> = financialDao.getTotalExpense()
    val pendingInvoicesTotal: Flow<Double?> = financialDao.getTotalPendingInvoiceAmount()
    val categorySpending: Flow<List<CategorySpendingSummary>> = financialDao.getCategorySpendingBreakdown()

    fun getRecentTransactions(limit: Int = 10): Flow<List<FinancialTransactionEntity>> =
        financialDao.getRecentTransactions(limit)

    fun getTransactionsByAccount(accountId: String): Flow<List<FinancialTransactionEntity>> =
        financialDao.getTransactionsByAccount(accountId)

    fun getTransactionsByType(type: String): Flow<List<FinancialTransactionEntity>> =
        financialDao.getTransactionsByType(type)

    fun getTransactionsByCategory(category: String): Flow<List<FinancialTransactionEntity>> =
        financialDao.getTransactionsByCategory(category)

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<FinancialTransactionEntity>> =
        financialDao.getTransactionsBetween(startTime, endTime)

    fun getTransactionById(id: String): Flow<FinancialTransactionEntity?> =
        financialDao.getTransactionById(id)

    fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>> =
        financialDao.getInvoicesByStatus(status)

    // --- Suspend Write Operations (Dispatchers.IO) ---

    suspend fun recordTransaction(
        title: String,
        amount: Double,
        type: String, // INCOME, EXPENSE, TRANSFER, REFUND
        category: String,
        accountId: String?,
        counterparty: String? = null,
        notes: String? = null,
        isTaxDeductible: Boolean = false
    ): FinancialTransactionEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val tx = FinancialTransactionEntity(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            title = title,
            amount = amount,
            type = type,
            category = category,
            accountId = accountId,
            status = "COMPLETED",
            counterparty = counterparty,
            notes = notes,
            timestamp = now,
            isTaxDeductible = isTaxDeductible
        )
        financialDao.insertTransaction(tx)

        // Automatically reflect balance change on linked account
        if (accountId != null) {
            val account = financialDao.getAccountById(accountId).firstOrNull()
            if (account != null) {
                val newBalance = when (type.uppercase()) {
                    "INCOME" -> account.balance + amount
                    "EXPENSE", "REFUND" -> account.balance - amount
                    else -> account.balance
                }
                financialDao.updateAccountBalance(accountId, newBalance, now)
            }
        }
        tx
    }

    suspend fun updateTransaction(transaction: FinancialTransactionEntity) = withContext(Dispatchers.IO) {
        financialDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: FinancialTransactionEntity) = withContext(Dispatchers.IO) {
        financialDao.deleteTransaction(transaction)
    }

    suspend fun createAccount(
        name: String,
        type: String,
        initialBalance: Double,
        currency: String = "USD",
        institutionName: String,
        accountNumberLast4: String
    ): FinancialAccountEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val account = FinancialAccountEntity(
            id = "acc_${UUID.randomUUID().toString().take(8)}",
            name = name,
            type = type,
            balance = initialBalance,
            currency = currency,
            institutionName = institutionName,
            accountNumberLast4 = accountNumberLast4,
            isActive = true,
            updatedAt = now
        )
        financialDao.insertAccount(account)
        account
    }

    suspend fun updateAccount(account: FinancialAccountEntity) = withContext(Dispatchers.IO) {
        financialDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: FinancialAccountEntity) = withContext(Dispatchers.IO) {
        financialDao.deleteAccount(account)
    }

    suspend fun createInvoice(
        invoiceNumber: String,
        customerName: String,
        customerEmail: String,
        amount: Double,
        dueDate: Long,
        itemsSummary: String,
        notes: String? = null
    ): InvoiceEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val invoice = InvoiceEntity(
            id = "inv_${UUID.randomUUID().toString().take(8)}",
            invoiceNumber = invoiceNumber,
            customerName = customerName,
            customerEmail = customerEmail,
            amount = amount,
            currency = "USD",
            status = "PENDING",
            issueDate = now,
            dueDate = dueDate,
            itemsSummary = itemsSummary,
            notes = notes
        )
        financialDao.insertInvoice(invoice)
        invoice
    }

    suspend fun markInvoicePaid(invoiceId: String, depositAccountId: String?) = withContext(Dispatchers.IO) {
        val invoice = financialDao.getInvoiceById(invoiceId).firstOrNull()
        if (invoice != null) {
            val now = System.currentTimeMillis()
            val updated = invoice.copy(
                status = "PAID",
                paidAt = now
            )
            financialDao.updateInvoice(updated)

            if (depositAccountId != null) {
                recordTransaction(
                    title = "Payment for ${invoice.invoiceNumber}",
                    amount = invoice.amount,
                    type = "INCOME",
                    category = "Sales Revenue",
                    accountId = depositAccountId,
                    counterparty = invoice.customerName,
                    notes = "Invoice ${invoice.invoiceNumber} paid in full",
                    isTaxDeductible = false
                )
            }
        }
    }

    suspend fun updateInvoice(invoice: InvoiceEntity) = withContext(Dispatchers.IO) {
        financialDao.updateInvoice(invoice)
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity) = withContext(Dispatchers.IO) {
        financialDao.deleteInvoice(invoice)
    }

    suspend fun setBudget(
        category: String,
        allocatedAmount: Double,
        currency: String = "USD",
        period: String = "MONTHLY",
        colorHex: String = "#E05520"
    ) = withContext(Dispatchers.IO) {
        val existing = financialDao.getBudgetByCategory(category).firstOrNull()
        if (existing != null) {
            financialDao.updateBudget(
                existing.copy(
                    allocatedAmount = allocatedAmount,
                    period = period,
                    colorHex = colorHex
                )
            )
        } else {
            financialDao.insertBudget(
                FinancialBudgetEntity(
                    id = "bg_${UUID.randomUUID().toString().take(8)}",
                    category = category,
                    allocatedAmount = allocatedAmount,
                    spentAmount = 0.0,
                    currency = currency,
                    period = period,
                    colorHex = colorHex
                )
            )
        }
    }
}
