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

test("Unauthenticated user: cannot read members or referrals", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("members").get());
  await assertFails(unauthDb.collection("referrals").get());
});

test("Authenticated member: can create valid member profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("members").doc("alice_doc").set({
      id: "alice_doc",
      userId: ALICE_UID,
      name: "Nguyễn Văn A",
      company: "PCCC Toàn Thắng",
      industry: "Phòng Cháy Chữa Cháy",
      role: "MEMBER",
      email: "nva@toanthang.vn",
      phone: "0912176050",
    })
  );
});

test("Authenticated member: fails when creating invalid member profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("members").doc("bad_member").set({
      id: "bad_member",
      userId: ALICE_UID,
      name: "", // empty name
      company: "Company",
      industry: "Industry",
      role: "INVALID_ROLE",
      email: "bad@test.com",
      phone: "0123456789"
    })
  );
});

test("Authenticated member: can create valid referral", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("referrals").doc("ref_1").set({
      id: "ref_1",
      title: "Cung cấp hệ thống PCCC tòa nhà Vincom",
      giverMemberId: "alice_doc",
      takerMemberId: "bob_doc",
      clientName: "Trần Trọng Nghĩa",
      clientPhone: "0909123456",
      urgency: "HOT",
      status: "RECEIVED"
    })
  );
});

test("Authenticated member: can read chapter goals and kpi reports", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("chapter_goals").doc("month_10").set({
      id: "month_10",
      period: "MONTH",
      periodLabel: "Tháng 10/2026",
      targetTyfcbRevenue: 15000000000,
      currentTyfcbRevenue: 11500000000,
      targetReferrals: 30,
      currentReferrals: 24,
      targetMeetings121: 25,
      currentMeetings121: 18
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("chapter_goals").doc("month_10").get());
});
