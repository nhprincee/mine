const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read expenses", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/expenses`).get());
});

test("Authenticated user: cannot read another user's expense", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection(`users/${BOB_UID}/expenses`).doc("exp_bob").set({
      userId: BOB_UID,
      title: "Bob Chai",
      category: "Chai & Snacks",
      amount: 40,
      paymentMode: "UPI",
      type: "expense",
      note: "",
      txEpochMillis: 1730000000000,
      createdAt: new Date(1730000000000),
      updatedAt: new Date(1730000000000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection(`users/${BOB_UID}/expenses`).doc("exp_bob").get());
});

test("Authenticated user: can create and query own expenses with matching filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/expenses`).doc("exp_alice_1").set({
      userId: ALICE_UID,
      title: "Masala Chai & Bun Maska",
      category: "Chai & Snacks",
      amount: 68,
      paymentMode: "UPI",
      type: "expense",
      note: "Tapri with team",
      txEpochMillis: Date.now(),
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/expenses`).where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: fails shadow update with extra ghost field", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/expenses`).doc("exp_ghost").set({
      userId: ALICE_UID,
      title: "Chai",
      category: "Chai & Snacks",
      amount: 20,
      paymentMode: "UPI",
      type: "expense",
      note: "",
      txEpochMillis: Date.now(),
      createdAt: now,
      updatedAt: now,
      isAdmin: true,
    })
  );
});

test("Authenticated user: can create and update own UserSettings including Night Guard fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dailyLimit: 500,
      lockdownActive: false,
      autoLockdownAt90: true,
      stealthMode: false,
      themeId: "stark",
      myUpiId: "alice@okaxis",
      upiGuardTone: "hinglish",
      nightGuardEnabled: true,
      nightGuardStartHour: 22,
      nightGuardStartMinute: 0,
      nightGuardEndHour: 5,
      nightGuardEndMinute: 0,
      nightGuardCooldownEndMillis: 0,
      lastChallengeId: "math_1",
      enablePreSpendConfirm: true,
      notifyPaymentReminders: true,
      notifyNightGuardAlert: true,
      notifyMorningReview: false,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated user: can create and query own JARVIS messages and cannot read other users' JARVIS messages", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/jarvis_messages`).doc("msg_1").set({
      userId: ALICE_UID,
      role: "user",
      text: "Jarvis, mera aaj ka kharcha kitna hua?",
      mode: "roast",
      language: "hinglish",
      txEpochMillis: Date.now(),
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/jarvis_messages`).where("userId", "==", ALICE_UID).get()
  );

  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/jarvis_messages`).doc("msg_1").get()
  );
});

test("Authenticated user: can create and query own payment_reminders and night_guard_events", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/payment_reminders`).doc("rem_1").set({
      userId: ALICE_UID,
      title: "WiFi Broadband Bill",
      amount: 999,
      category: "Bills & Rent",
      dueEpochMillis: Date.now() + 86400000,
      intervalType: "1_day_before",
      isPaid: false,
      note: "Airtel Fiber",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/payment_reminders`).where("userId", "==", ALICE_UID).get()
  );

  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/payment_reminders`).doc("rem_1").get()
  );

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/night_guard_events`).doc("ng_1").set({
      userId: ALICE_UID,
      title: "Midnight Sneaker Drop",
      category: "Shopping",
      amount: 4200,
      outcome: "cancelled",
      bypassReason: "",
      challengeCategory: "Quick Mathematics",
      eventEpochMillis: Date.now(),
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/night_guard_events`).where("userId", "==", ALICE_UID).get()
  );

  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/night_guard_events`).doc("ng_1").get()
  );
});

