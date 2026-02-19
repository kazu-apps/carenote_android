const mockSet = jest.fn().mockResolvedValue(undefined);
const mockDoc = jest.fn(() => ({ set: mockSet }));
const mockFirestore = jest.fn(() => ({ doc: mockDoc }));

const mockServerTimestamp = jest.fn(() => "SERVER_TIMESTAMP");

jest.mock("firebase-admin", () => ({
  initializeApp: jest.fn(),
  apps: [],
  firestore: Object.assign(mockFirestore, {
    FieldValue: {
      serverTimestamp: mockServerTimestamp,
    },
  }),
  credential: { applicationDefault: jest.fn() },
}));

import { savePurchase } from "../purchaseRepository";

describe("purchaseRepository", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("saves purchase data to Firestore with serverTimestamp", async () => {
    await savePurchase("test-uid", "test-token", {
      productId: "carenote_premium_monthly",
      isActive: true,
      expiryTimeMillis: "1735689600000",
      autoRenewing: true,
    });

    expect(mockSet).toHaveBeenCalledWith({
      productId: "carenote_premium_monthly",
      isActive: true,
      expiryTimeMillis: "1735689600000",
      autoRenewing: true,
      verifiedAt: "SERVER_TIMESTAMP",
    });
  });

  it("uses correct document path", async () => {
    await savePurchase("user-123", "token-abc", {
      productId: "carenote_premium_yearly",
      isActive: false,
      expiryTimeMillis: "0",
      autoRenewing: false,
    });

    expect(mockDoc).toHaveBeenCalledWith(
      "users/user-123/purchases/token-abc"
    );
  });
});
