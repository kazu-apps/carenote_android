const mockGet = jest.fn();
const mockSet = jest.fn().mockResolvedValue(undefined);
const mockDoc = jest.fn(() => ({ get: mockGet, set: mockSet }));
const mockFirestore = jest.fn(() => ({ doc: mockDoc }));

jest.mock("firebase-admin", () => ({
  initializeApp: jest.fn(),
  apps: [],
  firestore: Object.assign(mockFirestore, {
    FieldValue: {
      serverTimestamp: jest.fn(() => "SERVER_TIMESTAMP"),
    },
  }),
  credential: { applicationDefault: jest.fn() },
}));

const mockVerifySubscription = jest.fn();
jest.mock("../playApiClient", () => ({
  verifySubscription: mockVerifySubscription,
}));

const mockSavePurchase = jest.fn();
jest.mock("../purchaseRepository", () => ({
  savePurchase: mockSavePurchase,
}));

// Mock firebase-functions v2 https
const mockHttpsError = class extends Error {
  code: string;
  constructor(code: string, message: string) {
    super(message);
    this.code = code;
  }
};

jest.mock("firebase-functions/v2/https", () => ({
  onCall: jest.fn((_opts: unknown, handler: Function) => handler),
  HttpsError: mockHttpsError,
}));

jest.mock("firebase-functions", () => ({
  logger: {
    info: jest.fn(),
    error: jest.fn(),
    warn: jest.fn(),
  },
}));

// Import after mocks
import { verifyPurchase } from "../verifyPurchase";

describe("verifyPurchase", () => {
  // The exported verifyPurchase is the handler function itself (due to our mock)
  const handler = verifyPurchase as unknown as (
    request: {
      data: { purchaseToken?: string; productId?: string };
      auth?: { uid: string };
    }
  ) => Promise<unknown>;

  beforeEach(() => {
    jest.clearAllMocks();
    mockSavePurchase.mockResolvedValue(undefined);
  });

  it("returns unauthenticated error when no auth context", async () => {
    await expect(
      handler({
        data: { purchaseToken: "token", productId: "product" },
        auth: undefined,
      })
    ).rejects.toThrow("Authentication required");
  });

  it("returns invalid-argument when purchaseToken missing", async () => {
    await expect(
      handler({
        data: { purchaseToken: "", productId: "product" },
        auth: { uid: "user-1" },
      })
    ).rejects.toThrow("purchaseToken and productId are required");
  });

  it("returns invalid-argument when productId missing", async () => {
    await expect(
      handler({
        data: { purchaseToken: "token", productId: "" },
        auth: { uid: "user-1" },
      })
    ).rejects.toThrow("purchaseToken and productId are required");
  });

  it("returns existing data for replay (already verified purchase)", async () => {
    mockGet.mockResolvedValue({
      exists: true,
      data: () => ({
        isActive: true,
        productId: "carenote_premium_monthly",
        expiryTimeMillis: "1735689600000",
        autoRenewing: true,
      }),
    });

    const result = await handler({
      data: { purchaseToken: "existing-token", productId: "carenote_premium_monthly" },
      auth: { uid: "user-1" },
    });

    expect(result).toEqual({
      isActive: true,
      productId: "carenote_premium_monthly",
      expiryTimeMillis: "1735689600000",
      autoRenewing: true,
    });
    expect(mockVerifySubscription).not.toHaveBeenCalled();
    expect(mockSavePurchase).not.toHaveBeenCalled();
  });

  it("verifies and saves new purchase successfully", async () => {
    mockGet.mockResolvedValue({ exists: false });
    mockVerifySubscription.mockResolvedValue({
      isActive: true,
      expiryTimeMillis: "1735689600000",
      autoRenewing: true,
      paymentState: 1,
    });

    const result = await handler({
      data: { purchaseToken: "new-token", productId: "carenote_premium_monthly" },
      auth: { uid: "user-1" },
    });

    expect(result).toEqual({
      isActive: true,
      productId: "carenote_premium_monthly",
      expiryTimeMillis: "1735689600000",
      autoRenewing: true,
    });
    expect(mockVerifySubscription).toHaveBeenCalledWith(
      "com.carenote.app",
      "carenote_premium_monthly",
      "new-token"
    );
    expect(mockSavePurchase).toHaveBeenCalledWith("user-1", "new-token", {
      productId: "carenote_premium_monthly",
      isActive: true,
      expiryTimeMillis: "1735689600000",
      autoRenewing: true,
    });
  });

  it("returns error when Play API verification fails", async () => {
    mockGet.mockResolvedValue({ exists: false });
    mockVerifySubscription.mockRejectedValue(
      new Error("Play API verification failed")
    );

    await expect(
      handler({
        data: { purchaseToken: "bad-token", productId: "carenote_premium_monthly" },
        auth: { uid: "user-1" },
      })
    ).rejects.toThrow("Play API verification failed");
  });

  it("returns error when Firestore save fails", async () => {
    mockGet.mockResolvedValue({ exists: false });
    mockVerifySubscription.mockResolvedValue({
      isActive: true,
      expiryTimeMillis: "1735689600000",
      autoRenewing: true,
      paymentState: 1,
    });
    mockSavePurchase.mockRejectedValue(new Error("Firestore write failed"));

    await expect(
      handler({
        data: { purchaseToken: "token", productId: "carenote_premium_monthly" },
        auth: { uid: "user-1" },
      })
    ).rejects.toThrow("Firestore write failed");
  });
});
