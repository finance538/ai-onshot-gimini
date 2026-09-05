package com.example.oneshotai.data.local.financial

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialDao {

    // ==========================================
    // TRANSACTIONS
    // ==========================================

    @Query("SELECT * FROM financial_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: String): Flow<FinancialTransactionEntity?>

    @Query("SELECT * FROM financial_transactions WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getTransactionsByAccount(accountId: String): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getTransactionsByCategory(category: String): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT SUM(amount) FROM financial_transactions WHERE type = 'INCOME' AND status = 'COMPLETED'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM financial_transactions WHERE type = 'EXPENSE' AND status = 'COMPLETED'")
    fun getTotalExpense(): Flow<Double?>

    @Query("""
        SELECT category, SUM(amount) as totalAmount, COUNT(*) as transactionCount
        FROM financial_transactions
        WHERE type = 'EXPENSE' AND status = 'COMPLETED'
        GROUP BY category
        ORDER BY totalAmount DESC
    """)
    fun getCategorySpendingBreakdown(): Flow<List<CategorySpendingSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<FinancialTransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: FinancialTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransactionEntity)

    @Query("DELETE FROM financial_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM financial_transactions")
    suspend fun clearTransactions()

    // ==========================================
    // ACCOUNTS
    // ==========================================

    @Query("SELECT * FROM financial_accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<FinancialAccountEntity>>

    @Query("SELECT * FROM financial_accounts WHERE isActive = 1 ORDER BY balance DESC")
    fun getActiveAccounts(): Flow<List<FinancialAccountEntity>>

    @Query("SELECT * FROM financial_accounts WHERE id = :id LIMIT 1")
    fun getAccountById(id: String): Flow<FinancialAccountEntity?>

    @Query("SELECT SUM(balance) FROM financial_accounts WHERE isActive = 1")
    fun getTotalNetWorth(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinancialAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<FinancialAccountEntity>)

    @Update
    suspend fun updateAccount(account: FinancialAccountEntity)

    @Query("UPDATE financial_accounts SET balance = :newBalance, updatedAt = :updatedAt WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: String, newBalance: Double, updatedAt: Long)

    @Delete
    suspend fun deleteAccount(account: FinancialAccountEntity)

    @Query("DELETE FROM financial_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: String)

    // ==========================================
    // INVOICES
    // ==========================================

    @Query("SELECT * FROM invoices ORDER BY dueDate ASC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY dueDate ASC")
    fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    fun getInvoiceById(id: String): Flow<InvoiceEntity?>

    @Query("SELECT SUM(amount) FROM invoices WHERE status = 'PENDING' OR status = 'OVERDUE'")
    fun getTotalPendingInvoiceAmount(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM invoices WHERE status = 'PAID'")
    fun getTotalCollectedInvoiceAmount(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: String)

    // ==========================================
    // BUDGETS
    // ==========================================

    @Query("SELECT * FROM financial_budgets ORDER BY allocatedAmount DESC")
    fun getAllBudgets(): Flow<List<FinancialBudgetEntity>>

    @Query("SELECT * FROM financial_budgets WHERE category = :category LIMIT 1")
    fun getBudgetByCategory(category: String): Flow<FinancialBudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: FinancialBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<FinancialBudgetEntity>)

    @Update
    suspend fun updateBudget(budget: FinancialBudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: FinancialBudgetEntity)
}
