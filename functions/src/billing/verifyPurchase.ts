import { onCall, HttpsError, CallableRequest } from "firebase-functions/v2/https";
import { logger } from "firebase-functions";
import * as admin from "firebase-admin";
import { verifySubscription } from "./playApiClient";
import { savePurchase } from "./purchaseRepository";

if (!admin.apps.length) {
  admin.initializeApp();
}

const PACKAGE_NAME = "com.carenote.original.app";

interface VerifyPurchaseRequest {
  purchaseToken: string;
  productId: string;
}

interface VerifyPurchaseResponse {
  isActive: boolean;
  productId: string;
  expiryTimeMillis: string;
  autoRenewing: boolean;
}

export const verifyPurchase = onCall(
  { region: "asia-northeast1" },
  async (
    request: CallableRequest<VerifyPurchaseRequest>
  ): Promise<VerifyPurchaseResponse> => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Authentication required"
      );
    }

    const { purchaseToken, productId } = request.data;

    if (!purchaseToken || !productId) {
      throw new HttpsError(
        "invalid-argument",
        "purchaseToken and productId are required"
      );
    }

    const uid = request.auth.uid;

    logger.info("Purchase verification started");

    // Replay prevention: check for existing verified purchase
    const existingDoc = await admin
      .firestore()
      .doc(`users/${uid}/purchases/${purchaseToken}`)
      .get();

    if (existingDoc.exists) {
      logger.info("Purchase already verified");
      const existing = existingDoc.data()!;
      return {
        isActive: existing.isActive ?? false,
        productId: existing.productId ?? productId,
        expiryTimeMillis: existing.expiryTimeMillis ?? "0",
        autoRenewing: existing.autoRenewing ?? false,
      };
    }

    // Verify with Google Play API
    const verification = await verifySubscription(
      PACKAGE_NAME,
      productId,
      purchaseToken
    );

    // Save to Firestore
    await savePurchase(uid, purchaseToken, {
      productId,
      isActive: verification.isActive,
      expiryTimeMillis: verification.expiryTimeMillis,
      autoRenewing: verification.autoRenewing,
    });

    logger.info("Purchase verification completed");

    return {
      isActive: verification.isActive,
      productId,
      expiryTimeMillis: verification.expiryTimeMillis,
      autoRenewing: verification.autoRenewing,
    };
  }
);
