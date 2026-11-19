package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.isIncomeTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

private val MissionLime = Color(0xFFB7F34A)
private val MissionSurface = Color(0xFF151A18)
private val MissionMuted = Color(0xFF9AA49D)

private data class MissionDefinition(
    val id: String,
    val title: String,
    val tagline: String,
    val description: String,
    val xp: Int,
    val frequency: String
)

private data class MissionUiState(
    val totalXp: Int,
    val completedKeys: Set<String>,
    val completionDates: Set<String>
)

private object KharcheeMissionStore {
    private fun prefs(context: Context, userId: String) =
        context.getSharedPreferences("kharchee_missions_${userId.hashCode()}", Context.MODE_PRIVATE)

    fun read(context: Context, userId: String): MissionUiState {
        val p = prefs(context, userId)
        return MissionUiState(
            totalXp = p.getInt("total_xp", 0).coerceAtLeast(0),
            completedKeys = p.getStringSet("completed_keys", emptySet())?.toSet() ?: emptySet(),
            completionDates = p.getStringSet("completion_dates", emptySet())?.toSet() ?: emptySet()
        )
    }

    fun complete(context: Context, userId: String, key: String, xp: Int, today: String): MissionUiState {
        val p = prefs(context, userId)
        val current = read(context, userId)
        if (key in current.completedKeys) return current
        val nextKeys = current.completedKeys + key
        val nextDates = current.completionDates + today
        val nextXp = (current.totalXp + xp).coerceAtMost(1_000_000)
        p.edit()
            .putInt("total_xp", nextXp)
            .putStringSet("completed_keys", nextKeys)
            .putStringSet("completion_dates", nextDates)
            .apply()
        return MissionUiState(nextXp, nextKeys, nextDates)
    }
}

private fun missionPeriod(frequency: String, now: Calendar = Calendar.getInstance()): String {
    val formatter = when (frequency) {
        "DAILY" -> SimpleDateFormat("yyyyMMdd", Locale.US)
        "WEEKLY" -> SimpleDateFormat("yyyyww", Locale.US)
        else -> SimpleDateFormat("yyyyMM", Locale.US)
    }
    return formatter.format(now.time)
}

private fun missionLevel(xp: Int): Pair<String, Int> {
    val levels = listOf(
        0 to "INITIATE",
        100 to "OBSERVER",
        300 to "STRATEGIST",
        650 to "CONTROLLER",
        1100 to "ARCHITECT",
        1800 to "ELITE",
        2800 to "SOVEREIGN"
    )
    val current = levels.lastOrNull { xp >= it.first } ?: levels.first()
    val next = levels.firstOrNull { it.first > xp }?.first
    return current.second to (next ?: current.first)
}

@Composable
fun KharcheeMissionsSection(
    userId: String,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    onOpenJarvis: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()
    val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(now))
    val todayExpenseCount = remember(expenses, todayKey) {
        expenses.count { !isIncomeTransaction(it) && SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(it.txEpochMillis)) == todayKey }
    }
    val weekExpenseCount = remember(expenses) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        expenses.count { !isIncomeTransaction(it) && it.txEpochMillis >= start && it.txEpochMillis <= now }
    }
    var state by remember(userId) { mutableStateOf(KharcheeMissionStore.read(context, userId)) }

    val missions = remember(todayExpenseCount, weekExpenseCount, savingsGoals.size) {
        listOf(
            MissionDefinition("first_move", "FIRST MOVE", "Build the habit.", "Log at least one real expense today.", 20, "DAILY"),
            MissionDefinition("money_radar", "MONEY RADAR", "Know your numbers.", "Review today's spending and mark this mission complete.", 15, "DAILY"),
            MissionDefinition("zero_leak", "PROJECT ZERO LEAK", "Find the pattern.", "Review your week and identify one category to watch.", 30, "WEEKLY"),
            MissionDefinition("budget_lockdown", "BUDGET LOCKDOWN", "Give every rupee a direction.", "Check your weekly spending against your budget.", 25, "WEEKLY"),
            MissionDefinition("milestone", "MISSION: MILESTONE", "Progress over pressure.", if (savingsGoals.isEmpty()) "Create a savings goal when you are ready, or review what you want to work towards." else "Review the progress of one savings goal.", 25, "MONTHLY")
        )
    }

    LaunchedEffect(userId, todayExpenseCount, weekExpenseCount) {
        val autoMissions = listOf(
            Triple("first_move", todayExpenseCount > 0, "DAILY"),
            Triple("zero_leak", weekExpenseCount >= 5, "WEEKLY")
        )
        autoMissions.forEach { (id, eligible, frequency) ->
            if (eligible) {
                val definition = missions.first { it.id == id }
                val key = "${id}_${missionPeriod(frequency)}"
                state = KharcheeMissionStore.complete(context, userId, key, definition.xp, todayKey)
            }
        }
    }

    val totalXp = state.totalXp
    val (levelName, nextLevelXp) = missionLevel(totalXp)
    val currentLevelFloor = when (levelName) {
        "INITIATE" -> 0
        "OBSERVER" -> 100
        "STRATEGIST" -> 300
        "CONTROLLER" -> 650
        "ARCHITECT" -> 1100
        "ELITE" -> 1800
        else -> 2800
    }
    val progress = if (nextLevelXp <= currentLevelFloor) 1f else
        ((totalXp - currentLevelFloor).toFloat() / (nextLevelXp - currentLevelFloor)).coerceIn(0f, 1f)

    Card(
        modifier = modifier.fillMaxWidth().border(1.dp, MissionLime.copy(alpha = 0.28f), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MissionSurface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("KHARCHEE MISSIONS", color = MissionLime, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    Text("Small actions. Real progress.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$totalXp XP", color = MissionLime, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(levelName, color = MissionMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MissionLime,
                trackColor = Color.White.copy(alpha = 0.10f)
            )
            Text(if (nextLevelXp > currentLevelFloor) "${(nextLevelXp - totalXp).coerceAtLeast(0)} XP to next level" else "Top level reached", color = MissionMuted, fontSize = 11.sp)

            missions.forEach { mission ->
                val period = missionPeriod(mission.frequency)
                val completionKey = "${mission.id}_$period"
                val completed = completionKey in state.completedKeys
                Column(
                    Modifier.fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.035f), RoundedCornerShape(15.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(mission.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        Text(if (completed) "COMPLETED" else "+${mission.xp} XP", color = if (completed) MissionLime else MissionMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(mission.tagline, color = MissionLime, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(mission.description, color = MissionMuted, fontSize = 11.sp, lineHeight = 16.sp)
                    if (!completed) {
                        Button(
                            onClick = {
                                state = KharcheeMissionStore.complete(context, userId, completionKey, mission.xp, todayKey)
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MissionLime, contentColor = Color(0xFF101510))
                        ) {
                            Text("MARK REVIEW COMPLETE  ·  +${mission.xp} XP", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            Text("XP rewards consistency, not spending. No deposit or purchase is required.", color = MissionMuted, fontSize = 10.sp, lineHeight = 14.sp)
            Spacer(Modifier.height(1.dp))
            Button(
                onClick = { onOpenJarvis("Review my spending patterns using my actual KHARCHEE data. Suggest one realistic improvement without inventing savings.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White)
            ) {
                Text("ASK JARVIS FOR A MONEY REVIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
