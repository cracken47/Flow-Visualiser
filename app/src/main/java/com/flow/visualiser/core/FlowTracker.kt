package com.flow.visualiser.core

import com.flow.visualiser.model.FlowEvent
import com.flow.visualiser.plugin.FlowVisualizerConfig
import kotlinx.coroutines.flow.Flow

/**
 * Tracks and visualizes Kotlin Flow emissions.
 * This class is maintained for backward compatibility and forwards to ReactiveStreamTracker.
 */
object FlowTracker {
    
    /**
     * Expose ReactiveStreamTracker's event flow
     */
    val eventFlow: Flow<FlowEvent<*>> get() = ReactiveStreamTracker.eventFlow
    
    /**
     * Set configuration for the FlowTracker
     */
    internal fun setConfig(config: FlowVisualizerConfig) {
        ReactiveStreamTracker.setConfig(config)
    }
    
    /**
     * Enable or disable flow tracking
     */
    fun setTrackingEnabled(enabled: Boolean) {
        ReactiveStreamTracker.setTrackingEnabled(enabled)
    }
    
    /**
     * Track a flow by wrapping it in a monitoring flow that reports events to the tracker.
     *
     * @param flow The source flow to be tracked
     * @param name Optional name for this flow for identification purposes
     * @return A new flow that emits the same values but reports all events to the visualizer
     */
    fun <T> track(flow: Flow<T>, name: String = ""): Flow<T> {
        return ReactiveStreamTracker.trackFlow(flow, name)
    }
    
    /**
     * Reset the tracker
     */
    fun reset() {
        ReactiveStreamTracker.reset()
    }
} 