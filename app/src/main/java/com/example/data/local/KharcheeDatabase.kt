package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ExpenseRecord
import com.example.data.model.SavingsGoalRecord
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "expenses")
data class ExpenseRoomEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val category: String,
    val amount: Long,
    val paymentMode: String,
    val type: String,
    val note: String,
    val txEpochMillis: Long,
    val exactAmount: Double = amount.toDouble(),
    val transactionScope: String = LifetimeFinancialMemorySystem.classifyTransactionScope(
        category = category,
        type = type,
        note = note
    )
) {
    fun toDomainModel(): ExpenseRecord = ExpenseRecord(
        id = id,
        userId = userId,
        title = title,
        category = category,
        amount = amount,
        exactAmount = if (exactAmount > 0.0) exactAmount else amount.toDouble(),
        paymentMode = paymentMode,
        type = type,
        note = note,
        txEpochMillis = txEpochMillis
    )

    companion object {
        fun fromDomainModel(record: ExpenseRecord): ExpenseRoomEntity = ExpenseRoomEntity(
            id = record.id,
            userId = record.userId,
            title = record.title,
            category = record.category,
            amount = record.amount,
            paymentMode = record.paymentMode,
            type = record.type,
            note = record.note,
            txEpochMillis = record.txEpochMillis,
            exactAmount = record.resolvedExactAmount,
            transactionScope = LifetimeFinancialMemorySystem.classifyTransactionScope(
                category = record.category,
                type = record.type,
                note = record.note
            )
        )
    }
}

@Entity(tableName = "savings_goals")
data class GoalRoomEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val targetAmount: Long,
    val savedAmount: Long
) {
    fun toDomainModel(): SavingsGoalRecord = SavingsGoalRecord(
        id = id,
        userId = userId,
        title = title,
        targetAmount = targetAmount,
        savedAmount = savedAmount
    )

    companion object {
        fun fromDomainModel(record: SavingsGoalRecord): GoalRoomEntity = GoalRoomEntity(
            id = record.id,
            userId = record.userId,
            title = record.title,
            targetAmount = record.targetAmount,
            savedAmount = record.savedAmount
        )
    }
}

/**
 * Permanent Lifetime Financial Memory entity for Khata transactions.
 * INVARIANT: Historical records are NEVER hard-deleted; removals use soft-archive (`isArchived = 1`).
 */
@Entity(tableName = "khata_lifetime_ledger")
data class KhataLifetimeRoomEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val contactId: String,
    val contactName: String,
    val direction: String, // "LENA_HAI" | "DENA_HAI"
    val recordType: String, // "ORIGINAL_ENTRY" | "PARTIAL_PAYMENT" | "FULL_SETTLEMENT"
    val amount: Long,
    val description: String,
    val txDateMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val transactionScope: String = LifetimeFinancialMemorySystem.SCOPE_MONEY_MOVEMENT
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY txEpochMillis DESC")
    fun observeExpensesForUser(userId: String): Flow<List<ExpenseRoomEntity>>

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY txEpochMillis DESC")
    suspend fun getExpensesForUser(userId: String): List<ExpenseRoomEntity>

    /**
     * Strictly personal spending expenses — excludes lending/borrowing/Khata money movements.
     */
    @Query(
        "SELECT * FROM expenses WHERE userId = :userId AND type != 'income' AND transactionScope = 'personalspending' ORDER BY txEpochMillis DESC"
    )
    fun observePersonalSpendingForUser(userId: String): Flow<List<ExpenseRoomEntity>>

    @Query(
        "SELECT * FROM expenses WHERE userId = :userId AND type != 'income' AND transactionScope = 'personalspending' ORDER BY txEpochMillis DESC"
    )
    suspend fun getPersonalSpendingForUser(userId: String): List<ExpenseRoomEntity>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE userId = :userId AND type != 'income' AND transactionScope = 'personalspending' AND txEpochMillis BETWEEN :startMillis AND :endMillis"
    )
    suspend fun getPersonalSpendingTotalInRange(userId: String, startMillis: Long, endMillis: Long): Long

    /**
     * Strictly lending/borrowing/Khata money movements — isolated from personal spending calculations.
     */
    @Query(
        "SELECT * FROM expenses WHERE userId = :userId AND transactionScope = 'moneymovement' ORDER BY txEpochMillis DESC"
    )
    fun observeMoneyMovementsForUser(userId: String): Flow<List<ExpenseRoomEntity>>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE userId = :userId AND transactionScope = 'moneymovement' AND txEpochMillis BETWEEN :startMillis AND :endMillis"
    )
    suspend fun getMoneyMovementTotalInRange(userId: String, startMillis: Long, endMillis: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseRoomEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseRoomEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId AND userId = :userId")
    suspend fun deleteExpenseById(expenseId: String, userId: String)

    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun clearExpensesForUser(userId: String)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY title ASC")
    fun observeGoalsForUser(userId: String): Flow<List<GoalRoomEntity>>

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY title ASC")
    suspend fun getGoalsForUser(userId: String): List<GoalRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalRoomEntity)

    @Update
    suspend fun updateGoal(goal: GoalRoomEntity)

    @Query("UPDATE savings_goals SET savedAmount = :newSavedAmount WHERE id = :goalId AND userId = :userId")
    suspend fun updateGoalSavedAmount(goalId: String, userId: String, newSavedAmount: Long)

    @Query("DELETE FROM savings_goals WHERE id = :goalId AND userId = :userId")
    suspend fun deleteGoalById(goalId: String, userId: String)

    @Query("DELETE FROM savings_goals WHERE userId = :userId")
    suspend fun clearGoalsForUser(userId: String)
}

