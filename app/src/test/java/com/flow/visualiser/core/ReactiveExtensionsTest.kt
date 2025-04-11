package com.flow.visualiser.core

import androidx.lifecycle.MutableLiveData
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ReactiveExtensionsTest {
    
    @Before
    fun setup() {
        // Create mocks to prevent actual calls
        mockkObject(ReactiveStreamTracker)
        
        // Use explicit type parameters for the mocks
        every { 
            ReactiveStreamTracker.trackFlow<String>(any(), any()) 
        } returns flow { emit("Mocked value") }
        
        every { 
            ReactiveStreamTracker.trackStateFlow<String>(any(), any()) 
        } returns MutableStateFlow("Mocked value")
        
        every { 
            ReactiveStreamTracker.trackLiveData<String>(any(), any()) 
        } returns MutableLiveData("Mocked value")
        
        every { 
            ReactiveStreamTracker.stopTrackingLiveData<String>(any()) 
        } returns Unit
    }
    
    @After
    fun tearDown() {
        unmockkObject(ReactiveStreamTracker)
    }
    
    @Test
    fun `trackFlow extension doesn't crash`() {
        // Just test that it doesn't crash
        val testFlow = flow { emit("Test") }
        testFlow.trackFlow("Test Flow")
    }
    
    @Test
    fun `trackStateFlow extension doesn't crash`() {
        // Just test that it doesn't crash
        val testStateFlow = MutableStateFlow("Test")
        testStateFlow.trackStateFlow("Test StateFlow")
    }
    
    @Test
    fun `trackLiveData extension doesn't crash`() {
        // Just test that it doesn't crash
        val testLiveData = MutableLiveData("Test")
        testLiveData.trackLiveData("Test LiveData")
    }
    
    @Test
    fun `stopTracking extension doesn't crash`() {
        // Just test that it doesn't crash
        val testLiveData = MutableLiveData("Test")
        testLiveData.stopTracking()
    }
    
    @Test
    fun `trackOperator extension doesn't crash`() {
        // Just test that it doesn't crash
        val testFlow = flow { emit("Test") }
        testFlow.trackOperator("After map")
    }
} 