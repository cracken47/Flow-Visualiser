package com.flow.visualiser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flow.visualiser.core.ReactiveStreamTracker
import com.flow.visualiser.model.FlowEvent
import com.flow.visualiser.model.StreamType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel that manages the visualization of reactive stream events
 */
class FlowVisualizerViewModel : ViewModel() {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    // Maximum number of events to keep in history
    private val maxEventsHistory = 100
    
    // State to hold the list of tracked stream events
    private val _flowEvents = MutableStateFlow<List<FlowEventUI>>(emptyList())
    val flowEvents: StateFlow<List<FlowEventUI>> = _flowEvents.asStateFlow()
    
    // States to filter by stream type
    private val _showFlowEvents = MutableStateFlow(true)
    val showFlowEvents: StateFlow<Boolean> = _showFlowEvents.asStateFlow()
    
    private val _showStateFlowEvents = MutableStateFlow(true)
    val showStateFlowEvents: StateFlow<Boolean> = _showStateFlowEvents.asStateFlow()
    
    private val _showLiveDataEvents = MutableStateFlow(true)
    val showLiveDataEvents: StateFlow<Boolean> = _showLiveDataEvents.asStateFlow()
    
    // Track active streams
    private val _activeStreams = MutableStateFlow<Set<String>>(emptySet())
    val activeStreams: StateFlow<Set<String>> = _activeStreams.asStateFlow()
    
    init {
        // Start collecting events from the tracker
        viewModelScope.launch {
            ReactiveStreamTracker.eventFlow.collect { event ->
                processEvent(event)
            }
        }
    }
    
    /**
     * Process an incoming reactive stream event and update the UI state
     */
    private fun processEvent(event: FlowEvent<*>) {
        val formattedTime = timeFormatter.format(Date(event.timestamp))
        
        // Update active streams list for new streams
        if (event is FlowEvent.Started) {
            _activeStreams.update { streams ->
                streams + event.streamName
            }
        }
        
        // Get color based on the stream type
        val colorForType = when (event.streamType) {
            StreamType.FLOW -> "#4CAF50" // Green
            StreamType.STATE_FLOW -> "#2196F3" // Blue
            StreamType.LIVE_DATA -> "#9C27B0" // Purple
            StreamType.RX_OBSERVABLE -> "#FF9800" // Orange
            StreamType.RX_SUBJECT -> "#F44336" // Red
            StreamType.CHANNEL -> "#795548" // Brown
        }
        
        val eventUI = when (event) {
            is FlowEvent.Emission<*> -> {
                FlowEventUI(
                    id = event.id,
                    time = formattedTime,
                    type = "EMISSION",
                    content = event.value.toString(),
                    color = colorForType,
                    streamName = event.streamName,
                    streamType = event.streamType
                )
            }
            is FlowEvent.Error<*> -> {
                FlowEventUI(
                    id = event.id,
                    time = formattedTime,
                    type = "ERROR",
                    content = event.throwable.message ?: "Unknown error",
                    color = "#F44336", // Always red for errors
                    streamName = event.streamName,
                    streamType = event.streamType
                )
            }
            is FlowEvent.Completion<*> -> {
                // Remove from active streams
                _activeStreams.update { streams -> 
                    streams - event.streamName 
                }
                
                FlowEventUI(
                    id = event.id,
                    time = formattedTime,
                    type = "COMPLETED",
                    content = "Stream completed",
                    color = colorForType,
                    streamName = event.streamName,
                    streamType = event.streamType
                )
            }
            is FlowEvent.Started<*> -> {
                FlowEventUI(
                    id = event.id,
                    time = formattedTime,
                    type = "STARTED",
                    content = "Stream started",
                    color = colorForType,
                    streamName = event.streamName,
                    streamType = event.streamType
                )
            }
            is FlowEvent.Cancelled<*> -> {
                // Remove from active streams
                _activeStreams.update { streams -> 
                    streams - event.streamName 
                }
                
                FlowEventUI(
                    id = event.id,
                    time = formattedTime,
                    type = "CANCELLED",
                    content = "Stream cancelled",
                    color = "#FF9800", // Orange for cancelled
                    streamName = event.streamName,
                    streamType = event.streamType
                )
            }
        }
        
        _flowEvents.update { events ->
            (events + eventUI).takeLast(maxEventsHistory)
        }
    }
    
    /**
     * Set whether to show or hide events from a specific stream type
     */
    fun toggleStreamTypeVisibility(streamType: StreamType, show: Boolean) {
        when (streamType) {
            StreamType.FLOW -> _showFlowEvents.value = show
            StreamType.STATE_FLOW -> _showStateFlowEvents.value = show
            StreamType.LIVE_DATA -> _showLiveDataEvents.value = show
            else -> {} // Not implemented yet
        }
    }
    
    /**
     * Filter events based on current visibility settings
     */
    fun getFilteredEvents(): List<FlowEventUI> {
        return flowEvents.value.filter { event ->
            when (event.streamType) {
                StreamType.FLOW -> showFlowEvents.value
                StreamType.STATE_FLOW -> showStateFlowEvents.value
                StreamType.LIVE_DATA -> showLiveDataEvents.value
                else -> true // Show other types by default
            }
        }
    }
    
    /**
     * Clear all events from the history
     */
    fun clearEvents() {
        _flowEvents.update { emptyList() }
        _activeStreams.update { emptySet() }
        
        // Also reset event tracking in the tracker
        ReactiveStreamTracker.reset()
    }
}

/**
 * Represents a Flow event prepared for UI display
 */
data class FlowEventUI(
    val id: String,
    val time: String,
    val type: String,
    val content: String,
    val color: String,
    val streamName: String,
    val streamType: StreamType
) 