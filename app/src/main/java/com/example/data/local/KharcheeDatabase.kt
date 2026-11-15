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
    val exactAmount: Double = amount.toDouble()
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
            exactAmount = record.resolvedExactAmount
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

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY txEpochMillis DESC")
    fun observeExpensesForUser(userId: String): Flow<List<ExpenseRoomEntity>>

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY txEpochMillis DESC")
    suspend fun getExpensesForUser(userId: String): List<ExpenseRoomEntity>

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

@Database(
    entities = [ExpenseRoomEntity::class, GoalRoomEntity::class],
    version = 2,
    exportSchema = false
)
abstract class KharcheeDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao

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

        fun getInstance(context: Context): KharcheeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KharcheeDatabase::class.java,
                    "kharchee_local_cache.db"
                )
                    // Safe incremental Migration policy: NEVER drop user tables on app updates.
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
