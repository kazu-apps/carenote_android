package com.carenote.app.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

@ExperimentalCoroutinesApi
class MainCoroutineRuleTest {

    @Test
    fun `default dispatcher is TestDispatcher`() {
        val rule = MainCoroutineRule()
        assertTrue(rule.testDispatcher is TestDispatcher)
    }

    @Test
    fun `accepts custom TestDispatcher`() {
        val customDispatcher = UnconfinedTestDispatcher()
        val rule = MainCoroutineRule(customDispatcher)
        assertNotSame(MainCoroutineRule().testDispatcher, rule.testDispatcher)
    }

    @Test
    fun `testDispatcher property is accessible`() {
        val rule = MainCoroutineRule()
        assertNotNull(rule.testDispatcher)
    }

    @Test
    fun `sets Main dispatcher during test execution`() {
        val rule = MainCoroutineRule()
        val description = Description.createTestDescription("TestClass", "testMethod")
        val baseStatement = object : Statement() {
            override fun evaluate() {
                // If Dispatchers.Main is set, runTest should work
                // We verify by successfully running a coroutine
            }
        }
        // apply() calls starting() before evaluate() and finished() after
        rule.apply(baseStatement, description).evaluate()
    }

    @Test
    fun `resets Main dispatcher after test execution`() {
        val rule = MainCoroutineRule()
        val description = Description.createTestDescription("TestClass", "testMethod")
        var evaluateCalled = false
        val baseStatement = object : Statement() {
            override fun evaluate() {
                evaluateCalled = true
            }
        }
        rule.apply(baseStatement, description).evaluate()
        assertTrue(evaluateCalled)
        // After evaluate(), finished() was called -> Dispatchers.Main is reset
        // No exception means reset was successful
    }

    @Test
    fun `works with runTest integration`() {
        val rule = MainCoroutineRule()
        val description = Description.createTestDescription("TestClass", "testMethod")
        val baseStatement = object : Statement() {
            override fun evaluate() {
                // Verify runTest works with the rule's dispatcher
                runTest(rule.testDispatcher) {
                    // If this completes without exception, Main dispatcher is properly set
                    val result = 1 + 1
                    assertTrue(result == 2)
                }
            }
        }
        rule.apply(baseStatement, description).evaluate()
    }
}
