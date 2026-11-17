/**
 * KHARCHEE — Trusted Server-Side Payment Verification & Pro Entitlement Cloud Functions
 *
 * SECURITY GUARANTEES:
 * 1. Ordinary authenticated clients can NEVER write `isPro`, `proTier`, `proExpiryMillis`,
 *    or `proExpiresAtMillis` on `/users/{userId}` (blocked by Firestore Rules).
 * 2. Only these trusted Cloud Functions (running with Firebase Admin SDK privileges)
 *    can grant, renew, or revoke Pro access after verifying payment authenticity.
 * 3. Secret API keys and HMAC secrets (`PAYMENT_PROVIDER_KEY_SECRET`, `PAYMENT_WEBHOOK_SECRET`)
 *    are loaded exclusively from Google Cloud Secret Manager (`defineSecret`), never stored in client code.
 * 4. Enforces strict plan pricing (`₹30` monthly starter, `₹299` lifetime sovereign), currency (`INR`),
 *    and transactional idempotency locks (`/payment_idempotency_locks/{paymentOrOrderId}`) so a single
 *    payment can never be replayed to grant duplicate entitlements.
 */

const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");
const crypto = require("node:crypto");

admin.initializeApp();
const db = admin.firestore();

const PAYMENT_PROVIDER_KEY_SECRET = defineSecret("PAYMENT_PROVIDER_KEY_SECRET");
const PAYMENT_WEBHOOK_SECRET = defineSecret("PAYMENT_WEBHOOK_SECRET");

const PLAN_CATALOG = Object.freeze({
  starter_monthly: {
    planId: "starter_monthly",
    tierKey: "gold",
    amountInr: 30,
    amountPaise: 3000,
    currency: "INR",
    durationDays: 30,
  },
  sovereign_lifetime: {
    planId: "sovereign_lifetime",
    tierKey: "platinum",
    amountInr: 299,
    amountPaise: 29900,
    currency: "INR",
    durationDays: 36500,
  },
});

/**
 * Constant-time HMAC-SHA256 verification for payment provider signatures
 * (compatible with Razorpay `order_id|payment_id` and webhook payload signatures).
 */
function verifyHmacSha256Signature(payloadString, providedSignatureHex, secretValue) {
  if (!payloadString || !providedSignatureHex || !secretValue) {
    return false;
  }
  const expectedHex = crypto
    .createHmac("sha256", secretValue)
    .update(payloadString, "utf8")
    .digest("hex");
  const expectedBuf = Buffer.from(expectedHex, "utf8");
  const providedBuf = Buffer.from(String(providedSignatureHex).trim(), "utf8");
  if (expectedBuf.length !== providedBuf.length) {
    return false;
  }
  return crypto.timingSafeEqual(expectedBuf, providedBuf);
}

/**
 * Atomically verifies idempotency and grants or renews Pro membership on `/users/{userId}`.
 */
