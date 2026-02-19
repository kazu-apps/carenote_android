package com.carenote.app.data.remote

import com.carenote.app.testing.assertNetworkError
import com.carenote.app.testing.assertNotFoundError
import com.carenote.app.testing.assertSuccess
import com.carenote.app.testing.assertUnauthorizedError
import com.carenote.app.testing.assertValidationError
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirebasePurchaseVerifierTest {

    private val mockFunctions = mockk<FirebaseFunctions>()
    private val mockCallable = mockk<HttpsCallableReference>()
    private val mockTask = mockk<Task<HttpsCallableResult>>()

    private lateinit var verifier: FirebasePurchaseVerifier

    @Before
    fun setup() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockFunctions.getHttpsCallable("verifyPurchase") } returns mockCallable
        every { mockCallable.call(any<Any>()) } returns mockTask
        verifier = FirebasePurchaseVerifier(dagger.Lazy { mockFunctions })
    }

    @After
    fun tearDown() {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `verify returns Success with valid response`() = runTest {
        val responseMap = mapOf(
            "isActive" to true,
            "productId" to "carenote_premium_monthly",
            "expiryTimeMillis" to 1735689600000L,
            "autoRenewing" to true
        )

        val callableResult = createHttpsCallableResult(responseMap)
        coEvery { mockTask.await() } returns callableResult

        val result = verifier.verify("test_token", "carenote_premium_monthly")

        val verified = result.assertSuccess()
        assertEquals(true, verified.isActive)
        assertEquals("carenote_premium_monthly", verified.productId)
        assertEquals(1735689600000L, verified.expiryTimeMillis)
        assertEquals(true, verified.autoRenewing)
    }

    @Test
    fun `verify returns UnauthorizedError for UNAUTHENTICATED`() = runTest {
        val exception = mockk<FirebaseFunctionsException>(relaxed = true)
        every { exception.code } returns FirebaseFunctionsException.Code.UNAUTHENTICATED
        coEvery { mockTask.await() } throws exception

        val result = verifier.verify("test_token", "carenote_premium_monthly")

        result.assertUnauthorizedError()
    }

    @Test
    fun `verify returns ValidationError for INVALID_ARGUMENT`() = runTest {
        val exception = mockk<FirebaseFunctionsException>(relaxed = true)
        every { exception.code } returns FirebaseFunctionsException.Code.INVALID_ARGUMENT
        coEvery { mockTask.await() } throws exception

        val result = verifier.verify("test_token", "carenote_premium_monthly")

        result.assertValidationError()
    }

    @Test
    fun `verify returns NotFoundError for NOT_FOUND`() = runTest {
        val exception = mockk<FirebaseFunctionsException>(relaxed = true)
        every { exception.code } returns FirebaseFunctionsException.Code.NOT_FOUND
        coEvery { mockTask.await() } throws exception

        val result = verifier.verify("test_token", "carenote_premium_monthly")

        result.assertNotFoundError()
    }

    @Test
    fun `verify returns UnauthorizedError for PERMISSION_DENIED`() = runTest {
        val exception = mockk<FirebaseFunctionsException>(relaxed = true)
        every { exception.code } returns FirebaseFunctionsException.Code.PERMISSION_DENIED
        coEvery { mockTask.await() } throws exception

        val result = verifier.verify("test_token", "carenote_premium_monthly")

        result.assertUnauthorizedError()
    }

    @Test
    fun `verify returns NetworkError for unexpected exception`() = runTest {
        coEvery { mockTask.await() } throws RuntimeException("Unexpected error")

        val result = verifier.verify("test_token", "carenote_premium_monthly")

        result.assertNetworkError()
    }

    private fun createHttpsCallableResult(data: Any): HttpsCallableResult {
        val constructor = HttpsCallableResult::class.java.getDeclaredConstructor(Any::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(data)
    }
}
