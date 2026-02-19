import {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
  RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import { doc, getDoc, setDoc, updateDoc } from "firebase/firestore";
import * as fs from "fs";
import * as path from "path";

const PROJECT_ID = "carenote-rules-test";
const RULES_PATH = path.resolve(__dirname, "../../../../firebase/firestore.rules");

let testEnv: RulesTestEnvironment;

beforeAll(async () => {
  const rules = fs.readFileSync(RULES_PATH, "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: { rules, host: "127.0.0.1", port: 8080 },
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

afterEach(async () => {
  await testEnv.clearFirestore();
});

// Helper: seed membership document bypassing rules
async function seedMembership(userId: string, careRecipientId: string) {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(
      doc(db, "careRecipientMembers", `${userId}_${careRecipientId}`),
      { userId, careRecipientId, role: "owner", invitedBy: userId }
    );
  });
}

// Helper: seed careRecipient document bypassing rules
async function seedCareRecipient(careRecipientId: string) {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, "careRecipients", careRecipientId), {
      name: "Test Recipient",
      createdAt: new Date(),
    });
  });
}

// Helper: seed a membership doc with specific data
async function seedMembershipDoc(
  membershipId: string,
  data: Record<string, unknown>
) {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, "careRecipientMembers", membershipId), data);
  });
}

describe("Firestore Security Rules", () => {
  // A. Authentication tests
  describe("A. Authentication", () => {
    test("1. Unauthenticated users are denied access to all collections", async () => {
      const unauthed = testEnv.unauthenticatedContext().firestore();
      await assertFails(getDoc(doc(unauthed, "users", "user1")));
      await assertFails(getDoc(doc(unauthed, "careRecipients", "cr1")));
      await assertFails(getDoc(doc(unauthed, "careRecipientMembers", "m1")));
    });

    test("2. Authenticated user can read/write own users/{uid} document", async () => {
      const uid = "user1";
      const authed = testEnv.authenticatedContext(uid).firestore();
      await assertSucceeds(
        setDoc(doc(authed, "users", uid), { name: "Test User" })
      );
      await assertSucceeds(getDoc(doc(authed, "users", uid)));
    });
  });

  // B. careRecipients access control
  describe("B. careRecipients access control", () => {
    const uid = "user1";
    const careRecipientId = "cr1";

    test("3. Member can read careRecipient", async () => {
      await seedMembership(uid, careRecipientId);
      await seedCareRecipient(careRecipientId);
      const authed = testEnv.authenticatedContext(uid).firestore();
      await assertSucceeds(
        getDoc(doc(authed, "careRecipients", careRecipientId))
      );
    });

    test("4. Member can update careRecipient", async () => {
      await seedMembership(uid, careRecipientId);
      await seedCareRecipient(careRecipientId);
      const authed = testEnv.authenticatedContext(uid).firestore();
      await assertSucceeds(
        updateDoc(doc(authed, "careRecipients", careRecipientId), {
          name: "Updated",
        })
      );
    });

    test("5. Non-member is denied read on careRecipient", async () => {
      await seedCareRecipient(careRecipientId);
      const nonMember = testEnv.authenticatedContext("otherUser").firestore();
      await assertFails(
        getDoc(doc(nonMember, "careRecipients", careRecipientId))
      );
    });

    test("6. Non-member is denied write on careRecipient", async () => {
      await seedCareRecipient(careRecipientId);
      const nonMember = testEnv.authenticatedContext("otherUser").firestore();
      await assertFails(
        setDoc(doc(nonMember, "careRecipients", careRecipientId), {
          name: "Hacked",
        })
      );
    });
  });

  // C. Sub-collections
  describe("C. Sub-collections", () => {
    const uid = "user1";
    const careRecipientId = "cr1";

    test("7. Member can read/write sub-collection (medications)", async () => {
      await seedMembership(uid, careRecipientId);
      const authed = testEnv.authenticatedContext(uid).firestore();
      const medRef = doc(
        authed,
        "careRecipients",
        careRecipientId,
        "medications",
        "med1"
      );
      await assertSucceeds(setDoc(medRef, { name: "Aspirin" }));
      await assertSucceeds(getDoc(medRef));
    });

    test("8. Member can read/write nested sub-collection (medications/logs)", async () => {
      await seedMembership(uid, careRecipientId);
      const authed = testEnv.authenticatedContext(uid).firestore();
      const logRef = doc(
        authed,
        "careRecipients",
        careRecipientId,
        "medications",
        "med1",
        "logs",
        "log1"
      );
      await assertSucceeds(
        setDoc(logRef, { takenAt: new Date(), status: "taken" })
      );
      await assertSucceeds(getDoc(logRef));
    });

    test("9. Non-member is denied sub-collection access", async () => {
      const nonMember = testEnv.authenticatedContext("otherUser").firestore();
      const medRef = doc(
        nonMember,
        "careRecipients",
        careRecipientId,
        "medications",
        "med1"
      );
      await assertFails(setDoc(medRef, { name: "Aspirin" }));
      await assertFails(getDoc(medRef));
    });
  });

  // D. careRecipientMembers
  describe("D. careRecipientMembers", () => {
    const uid = "user1";
    const careRecipientId = "cr1";
    const membershipId = `${uid}_${careRecipientId}`;

    test("10. User can read own membership", async () => {
      await seedMembershipDoc(membershipId, {
        userId: uid,
        careRecipientId,
        role: "owner",
        invitedBy: uid,
      });
      const authed = testEnv.authenticatedContext(uid).firestore();
      await assertSucceeds(
        getDoc(doc(authed, "careRecipientMembers", membershipId))
      );
    });

    test("11. User cannot read another user's membership", async () => {
      const otherMembershipId = `otherUser_${careRecipientId}`;
      await seedMembershipDoc(otherMembershipId, {
        userId: "otherUser",
        careRecipientId,
        role: "member",
        invitedBy: uid,
      });
      const authed = testEnv.authenticatedContext(uid).firestore();
      await assertFails(
        getDoc(doc(authed, "careRecipientMembers", otherMembershipId))
      );
    });

    test("12. User with invitedBy == uid can create membership", async () => {
      const newMembershipId = `newUser_${careRecipientId}`;
      const authed = testEnv.authenticatedContext(uid).firestore();
      await assertSucceeds(
        setDoc(doc(authed, "careRecipientMembers", newMembershipId), {
          userId: "newUser",
          careRecipientId,
          role: "member",
          invitedBy: uid,
        })
      );
    });
  });

  // E. Purchases
  describe("E. purchases", () => {
    const uid = "user1";

    test("13. User can read own purchases", async () => {
      // Seed a purchase document
      await testEnv.withSecurityRulesDisabled(async (context) => {
        const db = context.firestore();
        await setDoc(doc(db, "users", uid, "purchases", "token1"), {
          productId: "premium",
          purchaseTime: new Date(),
        });
      });
      const authed = testEnv.authenticatedContext(uid).firestore();
      await assertSucceeds(
        getDoc(doc(authed, "users", uid, "purchases", "token1"))
      );
    });

    test("14. Client-side write to purchases is denied", async () => {
      const authed = testEnv.authenticatedContext(uid).firestore();
      await assertFails(
        setDoc(doc(authed, "users", uid, "purchases", "token1"), {
          productId: "premium",
          purchaseTime: new Date(),
        })
      );
    });
  });
});