test("Lifetime Khata Memory: owner can create, list, and update khata_contacts and khata_entries; cross-user access and hard delete are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const later = new Date(Date.now() - 1000);

  // 1. Create Khata Contact
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/khata_contacts`).doc("contact_aman_1").set({
      userId: ALICE_UID,
      contactId: "contact_aman_1",
      name: "Aman Verma",
      phone: "9876543210",
      avatarInitials: "AV",
      notes: "College friend",
      reminderEnabled: true,
      createdAtMillis: 1700000000000,
      updatedAtMillis: 1700000000000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 2. Create Original Khata Entry
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/khata_entries`).doc("entry_1").set({
      userId: ALICE_UID,
      entryId: "entry_1",
      contactId: "contact_aman_1",
      contactName: "Aman Verma",
      direction: "LENA_HAI",
      recordType: "ORIGINAL_ENTRY",
      parentEntryId: "",
      amount: 1000,
      txDateMillis: 1700000000000,
      dueDateMillis: 1700600000000,
      description: "Dinner & Movie Split",
      category: "Food & Outing",
      notes: "Paid via UPI",
      deleted: false,
      archived: false,
      edited: false,
      previousAmountBeforeEdit: -1,
      previousDescriptionBeforeEdit: "",
      editCount: 0,
      proofAttachmentUri: "",
      proofAttachmentLabel: "",
      idempotencyKey: "idem_entry_1",
      createdAtMillis: 1700000000000,
      updatedAtMillis: 1700000000000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 3. Partial update (Edit + Proof Vault attachment) preserves createdAtMillis
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/khata_entries`).doc("entry_1").update({
      amount: 1200,
      description: "Dinner, Movie & Cab Split",
      edited: true,
      previousAmountBeforeEdit: 1000,
      previousDescriptionBeforeEdit: "Dinner & Movie Split",
      editCount: 1,
      proofAttachmentUri: "content://media/external/images/media/42",
      proofAttachmentLabel: "UPI Receipt #42",
      updatedAtMillis: 1700050000000,
      updatedAt: later,
    })
  );

  // 4. Tampering with immutable createdAtMillis or direction is rejected
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/khata_entries`).doc("entry_1").update({
      createdAtMillis: 1710000000000,
      updatedAtMillis: 1710000000000,
      updatedAt: later,
    })
  );

  // 5. Hard deletion is strictly prohibited (soft-archive / soft-delete only)
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/khata_entries`).doc("entry_1").delete()
  );
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/khata_contacts`).doc("contact_aman_1").delete()
  );

  // 6. Cross-user isolation: Bob cannot read or modify Alice's Khata contacts or entries
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/khata_contacts`).doc("contact_aman_1").get()
  );
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/khata_entries`).doc("entry_1").get()
  );
});

test("Security 1: User cannot grant themselves Pro on profile creation or update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const later = new Date(Date.now() - 1000);

  // Attempt 1: Create profile with isPro: true -> MUST FAIL
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dailyLimit: 500,
      lockdownActive: false,
      autoLockdownAt90: true,
      stealthMode: false,
      themeId: "stark",
      myUpiId: "alice@okaxis",
      upiGuardTone: "hinglish",
      isPro: true,
      proTier: "free",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Create legitimate free profile -> SUCCEEDS
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dailyLimit: 500,
      lockdownActive: false,
      autoLockdownAt90: true,
      stealthMode: false,
      themeId: "stark",
      myUpiId: "alice@okaxis",
      upiGuardTone: "hinglish",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Attempt 2: Update profile to set isPro: true -> MUST FAIL
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isPro: true,
      updatedAt: later,
    })
  );
});

test("Security 2: User cannot extend their own Pro expiry (proExpiryMillis or proExpiresAtMillis)", async () => {
  const now = new Date(Date.now() - 5000);
  const later = new Date(Date.now() - 1000);

  // Seed user via Admin context with server-granted monthly Pro
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dailyLimit: 500,
      lockdownActive: false,
      autoLockdownAt90: true,
      stealthMode: false,
      themeId: "stark",
      myUpiId: "alice@okaxis",
      upiGuardTone: "hinglish",
      isPro: true,
      proTier: "gold",
      proExpiryMillis: 1750000000000,
      proExpiresAtMillis: 1750000000000,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Attempt to extend proExpiryMillis -> MUST FAIL
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      proExpiryMillis: 1999999999999,
      updatedAt: later,
    })
  );

  // Attempt to extend proExpiresAtMillis -> MUST FAIL
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      proExpiresAtMillis: 1999999999999,
      updatedAt: later,
    })
  );
});

test("Security 3: User cannot change their Pro tier (on create or update)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const later = new Date(Date.now() - 1000);

  // Attempt to create profile with proTier: 'platinum' -> MUST FAIL
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dailyLimit: 500,
      lockdownActive: false,
      autoLockdownAt90: true,
      stealthMode: false,
      themeId: "stark",
      myUpiId: "alice@okaxis",
      upiGuardTone: "hinglish",
      isPro: false,
      proTier: "platinum",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Create valid free profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dailyLimit: 500,
      lockdownActive: false,
      autoLockdownAt90: true,
      stealthMode: false,
      themeId: "stark",
      myUpiId: "alice@okaxis",
      upiGuardTone: "hinglish",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Attempt to update proTier to 'platinum' or 'gold' -> MUST FAIL
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      proTier: "platinum",
      updatedAt: later,
    })
  );
});

