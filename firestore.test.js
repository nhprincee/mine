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

