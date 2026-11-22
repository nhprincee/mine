package com.example.data.model

/**
 * Catalog of KHARCHEE premium features gated by server-verified Firestore entitlements
 * stored on `/users/{uid}` (`isPro`, `proTier`, `proExpiryMillis`, `proExpiresAtMillis`).
 */
enum class PremiumFeature(
    val id: String,
    val title: String,
    val subtitle: String,
    val highlightBullets: List<String>
) {
    ADVANCED_ANALYTICS(
        id = "advanced_analytics",
        title = "Advanced Analytics Suite",
        subtitle = "Cashflow spline curves, 30-day intensity heatmap & burn-rate speedometers.",
        highlightBullets = listOf(
            "TradingView-style cumulative cashflow spline with last-month comparison",
            "30-day spending intensity heatmap matrix & no-spend streak tracking",
            "Budget burn-rate speedometer & top 5 money drainers leaderboard"
        )
    ),
    CUSTOM_DATE_FILTERS(
        id = "custom_date_filters",
        title = "Extended & Custom Date Windows",
        subtitle = "Analyze Last 3 Months, Full Year, or any custom date range.",
        highlightBullets = listOf(
            "Multi-quarter trend visibility across 90 days and 12 months",
            "Custom start-to-end date range analytics for trips or audits",
            "Period-over-period variance comparisons"
        )
    ),
    REPORT_EXPORTS(
        id = "report_exports",
        title = "PDF & CSV Statement Exports",
        subtitle = "Export structured monthly statements and CA-ready CSV spreadsheets.",
        highlightBullets = listOf(
            "One-tap formatted Monthly Financial PDF statements",
            "Raw CSV transaction exports for Excel & Google Sheets",
            "Instant share and save-to-device support"
        )
    ),
    PRO_VAULT_STUDIOS(
        id = "pro_vault_studios",
        title = "Sovereign Pro Studios",
        subtitle = "Secret Ghost Vault, Spare-Change Auto-Sweep & executive HUD themes.",
        highlightBullets = listOf(
            "4-digit PIN isolated Secret Ghost Vault mini-ledger",
            "Automated spare-change sweep into savings goals",
            "Exclusive Obsidian, Titanium & Mark 85 HUD themes"
        )
    )
}

enum class PremiumLockReason {
    FREE_TIER,
    SUBSCRIPTION_EXPIRED,
    PENDING_VERIFICATION,
    VERIFYING_ENTITLEMENT,
    FIRESTORE_ERROR
}

sealed interface PremiumAccessDecision {
    data class Granted(
        val tier: String,
        val expiryMillis: Long,
        val isLifetime: Boolean
    ) : PremiumAccessDecision

    data class Locked(
        val feature: PremiumFeature,
        val reason: PremiumLockReason,
        val ctaTitle: String,
        val ctaMessage: String
    ) : PremiumAccessDecision
}

/**
 * Immutable UI-ready representation of the user's real-time Firestore Pro entitlement.
 *
 * Zero-trust invariant:
 * - Derived exclusively from Firestore `/users/{uid}` snapshot fields (`isPro`, `proTier`,
 *   `proExpiryMillis`, `proExpiresAtMillis`) which are protected from client writes by `firestore.rules`.
 * - Fails closed (`isProActive = false`) while loading, on Firestore error, or when expired.
 */
