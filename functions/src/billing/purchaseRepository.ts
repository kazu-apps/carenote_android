import * as admin from "firebase-admin";

export interface PurchaseData {
  productId: string;
  isActive: boolean;
  expiryTimeMillis: string;
  autoRenewing: boolean;
}

export async function savePurchase(
  uid: string,
  purchaseToken: string,
  data: PurchaseData
): Promise<void> {
  await admin
    .firestore()
    .doc(`users/${uid}/purchases/${purchaseToken}`)
    .set({
      productId: data.productId,
      isActive: data.isActive,
      expiryTimeMillis: data.expiryTimeMillis,
      autoRenewing: data.autoRenewing,
      verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
}
