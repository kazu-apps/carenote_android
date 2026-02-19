package com.carenote.app.data.remote

import com.carenote.app.testing.assertNetworkError
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NoOpPurchaseVerifierTest {

    @Test
    fun `verify always returns NetworkError`() = runTest {
        val verifier = NoOpPurchaseVerifier()
        val result = verifier.verify("token", "productId")
        result.assertNetworkError()
    }
}
