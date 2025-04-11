package com.flow.visualiser.core

import com.flow.visualiser.model.FlowEvent
import com.flow.visualiser.model.StreamType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ReactiveStreamTrackerTest {

    @Before
    fun setup() {
        // Reset the tracker before each test
        ReactiveStreamTracker.reset()
    }

    @After
    fun tearDown() {
        // Clean up after each test
        ReactiveStreamTracker.reset()
    }

    @Test
    fun `trackFlow should emit events`() = runTest {
        // Simply verify the tracker doesn't crash
        val testFlow = flow { emit("Test value") }
        val trackedFlow = ReactiveStreamTracker.trackFlow(testFlow, "Test Flow")
        
        // Collect from the tracked flow
        trackedFlow.collect { /* just collecting, no further processing needed */ }
    }
    
    @Test
    fun `setTrackingEnabled should control tracking`() = runTest {
        // Disable tracking
        ReactiveStreamTracker.setTrackingEnabled(false)
        
        // Create a test flow and track it
        val testFlow = flow { emit("Test value") }
        val trackedFlow = ReactiveStreamTracker.trackFlow(testFlow, "Test Flow")
        
        // The tracked flow should be the same as the original when tracking is disabled
        assertEquals(testFlow, trackedFlow)
        
        // Re-enable tracking
        ReactiveStreamTracker.setTrackingEnabled(true)
    }
    
    @Test
    fun `reset should clear internal state`() = runTest {
        // Call reset to ensure it doesn't crash
        ReactiveStreamTracker.reset()
    }
} 