test("Security 4: Existing legitimate Pro user can update normal profile settings while preserving server-granted Pro membership", async () => {
  const now = new Date(Date.now() - 5000);
  const later = new Date(Date.now() - 1000);

  // Seed legitimate server-verified Sovereign Pro user
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dailyLimit: 1000,
      lockdownActive: false,
      autoLockdownAt90: true,
      stealthMode: false,
      themeId: "stark",
      myUpiId: "alice@okaxis",
      upiGuardTone: "hinglish",
      isPro: true,
      proTier: "platinum",
      proExpiryMillis: 1990000000000,
      proExpiresAtMillis: 1990000000000,
      lastVerifiedPaymentId: "pay_verified_999",
      nightGuardEnabled: true,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Normal profile settings update succeeds without touching Pro fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      dailyLimit: 1500,
      stealthMode: true,
      myUpiId: "alice.pro@okicici",
      updatedAt: later,
    })
  );
});

test("Security 5 & 7: Payment verification requests enforce ownership, pricing (₹30 / ₹299 INR), PENDING status, and reject duplicate idempotency keys", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const later = new Date(Date.now() - 1000);

  const verificationId = "pay_ord_lifetime_1_pay_98765";

  // 1. Valid first-time PENDING_SERVER_VERIFICATION request for ₹299 Lifetime Sovereign -> SUCCEEDS
  await assertSucceeds(
    aliceDb
      .collection(`users/${ALICE_UID}/payment_verifications`)
      .doc(verificationId)
      .set({
        userId: ALICE_UID,
        verificationId,
        orderId: "ord_lifetime_1",
        paymentId: "pay_98765",
        utrNumber: "432198765432",
        planId: "sovereign_lifetime",
        tierKey: "platinum",
        amountInr: 299,
        currency: "INR",
        deviceHash: "A1B2C3D4",
        status: "PENDING_SERVER_VERIFICATION",
        idempotencyKey: verificationId,
        createdAt: now,
        updatedAt: now,
      })
  );

  // 2. Duplicate payment verification with the same verificationId / idempotencyKey -> MUST FAIL
  await assertFails(
    aliceDb
      .collection(`users/${ALICE_UID}/payment_verifications`)
      .doc(verificationId)
      .set({
        userId: ALICE_UID,
        verificationId,
        orderId: "ord_lifetime_1",
        paymentId: "pay_98765",
        utrNumber: "432198765432",
        planId: "sovereign_lifetime",
        tierKey: "platinum",
        amountInr: 299,
        currency: "INR",
        deviceHash: "A1B2C3D4",
        status: "PENDING_SERVER_VERIFICATION",
        idempotencyKey: verificationId,
        createdAt: later,
        updatedAt: later,
      })
  );

  // 3. Client cannot self-mark status as "VERIFIED" on create or update -> MUST FAIL
  await assertFails(
    aliceDb
      .collection(`users/${ALICE_UID}/payment_verifications`)
      .doc("pay_forged_verified")
      .set({
        userId: ALICE_UID,
        verificationId: "pay_forged_verified",
        orderId: "ord_forged_1",
        paymentId: "pay_forged_1",
        utrNumber: "",
        planId: "sovereign_lifetime",
        tierKey: "platinum",
        amountInr: 299,
        currency: "INR",
        deviceHash: "A1B2C3D4",
        status: "VERIFIED",
        idempotencyKey: "pay_forged_verified",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertFails(
    aliceDb
      .collection(`users/${ALICE_UID}/payment_verifications`)
      .doc(verificationId)
      .update({
        status: "VERIFIED",
        updatedAt: later,
      })
  );

  // 4. Tampered amount (e.g. ₹1 for sovereign_lifetime) -> MUST FAIL
  await assertFails(
    aliceDb
      .collection(`users/${ALICE_UID}/payment_verifications`)
      .doc("pay_tampered_amount")
      .set({
        userId: ALICE_UID,
        verificationId: "pay_tampered_amount",
        orderId: "ord_tampered",
        paymentId: "pay_tampered",
        utrNumber: "",
        planId: "sovereign_lifetime",
        tierKey: "platinum",
        amountInr: 1,
        currency: "INR",
        deviceHash: "A1B2C3D4",
        status: "PENDING_SERVER_VERIFICATION",
        idempotencyKey: "pay_tampered_amount",
        createdAt: now,
        updatedAt: now,
      })
  );

  // 5. Cross-user isolation: Bob cannot read Alice's payment verification records
  await assertFails(
    bobDb
      .collection(`users/${ALICE_UID}/payment_verifications`)
      .doc(verificationId)
      .get()
  );
});


