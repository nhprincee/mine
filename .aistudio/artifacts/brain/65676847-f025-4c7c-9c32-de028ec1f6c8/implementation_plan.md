# Kharchee Architecture & Feature Implementation Plan

## Executive Summary
This plan details the end-to-end implementation of the requested core product features for **Kharchee**, transforming it into a CRED-inspired personal finance management platform with an intelligent, context-aware AI assistant (**JARVIS**). All features are implemented natively in Android Kotlin and Jetpack Compose, backed by Firebase Firestore and Firebase AI Logic (Gemini 2.5 Flash), adhering strictly to real data calculation with zero mock data.

---

## 1. Feature Breakdown & Requirements

### Feature 1 — JARVIS AI Assistant System
- **Context-Aware Analytics**:
  - Automatically calculate user financial metrics from actual `ExpenseRecord` entries (Total Balance, Today's Spend, This Week's Spend by Category, Top Outflow Categories, Daily Limit remaining, Khata net balances).
  - Inject structured financial snapshot directly into the Gemini prompt context system instructions without exposing sensitive credentials.
  - Implement deterministic anomaly & variance detection in Kotlin to identify surges (e.g. *"Food & Dining spend is 35% higher than last week's average"*).
- **Proactive Insights & Suggested Action Chips**:
  - Dynamically generate contextual suggestion chips both in the **Home Screen Proactive Insights Card** and within **JARVIS Chat** (e.g., *"Why did my food spending surge this week?"*, *"Analyze today's budget runway"*, *"Compare this month vs last month"*).
  - Generate daily financial summary reports on demand or as a proactive insight.
- **Robustness & Conversational Polish**:
  - Empty state handling when transactions are fewer than 3 entries, prompting sensible starter queries.
  - Real-time streaming response handling via Firebase AI Logic (`Firebase.ai`).
  - Text-to-Speech (TTS) integration with mute/replay controls and Android microphone Speech-to-Text (STT) input with runtime permission handling.

### Feature 2 — Configurable Daily Spending Limit
- **Accurate Local-Day Computation**:
  - Compute today's expenditures strictly using device local calendar start-of-day epoch millis (`ZoneId.systemDefault()`).
  - Compare actual total against `UserSettings.dailyLimit` (persisted in Firestore `users/{userId}`).
- **Visual Progress & Warning States**:
  - Safe State (< 70%): Muted Champagne Gold / Emerald progress.
  - Caution State (70% - 99%): Radiant Amber indicator.
  - Exceeded State (>= 100%): Crimson warning ring with overage amount displayed.
- **In-Place Modification**:
  - Interactive tap-to-edit dialog on the Home screen to modify the daily spending limit with immediate Firestore synchronization and real-time UI refresh.

### Feature 3 — CRED-Inspired Metallic Sovereign Card
- **Luxury Aesthetic**:
  - Brushed metallic dark titanium gradient (`#0C0D11` to `#1D2028`), subtle platinum hairline rim (`#C0C0C0`), and micro-chip emblem.
  - Masked account identifier (`•••• 2026`) and sovereign typography using Playfair Display for key financial balance highlights.
- **Dynamic Membership Binding**:
  - Live indicator for user status: "STANDARD MEMBER" vs "PRO TITANIUM ELITE" based on actual `UserSettings.isPro`.
  - Embedded discipline score ring and spending velocity status.

### Feature 4 — Dedicated Pro Dashboard & Tier Gating
- **Tier Persistence & Access Model**:
  - Add explicit Pro fields to `UserSettings`:
    ```kotlin
    val isPro: Boolean = false,
    val proTier: String = "free", // "free", "gold", "platinum"
    val proExpiryMillis: Long = 0L
    ```
  - Instant activation flow: `upgradeProPlan(tier: String, durationDays: Int)` writing directly to Firestore `users/{userId}`.
- **Dedicated Pro Analytics View (`ProVaultTab.kt`)**:
  - **Uncapped Analytics Dashboard**:
    - Category spending velocity and week-over-week variance breakdown.
    - Monthly spending trend curves and savings rate benchmarks.
    - Advanced JARVIS financial health audit report.
  - **Gated Tier Experience**:
    - For free users: Sophisticated preview with interactive tier cards ("PRO Gold Edition" @ ₹30/mo, "PRO Platinum Elite" @ ₹99/quarter) and instant one-tap upgrade.
    - For Pro users: Full unconstrained access to advanced analytics, priority JARVIS voice synthesis, and custom metallic HUD themes.