data class FirestorePremiumEntitlementState(
    val isLoading: Boolean = false,
    val isSyncedFromFirestore: Boolean = false,
    val isProActive: Boolean = false,
    val proTier: String = "free",
    val effectiveExpiryMillis: Long = 0L,
    val isExpired: Boolean = false,
    val hasPendingVerification: Boolean = false,
    val errorMessage: String? = null
) {
    val isLifetime: Boolean
        get() {
            val normalized = proTier.trim().lowercase()
            return isProActive &&
                (normalized == "platinum" || normalized == "sovereign") &&
                effectiveExpiryMillis == 0L
        }

    fun statusBadgeLabel(): String = when {
        isProActive && isLifetime -> "PRO LIFETIME"
        isProActive -> "PRO ${proTier.uppercase()}"
        hasPendingVerification -> "PENDING VERIFICATION"
        isExpired -> "PRO EXPIRED"
        isLoading -> "SYNCING PLAN"
        else -> "FREE PLAN"
    }

    fun isFeatureUnlocked(
        feature: PremiumFeature,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean = evaluateAccess(feature, nowMillis) is PremiumAccessDecision.Granted

    fun evaluateAccess(
        feature: PremiumFeature,
        nowMillis: Long = System.currentTimeMillis()
    ): PremiumAccessDecision {
        if (isProActive) {
            val stillValid = isLifetime || effectiveExpiryMillis > nowMillis
            if (stillValid) {
                return PremiumAccessDecision.Granted(
                    tier = proTier,
                    expiryMillis = effectiveExpiryMillis,
                    isLifetime = isLifetime
                )
            }
            return PremiumAccessDecision.Locked(
                feature = feature,
                reason = PremiumLockReason.SUBSCRIPTION_EXPIRED,
                ctaTitle = "Renew Pro Access",
                ctaMessage = "Your KHARCHEE Pro plan has expired. Upgrade to Pro to restore ${feature.title}."
            )
        }

        return when {
            hasPendingVerification -> PremiumAccessDecision.Locked(
                feature = feature,
                reason = PremiumLockReason.PENDING_VERIFICATION,
                ctaTitle = "View Verification Status",
                ctaMessage = "Your Pro payment verification is pending in Firestore. ${feature.title} unlocks automatically once verified."
            )
            isExpired -> PremiumAccessDecision.Locked(
                feature = feature,
                reason = PremiumLockReason.SUBSCRIPTION_EXPIRED,
                ctaTitle = "Renew Pro Access",
                ctaMessage = "Your previous Pro subscription expired. Renew Pro to continue using ${feature.title}."
            )
            isLoading && !isSyncedFromFirestore -> PremiumAccessDecision.Locked(
                feature = feature,
                reason = PremiumLockReason.VERIFYING_ENTITLEMENT,
                ctaTitle = "Upgrade to Pro",
                ctaMessage = "Checking your Firestore Pro entitlement..."
            )
            !errorMessage.isNullOrBlank() -> PremiumAccessDecision.Locked(
                feature = feature,
                reason = PremiumLockReason.FIRESTORE_ERROR,
                ctaTitle = "Upgrade to Pro",
                ctaMessage = "Could not verify Pro status from Firestore right now. Check your connection or view Pro plans."
            )
            else -> PremiumAccessDecision.Locked(
                feature = feature,
                reason = PremiumLockReason.FREE_TIER,
                ctaTitle = "Upgrade to Pro",
                ctaMessage = "Unlock ${feature.title} and full financial telemetry with KHARCHEE Pro."
            )
        }
    }
}

/**
 * Pure, deterministic conditional logic engine that translates Firestore `/users/{uid}`
 * snapshots and pending verification state into a fail-closed [FirestorePremiumEntitlementState].
 */
object PremiumEntitlementEvaluator {

    fun evaluate(
        remoteSettings: UserSettings?,
        isSyncedFromFirestore: Boolean,
        isLoading: Boolean = false,
        hasPendingVerification: Boolean = false,
        errorMessage: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): FirestorePremiumEntitlementState {
        if (remoteSettings == null) {
            return FirestorePremiumEntitlementState(
                isLoading = isLoading,
                isSyncedFromFirestore = isSyncedFromFirestore,
                isProActive = false,
                proTier = "free",
                effectiveExpiryMillis = 0L,
                isExpired = false,
                hasPendingVerification = hasPendingVerification,
                errorMessage = errorMessage
            )
        }

        val normalizedTier = remoteSettings.proTier.trim().lowercase().ifEmpty { "free" }
        val expiry = remoteSettings.effectiveProExpiryMillis
        val isActive = remoteSettings.hasActiveServerVerifiedPro(nowMillis)
        val wasPaidTier = normalizedTier != "free"
        val isExpired = !isActive && wasPaidTier && expiry > 0L && expiry <= nowMillis

        return FirestorePremiumEntitlementState(
            isLoading = false,
            isSyncedFromFirestore = isSyncedFromFirestore,
            isProActive = isActive,
            proTier = if (isActive) normalizedTier else "free",
            effectiveExpiryMillis = expiry,
            isExpired = isExpired,
            hasPendingVerification = hasPendingVerification && !isActive,
            errorMessage = errorMessage
        )
    }
}