/**
 * Append-and-archive-only DAO for Lifetime Khata Financial Memory.
 * Intentionally exposes NO hard-delete SQL statement so historical Khata entries can never be wiped.
 */
@Dao
interface KhataLifetimeDao {
    @Query("SELECT * FROM khata_lifetime_ledger WHERE userId = :userId ORDER BY txDateMillis DESC")
    fun observeAllLifetimeKhataEntries(userId: String): Flow<List<KhataLifetimeRoomEntity>>

    @Query("SELECT * FROM khata_lifetime_ledger WHERE userId = :userId ORDER BY txDateMillis DESC")
    suspend fun getAllLifetimeKhataEntries(userId: String): List<KhataLifetimeRoomEntity>

    @Query("SELECT * FROM khata_lifetime_ledger WHERE userId = :userId AND isArchived = 0 AND isDeleted = 0 ORDER BY txDateMillis DESC")
    suspend fun getActiveKhataEntries(userId: String): List<KhataLifetimeRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: KhataLifetimeRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntries(entries: List<KhataLifetimeRoomEntity>)

    @Query(
        "UPDATE khata_lifetime_ledger SET isArchived = 1, updatedAtMillis = :updatedAtMillis WHERE id = :entryId AND userId = :userId"
    )
    suspend fun softArchiveEntry(entryId: String, userId: String, updatedAtMillis: Long)
}

@Database(
    entities = [
        ExpenseRoomEntity::class,
        GoalRoomEntity::class,
        KhataLifetimeRoomEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KharcheeDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao
    abstract fun khataLifetimeDao(): KhataLifetimeDao

    companion object {
        @Volatile
        private var INSTANCE: KharcheeDatabase? = null

        /**
         * Non-destructive incremental schema migration from v1 to v2:
         * Adds `exactAmount` (2-decimal paise precision) to `expenses` while preserving 100% of existing rows.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE expenses ADD COLUMN exactAmount REAL NOT NULL DEFAULT 0.0"
                )
                db.execSQL(
                    "UPDATE expenses SET exactAmount = CAST(amount AS REAL) WHERE exactAmount = 0.0"
                )
            }
        }

        /**
         * Non-destructive incremental schema migration from v2 to v3:
         * 1. Adds `transactionScope` ('personalspending' vs 'moneymovement') to `expenses` and backfills existing rows.
         * 2. Creates permanent soft-archive table `khata_lifetime_ledger` preserving all historical Khata transactions.
         */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE expenses ADD COLUMN transactionScope TEXT NOT NULL DEFAULT 'personalspending'"
                )
                db.execSQL(
                    """
                    UPDATE expenses
                    SET transactionScope = 'moneymovement'
                    WHERE LOWER(category) IN ('khata', 'lending', 'borrowing', 'loan', 'split', 'udhar', 'udhaar', 'settlement', 'lena hai', 'dena hai')
                       OR LOWER(type) IN ('moneymovement', 'lending', 'borrowing', 'khata')
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS khata_lifetime_ledger (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        contactId TEXT NOT NULL,
                        contactName TEXT NOT NULL,
                        direction TEXT NOT NULL,
                        recordType TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        txDateMillis INTEGER NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        transactionScope TEXT NOT NULL DEFAULT 'moneymovement'
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): KharcheeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KharcheeDatabase::class.java,
                    "kharchee_local_cache.db"
                )
                    // Safe incremental Migration policy: NEVER drop user tables on app updates.
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