### Feature 5 — Ultra-Luxury Fintech Visual Refinements
- **Color Palette & Styling**:
  - Deep Obsidian Black (`#08090B`), Rich Graphite (`#141519`), Champagne Gold (`#D6B77A`), Platinum Silver (`#BFC3C9`), Emerald (`#35B58A`).
  - High-density spacing, refined card borders (0.5dp metallic outlines), and tactile ripple feedback.
  - Strict adherence to Playfair Display for headings and financial balances, with Inter/Roboto fallbacks.

---

## 2. Technical Architecture & Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    Firestore Database (Cloud)                   │
│  users/{uid} (UserSettings: dailyLimit, isPro, proTier)        │
│  expenses/{id} (ExpenseRecord: amount, category, epochMillis)   │
└────────────────────────────────┬────────────────────────────────┘
                                 │ Real-time Flow
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                     KharcheeRepository                          │
│  - observeExpenses(userId)                                      │
│  - observeSettings(userId)                                      │
│  - updateDailyLimit(userId, limit)                              │
│  - upgradeProStatus(userId, tier, days)                         │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                      KharcheeViewModel                          │
│  - Deterministic Proactive Insights Generator                   │
│  - Today's Spend & Daily Limit Percentage State                 │
│  - Category Variance & Spend Surges Engine                      │
│  - Jarvis Prompt Context Builder (Actual Data Injection)        │
└────────────────────────────────┬────────────────────────────────┘
                                 │
         ┌───────────────────────┼────────────────────────┐
         ▼                       ▼                        ▼
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────────┐
│     HomeTab      │   │  ProVaultTab     │   │ JarvisAssistantSheet │
│ - Proactive Card │   │ - Pro Analytics  │   │ - Dynamic Insights   │
│ - Metallic Card  │   │ - Tier Upgrades  │   │ - Contextual Q&A     │
│ - Daily Gauge    │   │ - Gated Features │   │ - Voice STT / TTS    │
└──────────────────┘   └──────────────────┘   └──────────────────────┘
```

---

## 3. Detailed File Modification Roadmap

| File Path | Modifications |
|---|---|
| `/app/src/main/java/com/example/model/KharcheeModels.kt` | Add `isPro: Boolean`, `proTier: String`, `proExpiryMillis: Long` to `UserSettings`. Add `ProactiveFinancialInsight` data class with action type, title, description, and suggested query. |
| `/app/src/main/java/com/example/data/KharcheeRepository.kt` | Add repository methods to update `dailyLimit` and `updateProMembership(tier, expiryMillis)`. Ensure Firestore rules compatibility. |
| `/app/src/main/java/com/example/ui/KharcheeViewModel.kt` | Implement: 1) `computeProactiveInsights(expenses, dailyLimit)` to detect surges (food, transit, shopping) and budget health. 2) Today's spend calculation with local midnight epoch. 3) Enhanced `buildFinancialContext()` for Gemini. 4) `upgradeProPlan()` action. |
| `/app/src/main/java/com/example/ui/components/CredBlackTitaniumCard.kt` | Enhance card with dynamic Pro badge, metallic sheen animation, and actual user discipline statistics. |
| `/app/src/main/java/com/example/ui/tabs/HomeTab.kt` | 1) Add Proactive Insight Banner Card at the top with one-tap action to ask JARVIS. 2) Enhance Daily Spending Limit Gauge with editable dialog. 3) Bind real Pro status to the metallic card. |
| `/app/src/main/java/com/example/ui/tabs/ProVaultTab.kt` | Redesign into full Pro Dashboard: Show active membership pass, spending velocity analytics, monthly financial trends, and interactive upgrade modal with instant activation. |
| `/app/src/main/java/com/example/ui/components/JarvisAssistantSheet.kt` | Display dynamic insight chips derived from actual financial variances. Provide quick prompts: "Summarize today's spend", "Explain my food spending", "How is my budget runway?". |
| `/app/src/test/java/com/example/KharcheeLogicTest.kt` | Add unit tests for insight calculation, today's spend computation, and Pro tier gating logic. |

---

## 4. Verification & Testing Strategy
1. **JVM Unit Tests (`gradle :app:testDebugUnitTest`)**:
   - Verify category spend surge detection logic with positive, negative, and zero variance.
   - Verify local calendar date bounds calculation for daily limit consumption.
   - Verify Pro tier duration and expiry calculations.
2. **Build Verification (`compile_applet`)**:
   - Verify error-free compilation of all Kotlin and Jetpack Compose files.
   - Verify testTag preservation for UI automated testing.