async function grantVerifiedProEntitlementTransaction({
  userId,
  verificationId,
  orderId,
  paymentId,
  planConfig,
  verificationSource,
}) {
  const lockId = `lock_${orderId}_${paymentId || verificationId}`.replace(/[^a-zA-Z0-9_\-]/g, "_");
  const lockRef = db.collection("payment_idempotency_locks").doc(lockId);
  const userRef = db.collection("users").doc(userId);
  const verificationRef = userRef.collection("payment_verifications").doc(verificationId);

  return db.runTransaction(async (tx) => {
    const [lockSnap, userSnap, verificationSnap] = await Promise.all([
      tx.get(lockRef),
      tx.get(userRef),
      tx.get(verificationRef),
    ]);

    if (lockSnap.exists) {
      throw new HttpsError(
        "already-exists",
        `Duplicate payment verification rejected: lock ${lockId} has already been processed.`
      );
    }

    if (verificationSnap.exists && verificationSnap.data().status === "VERIFIED") {
      throw new HttpsError(
        "already-exists",
        `Verification request ${verificationId} was already verified.`
      );
    }

    const nowMillis = Date.now();
    const existingData = userSnap.exists ? userSnap.data() : {};
    const existingExpiry = Math.max(
      Number(existingData.proExpiryMillis || 0),
      Number(existingData.proExpiresAtMillis || 0)
    );
    const renewalBaseMillis = existingExpiry > nowMillis ? existingExpiry : nowMillis;
    const newExpiryMillis =
      planConfig.durationDays * 24 * 60 * 60 * 1000 + renewalBaseMillis;

    tx.set(lockRef, {
      lockId,
      userId,
      verificationId,
      orderId,
      paymentId: paymentId || "",
      planId: planConfig.planId,
      tierKey: planConfig.tierKey,
      amountInr: planConfig.amountInr,
      currency: planConfig.currency,
      verificationSource,
      processedAtMillis: nowMillis,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    if (verificationSnap.exists) {
      tx.update(verificationRef, {
        status: "VERIFIED",
        paymentId: paymentId || verificationSnap.data().paymentId || "",
        verifiedAtMillis: nowMillis,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    const proEntitlementFields = {
      isPro: true,
      proTier: planConfig.tierKey,
      proExpiryMillis: newExpiryMillis,
      proExpiresAtMillis: newExpiryMillis,
      lastVerifiedPaymentId: paymentId || orderId,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    if (!userSnap.exists) {
      tx.set(userRef, {
        userId,
        dailyLimit: 500,
        lockdownActive: false,
        autoLockdownAt90: true,
        stealthMode: false,
        themeId: "stark",
        myUpiId: "",
        upiGuardTone: "hinglish",
        nightGuardEnabled: true,
        nightGuardStartHour: 22,
        nightGuardStartMinute: 0,
        nightGuardEndHour: 5,
        nightGuardEndMinute: 0,
        nightGuardCooldownEndMillis: 0,
        lastChallengeId: "",
        enablePreSpendConfirm: false,
        notifyPaymentReminders: false,
        notifyNightGuardAlert: false,
        notifyMorningReview: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        ...proEntitlementFields,
      });
    } else {
      tx.update(userRef, proEntitlementFields);
    }

    return {
      status: "VERIFIED",
      userId,
      planId: planConfig.planId,
      proTier: planConfig.tierKey,
      proExpiryMillis: newExpiryMillis,
    };
  });
}

/**
 * 1. Callable Cloud Function: Verifies a signed payment order/checkout response from the Android client.
 */
exports.verifyKharcheeProPayment = onCall(
  { secrets: [PAYMENT_PROVIDER_KEY_SECRET] },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "User must be authenticated to verify a payment.");
    }

    const uid = request.auth.uid;
    const {
      userId,
      verificationId,
      orderId,
      paymentId,
      providerSignature,
      planId,
      amountInr,
      currency,
    } = request.data || {};

    if (!userId || userId !== uid) {
      throw new HttpsError("permission-denied", "Payment verification userId must match authenticated caller.");
    }

    const planConfig = PLAN_CATALOG[planId];
    if (!planConfig) {
      throw new HttpsError("invalid-argument", `Unsupported Pro planId: ${planId}`);
    }

    if (Number(amountInr) !== planConfig.amountInr || currency !== "INR") {
      throw new HttpsError(
        "invalid-argument",
        `Payment amount/currency mismatch for ${planId}. Expected ₹${planConfig.amountInr} INR.`
      );
    }

    const secretKey = PAYMENT_PROVIDER_KEY_SECRET.value();
    if (!secretKey) {
      throw new HttpsError(
        "failed-precondition",
        "Backend payment verification credentials (PAYMENT_PROVIDER_KEY_SECRET) are not configured."
      );
    }

    const signaturePayload = `${orderId}|${paymentId}`;
    const isSignatureValid = verifyHmacSha256Signature(
      signaturePayload,
      providerSignature,
      secretKey
    );

    if (!isSignatureValid) {
      throw new HttpsError("permission-denied", "Invalid payment provider cryptographic signature.");
    }

    return grantVerifiedProEntitlementTransaction({
      userId: uid,
      verificationId: verificationId || `pay_${orderId}_${paymentId}`,
      orderId,
      paymentId,
      planConfig,
      verificationSource: "CALLABLE_SIGNATURE_VERIFICATION",
    });
  }
);

/**
 * 2. Webhook Endpoint: Verifies server-to-server payment provider webhooks (e.g. `payment.captured`).
 */
exports.paymentProviderWebhook = onRequest(
  { secrets: [PAYMENT_WEBHOOK_SECRET] },
  async (req, res) => {
    if (req.method !== "POST") {
      res.status(405).json({ error: "Method Not Allowed" });
      return;
    }

    const webhookSecret = PAYMENT_WEBHOOK_SECRET.value();
    if (!webhookSecret) {
      res.status(503).json({ error: "Webhook verification secret not configured" });
      return;
    }

    const signatureHeader = req.get("x-razorpay-signature") || req.get("x-webhook-signature") || "";
    const rawBody = req.rawBody ? req.rawBody.toString("utf8") : JSON.stringify(req.body || {});

    if (!verifyHmacSha256Signature(rawBody, signatureHeader, webhookSecret)) {
      res.status(401).json({ error: "Invalid webhook signature" });
      return;
    }

    const event = req.body || {};
    const paymentEntity = event?.payload?.payment?.entity;
    if (!paymentEntity || paymentEntity.status !== "captured") {
      res.status(200).json({ status: "ignored_non_captured_event" });
      return;
    }

    const notes = paymentEntity.notes || {};
    const userId = notes.userId;
    const planId = notes.planId;
    const verificationId = notes.verificationId || `pay_${paymentEntity.order_id}_${paymentEntity.id}`;
    const planConfig = PLAN_CATALOG[planId];

    if (
      !userId ||
      !planConfig ||
      Number(paymentEntity.amount) !== planConfig.amountPaise ||
      paymentEntity.currency !== "INR"
    ) {
      res.status(400).json({ error: "Invalid payment metadata, amount, or currency" });
      return;
    }

    try {
      const result = await grantVerifiedProEntitlementTransaction({
        userId,
        verificationId,
        orderId: paymentEntity.order_id,
        paymentId: paymentEntity.id,
        planConfig,
        verificationSource: "PROVIDER_WEBHOOK",
      });
      res.status(200).json(result);
    } catch (err) {
      if (err.code === "already-exists") {
        res.status(200).json({ status: "already_processed_idempotent" });
        return;
      }
      res.status(500).json({ error: err.message || "Internal verification failure" });
    }
  }
);

/**
 * 3. Firestore Trigger: Audits newly created `/users/{userId}/payment_verifications/{verificationId}` documents.
 * Leaves the request in `PENDING_SERVER_VERIFICATION` until a verified provider signature or webhook arrives.
 */
exports.onPaymentVerificationRequested = onDocumentCreated(
  "users/{userId}/payment_verifications/{verificationId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;
    const data = snap.data();
    const planConfig = PLAN_CATALOG[data.planId];
    if (
      !planConfig ||
      data.amountInr !== planConfig.amountInr ||
      data.currency !== "INR"
    ) {
      await snap.ref.update({
        status: "REJECTED_INVALID_PLAN_OR_AMOUNT",
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }
  }
);

/**
 * 4. Scheduled Expiry Enforcement: Revokes expired monthly Pro memberships daily.
 */
exports.revokeExpiredProMemberships = onSchedule("every 24 hours", async () => {
  const nowMillis = Date.now();
  const expiredQuery = await db
    .collection("users")
    .where("isPro", "==", true)
    .where("proExpiryMillis", ">", 0)
    .where("proExpiryMillis", "<", nowMillis)
    .get();

  const batch = db.batch();
  expiredQuery.docs.forEach((doc) => {
    const data = doc.data();
    if (data.proTier !== "platinum" && data.proTier !== "sovereign") {
      batch.update(doc.ref, {
        isPro: false,
        proTier: "free",
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }
  });

  if (!expiredQuery.empty) {
    await batch.commit();
  }
});
