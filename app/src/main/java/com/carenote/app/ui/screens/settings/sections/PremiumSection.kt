package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.BillingConnectionState
import com.carenote.app.domain.model.PremiumStatus
import com.carenote.app.domain.model.ProductInfo
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.theme.CareNoteTheme

@Composable
fun PremiumSection(
    premiumStatus: PremiumStatus,
    connectionState: BillingConnectionState,
    products: List<ProductInfo>,
    isLoading: Boolean,
    onPurchaseClick: (String) -> Unit,
    onRestoreClick: () -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.testTag(TestTags.PREMIUM_SECTION)) {
        SettingsSection(title = stringResource(R.string.settings_premium))
        PremiumStatusBadge(premiumStatus = premiumStatus)

        when {
            connectionState == BillingConnectionState.UNAVAILABLE -> {
                Text(
                    text = stringResource(R.string.billing_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
                        vertical = AppConfig.UI.PREFERENCE_VERTICAL_PADDING_DP.dp
                    )
                )
            }
            connectionState == BillingConnectionState.CONNECTING || isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = AppConfig.UI.PROGRESS_STROKE_WIDTH_DP.dp
                    )
                }
            }
            premiumStatus.isActive -> {
                ClickablePreference(
                    title = stringResource(R.string.billing_manage_subscription),
                    summary = stringResource(
                        R.string.billing_manage_subscription_summary
                    ),
                    onClick = onManageClick
                )
            }
            else -> {
                Text(
                    text = stringResource(R.string.billing_free_tier_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
                        vertical = AppConfig.UI.SMALL_SPACING_DP.dp
                    )
                )
                products.forEach { product ->
                    ProductCard(
                        product = product,
                        onPurchaseClick = { onPurchaseClick(product.productId) }
                    )
                }
                ClickablePreference(
                    title = stringResource(R.string.billing_restore_purchases),
                    summary = stringResource(
                        R.string.billing_restore_purchases_summary
                    ),
                    onClick = onRestoreClick,
                    modifier = Modifier.testTag(TestTags.PREMIUM_RESTORE_BUTTON)
                )
            }
        }
    }
}

@Composable
private fun PremiumStatusBadge(
    premiumStatus: PremiumStatus,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (premiumStatus) {
        is PremiumStatus.Active -> stringResource(
            R.string.billing_status_active
        ) to MaterialTheme.colorScheme.primary
        is PremiumStatus.Inactive -> stringResource(
            R.string.billing_status_inactive
        ) to MaterialTheme.colorScheme.outline
        is PremiumStatus.Expired -> stringResource(
            R.string.billing_status_expired
        ) to MaterialTheme.colorScheme.error
        is PremiumStatus.Pending -> stringResource(
            R.string.billing_status_pending
        ) to MaterialTheme.colorScheme.tertiary
    }

    Box(
        modifier = modifier
            .padding(
                horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
                vertical = AppConfig.UI.SMALL_SPACING_DP.dp
            )
            .testTag(TestTags.PREMIUM_STATUS_BADGE)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clip(
                    RoundedCornerShape(AppConfig.UI.SMALL_SPACING_DP.dp)
                )
                .background(color)
                .padding(
                    horizontal = AppConfig.UI.ITEM_SPACING_DP.dp,
                    vertical = AppConfig.UI.SMALL_SPACING_DP.dp
                )
        )
    }
}

@Composable
private fun ProductCard(
    product: ProductInfo,
    onPurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
                vertical = AppConfig.UI.PREFERENCE_VERTICAL_PADDING_DP.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = buildString {
                    append(product.formattedPrice)
                    val periodText = parseBillingPeriod(product.billingPeriod)
                    if (periodText.isNotEmpty()) {
                        append(" / ")
                        append(periodText)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(AppConfig.UI.ITEM_SPACING_DP.dp))
        Button(
            onClick = onPurchaseClick,
            modifier = Modifier.testTag(TestTags.PREMIUM_PURCHASE_BUTTON)
        ) {
            Text(text = stringResource(R.string.billing_purchase_button))
        }
    }
}

@Composable
private fun parseBillingPeriod(period: String): String {
    return when (period) {
        "P1M" -> stringResource(R.string.billing_period_month)
        "P1Y" -> stringResource(R.string.billing_period_year)
        "P3M" -> stringResource(R.string.billing_period_3months)
        "P6M" -> stringResource(R.string.billing_period_6months)
        else -> period
    }
}

@LightDarkPreview
@Composable
private fun PremiumSectionInactivePreview() {
    CareNoteTheme {
        PremiumSection(
            premiumStatus = PremiumStatus.Inactive,
            connectionState = BillingConnectionState.CONNECTED,
            products = listOf(
                ProductInfo(
                    "monthly", "Monthly Plan", "Billed monthly",
                    "480", 480_000_000L, "P1M"
                ),
                ProductInfo(
                    "yearly", "Yearly Plan", "Billed annually",
                    "4,800", 4_800_000_000L, "P1Y"
                )
            ),
            isLoading = false,
            onPurchaseClick = {},
            onRestoreClick = {},
            onManageClick = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun PremiumSectionActivePreview() {
    CareNoteTheme {
        PremiumSection(
            premiumStatus = PremiumStatus.Active(
                productId = "monthly",
                purchaseToken = "token",
                expiryTime = null,
                autoRenewing = true
            ),
            connectionState = BillingConnectionState.CONNECTED,
            products = emptyList(),
            isLoading = false,
            onPurchaseClick = {},
            onRestoreClick = {},
            onManageClick = {}
        )
    }
}
