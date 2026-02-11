package com.carenote.app.ui.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SnackbarControllerTest {

    private val controller = SnackbarController()

    @Test
    fun `showMessage with ResId sends WithResId event`() = runTest {
        val resId = 42

        var received: SnackbarEvent? = null
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.events.collect { received = it }
        }

        controller.showMessage(resId)

        assertTrue(received is SnackbarEvent.WithResId)
        assertEquals(resId, (received as SnackbarEvent.WithResId).messageResId)
        assertNull((received as SnackbarEvent.WithResId).actionLabel)
        assertNull((received as SnackbarEvent.WithResId).onAction)

        job.cancel()
    }

    @Test
    fun `showMessage with String sends WithString event`() = runTest {
        val message = "テストメッセージ"

        var received: SnackbarEvent? = null
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.events.collect { received = it }
        }

        controller.showMessage(message)

        assertTrue(received is SnackbarEvent.WithString)
        assertEquals(message, (received as SnackbarEvent.WithString).message)
        assertNull((received as SnackbarEvent.WithString).actionLabel)

        job.cancel()
    }

    @Test
    fun `showMessageWithAction sends WithResId event with action`() = runTest {
        val resId = 99
        val actionLabel = "取消"
        var actionCalled = false
        val onAction = { actionCalled = true }

        var received: SnackbarEvent? = null
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.events.collect { received = it }
        }

        controller.showMessageWithAction(resId, actionLabel, onAction)

        assertTrue(received is SnackbarEvent.WithResId)
        val event = received as SnackbarEvent.WithResId
        assertEquals(resId, event.messageResId)
        assertEquals(actionLabel, event.actionLabel)
        event.onAction?.invoke()
        assertTrue(actionCalled)

        job.cancel()
    }

    @Test
    fun `multiple events are received in order`() = runTest {
        val events = mutableListOf<SnackbarEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.events.collect { events.add(it) }
        }

        controller.showMessage("first")
        controller.showMessage("second")
        controller.showMessage(123)

        assertEquals(3, events.size)
        assertTrue(events[0] is SnackbarEvent.WithString)
        assertEquals("first", (events[0] as SnackbarEvent.WithString).message)
        assertTrue(events[1] is SnackbarEvent.WithString)
        assertEquals("second", (events[1] as SnackbarEvent.WithString).message)
        assertTrue(events[2] is SnackbarEvent.WithResId)
        assertEquals(123, (events[2] as SnackbarEvent.WithResId).messageResId)

        job.cancel()
    }

    @Test
    fun `WithResId data class equality`() {
        val event1 = SnackbarEvent.WithResId(messageResId = 1)
        val event2 = SnackbarEvent.WithResId(messageResId = 1)
        val event3 = SnackbarEvent.WithResId(messageResId = 2)

        assertEquals(event1, event2)
        assertTrue(event1 != event3)
    }

    @Test
    fun `WithString data class equality`() {
        val event1 = SnackbarEvent.WithString(message = "hello")
        val event2 = SnackbarEvent.WithString(message = "hello")
        val event3 = SnackbarEvent.WithString(message = "world")

        assertEquals(event1, event2)
        assertTrue(event1 != event3)
    }
}
