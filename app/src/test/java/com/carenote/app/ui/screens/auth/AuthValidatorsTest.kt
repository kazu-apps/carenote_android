package com.carenote.app.ui.screens.auth

import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.common.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthValidatorsTest {

    // ====== validateEmail ======

    @Test
    fun `validateEmail with blank email returns required error`() {
        val result = AuthValidators.validateEmail("")

        assertNotNull(result)
        assertTrue(result is UiText.Resource)
        assertEquals(R.string.auth_email_required, (result as UiText.Resource).resId)
    }

    @Test
    fun `validateEmail with invalid format returns invalid error`() {
        val result = AuthValidators.validateEmail("invalid-email")

        assertNotNull(result)
        assertTrue(result is UiText.Resource)
        assertEquals(R.string.auth_email_invalid, (result as UiText.Resource).resId)
    }

    @Test
    fun `validateEmail exceeding max length returns too long error`() {
        val longEmail = "a".repeat(AppConfig.Auth.EMAIL_MAX_LENGTH + 1) + "@example.com"

        val result = AuthValidators.validateEmail(longEmail)

        assertNotNull(result)
        assertTrue(result is UiText.ResourceWithArgs)
        assertEquals(R.string.ui_validation_too_long, (result as UiText.ResourceWithArgs).resId)
    }

    @Test
    fun `validateEmail with valid email returns null`() {
        val result = AuthValidators.validateEmail("test@example.com")

        assertNull(result)
    }

    // ====== validatePasswordForLogin ======

    @Test
    fun `validatePasswordForLogin with blank password returns required error`() {
        val result = AuthValidators.validatePasswordForLogin("")

        assertNotNull(result)
        assertTrue(result is UiText.Resource)
        assertEquals(R.string.auth_password_required, (result as UiText.Resource).resId)
    }

    @Test
    fun `validatePasswordForLogin with any non-blank password returns null`() {
        val result = AuthValidators.validatePasswordForLogin("x")

        assertNull(result)
    }

    // ====== validatePassword ======

    @Test
    fun `validatePassword with blank password returns required error`() {
        val result = AuthValidators.validatePassword("")

        assertNotNull(result)
        assertTrue(result is UiText.Resource)
        assertEquals(R.string.auth_password_required, (result as UiText.Resource).resId)
    }

    @Test
    fun `validatePassword with too short password returns too short error`() {
        val shortPassword = "a".repeat(AppConfig.Auth.PASSWORD_MIN_LENGTH - 1)

        val result = AuthValidators.validatePassword(shortPassword)

        assertNotNull(result)
        assertTrue(result is UiText.ResourceWithArgs)
        assertEquals(R.string.auth_password_too_short, (result as UiText.ResourceWithArgs).resId)
    }

    @Test
    fun `validatePassword exceeding max length returns too long error`() {
        val longPassword = "a".repeat(AppConfig.Auth.PASSWORD_MAX_LENGTH + 1)

        val result = AuthValidators.validatePassword(longPassword)

        assertNotNull(result)
        assertTrue(result is UiText.ResourceWithArgs)
        assertEquals(R.string.ui_validation_too_long, (result as UiText.ResourceWithArgs).resId)
    }

    @Test
    fun `validatePassword at exact min length returns null`() {
        val minPassword = "a".repeat(AppConfig.Auth.PASSWORD_MIN_LENGTH)

        val result = AuthValidators.validatePassword(minPassword)

        assertNull(result)
    }

    @Test
    fun `validatePassword at exact max length returns null`() {
        val maxPassword = "a".repeat(AppConfig.Auth.PASSWORD_MAX_LENGTH)

        val result = AuthValidators.validatePassword(maxPassword)

        assertNull(result)
    }

    // ====== validateDisplayName ======

    @Test
    fun `validateDisplayName with blank name returns required error`() {
        val result = AuthValidators.validateDisplayName("")

        assertNotNull(result)
        assertTrue(result is UiText.Resource)
        assertEquals(R.string.auth_display_name_required, (result as UiText.Resource).resId)
    }

    @Test
    fun `validateDisplayName exceeding max length returns too long error`() {
        val longName = "a".repeat(AppConfig.Auth.DISPLAY_NAME_MAX_LENGTH + 1)

        val result = AuthValidators.validateDisplayName(longName)

        assertNotNull(result)
        assertTrue(result is UiText.ResourceWithArgs)
        assertEquals(R.string.ui_validation_too_long, (result as UiText.ResourceWithArgs).resId)
    }

    @Test
    fun `validateDisplayName with valid name returns null`() {
        val result = AuthValidators.validateDisplayName("John Doe")

        assertNull(result)
    }
}
