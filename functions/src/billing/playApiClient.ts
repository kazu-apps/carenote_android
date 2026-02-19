import { google } from "googleapis";

export interface SubscriptionVerification {
  isActive: boolean;
  expiryTimeMillis: string;
  autoRenewing: boolean;
  paymentState: number;
}

export async function verifySubscription(
  packageName: string,
  productId: string,
  purchaseToken: string
): Promise<SubscriptionVerification> {
  const auth = await google.auth.getClient({
    scopes: ["https://www.googleapis.com/auth/androidpublisher"],
  });

  const androidpublisher = google.androidpublisher({
    version: "v3",
    auth,
  });

  try {
    const response =
      await androidpublisher.purchases.subscriptions.get({
        packageName,
        subscriptionId: productId,
        token: purchaseToken,
      });

    const data = response.data;
    const expiryTimeMillis = data.expiryTimeMillis ?? "0";
    const now = Date.now();
    const isActive = parseInt(expiryTimeMillis, 10) > now;

    return {
      isActive,
      expiryTimeMillis,
      autoRenewing: data.autoRenewing ?? false,
      paymentState: data.paymentState ?? 0,
    };
  } catch (error: unknown) {
    const message =
      error instanceof Error ? error.message : "Unknown Play API error";
    throw new Error(`Play API verification failed: ${message}`);
  }
}
