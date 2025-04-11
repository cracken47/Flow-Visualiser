package com.flow.visualiser.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.flow.visualiser.core.ReactiveStreamTracker
import com.flow.visualiser.model.FlowEvent
import com.flow.visualiser.model.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FlowVisualizerViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FlowVisualizerViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Initialize ViewModel
        viewModel = FlowVisualizerViewModel()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `toggleStreamTypeVisibility should update visibility states`() = runTest {
        // Initially all stream types should be visible
        assertEquals(true, viewModel.showFlowEvents.value)
        assertEquals(true, viewModel.showStateFlowEvents.value)
        assertEquals(true, viewModel.showLiveDataEvents.value)
        
        // Toggle Flow visibility off
        viewModel.toggleStreamTypeVisibility(StreamType.FLOW, false)
        
        // Verify Flow visibility is off, others still on
        assertEquals(false, viewModel.showFlowEvents.value)
        assertEquals(true, viewModel.showStateFlowEvents.value)
        assertEquals(true, viewModel.showLiveDataEvents.value)
        
        // Toggle LiveData visibility off
        viewModel.toggleStreamTypeVisibility(StreamType.LIVE_DATA, false)
        
        // Verify both Flow and LiveData are off, StateFlow still on
        assertEquals(false, viewModel.showFlowEvents.value)
        assertEquals(true, viewModel.showStateFlowEvents.value)
        assertEquals(false, viewModel.showLiveDataEvents.value)
        
        // Toggle StateFlow visibility off
        viewModel.toggleStreamTypeVisibility(StreamType.STATE_FLOW, false)
        
        // Verify all are off
        assertEquals(false, viewModel.showFlowEvents.value)
        assertEquals(false, viewModel.showStateFlowEvents.value)
        assertEquals(false, viewModel.showLiveDataEvents.value)
        
        // Toggle Flow visibility back on
        viewModel.toggleStreamTypeVisibility(StreamType.FLOW, true)
        
        // Verify Flow is on, others still off
        assertEquals(true, viewModel.showFlowEvents.value)
        assertEquals(false, viewModel.showStateFlowEvents.value)
        assertEquals(false, viewModel.showLiveDataEvents.value)
    }
    
    @Test
    fun `clearEvents should reset state`() = runTest {
        // Call clearEvents method
        viewModel.clearEvents()
        
        // Verify events list is empty
        assertEquals(0, viewModel.flowEvents.value.size)
        
        // Verify active streams is empty
        assertEquals(0, viewModel.activeStreams.value.size)
    }
} 