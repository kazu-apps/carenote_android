import { verifySubscription } from "../playApiClient";

const mockGet = jest.fn();

jest.mock("googleapis", () => ({
  google: {
    auth: {
      getClient: jest.fn().mockResolvedValue({}),
    },
    androidpublisher: jest.fn(() => ({
      purchases: {
        subscriptions: {
          get: mockGet,
        },
      },
    })),
  },
}));

describe("playApiClient", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("returns active subscription for valid purchase", async () => {
    const futureExpiry = String(Date.now() + 30 * 24 * 60 * 60 * 1000);
    mockGet.mockResolvedValue({
      data: {
        expiryTimeMillis: futureExpiry,
        autoRenewing: true,
        paymentState: 1,
      },
    });

    const result = await verifySubscription(
      "com.carenote.app",
      "carenote_premium_monthly",
      "test_token"
    );

    expect(result.isActive).toBe(true);
    expect(result.expiryTimeMillis).toBe(futureExpiry);
    expect(result.autoRenewing).toBe(true);
    expect(result.paymentState).toBe(1);
  });

  it("returns expired subscription", async () => {
    const pastExpiry = String(Date.now() - 24 * 60 * 60 * 1000);
    mockGet.mockResolvedValue({
      data: {
        expiryTimeMillis: pastExpiry,
        autoRenewing: false,
        paymentState: 0,
      },
    });

    const result = await verifySubscription(
      "com.carenote.app",
      "carenote_premium_monthly",
      "test_token"
    );

    expect(result.isActive).toBe(false);
    expect(result.autoRenewing).toBe(false);
  });

  it("returns cancelled subscription", async () => {
    const futureExpiry = String(Date.now() + 7 * 24 * 60 * 60 * 1000);
    mockGet.mockResolvedValue({
      data: {
        expiryTimeMillis: futureExpiry,
        autoRenewing: false,
        paymentState: 1,
        cancelReason: 0,
      },
    });

    const result = await verifySubscription(
      "com.carenote.app",
      "carenote_premium_monthly",
      "test_token"
    );

    expect(result.isActive).toBe(true);
    expect(result.autoRenewing).toBe(false);
  });

  it("handles API error gracefully", async () => {
    mockGet.mockRejectedValue(new Error("API quota exceeded"));

    await expect(
      verifySubscription(
        "com.carenote.app",
        "carenote_premium_monthly",
        "test_token"
      )
    ).rejects.toThrow("Play API verification failed: API quota exceeded");
  });

  it("handles network error", async () => {
    mockGet.mockRejectedValue(new Error("ECONNREFUSED"));

    await expect(
      verifySubscription(
        "com.carenote.app",
        "carenote_premium_monthly",
        "test_token"
      )
    ).rejects.toThrow("Play API verification failed: ECONNREFUSED");
  });
});
