package com.flow.visualiser.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.flow.visualiser.model.FlowEvent
import com.flow.visualiser.model.StreamType
import com.flow.visualiser.plugin.FlowVisualizerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Core tracker for monitoring reactive streams (Flow, StateFlow, LiveData)
 */
object ReactiveStreamTracker {
    
    // Coroutine scope for the tracker
    internal val trackerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // This shared flow will receive all events from all tracked streams
    private val _eventFlow = MutableSharedFlow<FlowEvent<*>>(extraBufferCapacity = 100)
    
    // Expose as immutable flow
    val eventFlow: Flow<FlowEvent<*>> = _eventFlow
    
    // Track active LiveData observers to prevent memory leaks
    private val liveDataObservers = mutableMapOf<LiveData<*>, Observer<*>>()
    
    // Track MutableStateFlows to avoid garbage collection
    private val mutableStateFlows = mutableMapOf<MutableStateFlow<*>, String>()
    
    // Configuration
    private var config: FlowVisualizerConfig = FlowVisualizerConfig.default()
    
    // Tracking state
    private var isTrackingEnabled = true
    private var streamCounter = 0
    
    /**
     * Set configuration for the tracker
     */
    internal fun setConfig(config: FlowVisualizerConfig) {
        this.config = config
    }
    
    /**
     * Enable or disable tracking
     */
    fun setTrackingEnabled(enabled: Boolean) {
        isTrackingEnabled = enabled
    }
    
    /**
     * Track a Flow by wrapping it in a monitoring flow
     */
    fun <T> trackFlow(flow: Flow<T>, name: String = ""): Flow<T> {
        if (!isTrackingEnabled) {
            return flow
        }
        
        val streamId = ++streamCounter
        val streamName = if (name.isNotEmpty()) name else "Flow-$streamId"
        
        return flow {
            val startEvent = FlowEvent.Started<T>(streamName, StreamType.FLOW)
            _eventFlow.emit(startEvent)
            
            flow
                .onStart { /* Already handled above */ }
                .catch { error ->
                    val errorEvent = FlowEvent.Error<T>(error, streamName, StreamType.FLOW)
                    _eventFlow.emit(errorEvent)
                    throw error // Re-throw to preserve original flow semantics
                }
                .onCompletion { error ->
                    if (error == null) {
                        val completionEvent = FlowEvent.Completion<T>(streamName, StreamType.FLOW)
                        _eventFlow.emit(completionEvent)
                    } else {
                        val cancelEvent = FlowEvent.Cancelled<T>(streamName, StreamType.FLOW)
                        _eventFlow.emit(cancelEvent)
                    }
                }
                .collect { value ->
                    val emissionEvent = FlowEvent.Emission(value, streamName, StreamType.FLOW)
                    _eventFlow.emit(emissionEvent)
                    emit(value)
                }
        }
    }
    
    /**
     * Track an operator in a flow chain
     * This is useful for visualizing transformations within a flow pipeline
     */
    fun <T> trackOperator(flow: Flow<T>, operatorName: String): Flow<T> {
        if (!isTrackingEnabled) {
            return flow
        }
        
        val streamId = ++streamCounter
        val streamName = if (operatorName.isNotEmpty()) operatorName else "Operator-$streamId"
        
        return flow {
            val startEvent = FlowEvent.Started<T>(streamName, StreamType.OPERATOR)
            _eventFlow.emit(startEvent)
            
            flow
                .onStart { /* Already handled above */ }
                .catch { error ->
                    val errorEvent = FlowEvent.Error<T>(error, streamName, StreamType.OPERATOR)
                    _eventFlow.emit(errorEvent)
                    throw error // Re-throw to preserve original flow semantics
                }
                .onCompletion { error ->
                    if (error == null) {
                        val completionEvent = FlowEvent.Completion<T>(streamName, StreamType.OPERATOR)
                        _eventFlow.emit(completionEvent)
                    } else {
                        val cancelEvent = FlowEvent.Cancelled<T>(streamName, StreamType.OPERATOR)
                        _eventFlow.emit(cancelEvent)
                    }
                }
                .collect { value ->
                    val emissionEvent = FlowEvent.Emission(value, streamName, StreamType.OPERATOR)
                    _eventFlow.emit(emissionEvent)
                    emit(value)
                }
        }
    }
    
    /**
     * Track a StateFlow by wrapping it and monitoring its state changes
     */
    fun <T> trackStateFlow(stateFlow: StateFlow<T>, name: String = ""): StateFlow<T> {
        if (!isTrackingEnabled) {
            return stateFlow
        }
        
        val streamId = ++streamCounter
        val streamName = if (name.isNotEmpty()) name else "StateFlow-$streamId"
        
        // Emit initial state
        trackerScope.launch {
            val startEvent = FlowEvent.Started<T>(streamName, StreamType.STATE_FLOW)
            _eventFlow.emit(startEvent)
            
            val initialValue = stateFlow.value
            val initialEvent = FlowEvent.Emission(initialValue, streamName, StreamType.STATE_FLOW)
            _eventFlow.emit(initialEvent)
        }
        
        // Create a wrapper StateFlow that tracks changes
        val mutableStateFlow = MutableStateFlow(stateFlow.value)
        
        // Collect from original and update wrapped StateFlow
        trackerScope.launch {
            try {
                stateFlow.collect { value ->
                    mutableStateFlow.value = value
                    val emissionEvent = FlowEvent.Emission(value, streamName, StreamType.STATE_FLOW)
                    _eventFlow.emit(emissionEvent)
                }
            } catch (e: Exception) {
                val errorEvent = FlowEvent.Error<T>(e, streamName, StreamType.STATE_FLOW)
                _eventFlow.emit(errorEvent)
            }
        }
        
        return mutableStateFlow.asStateFlow()
    }
    
    /**
     * Register a MutableStateFlow for tracking
     */
    fun <T> registerMutableStateFlow(flow: MutableStateFlow<T>, name: String = "") {
        if (!isTrackingEnabled) return
        
        val streamId = ++streamCounter
        val streamName = if (name.isNotEmpty()) name else "MutableStateFlow-$streamId"
        
        // Store reference to prevent garbage collection
        mutableStateFlows[flow] = streamName
        
        // Emit initial state
        trackerScope.launch {
            val startEvent = FlowEvent.Started<T>(streamName, StreamType.STATE_FLOW)
            _eventFlow.emit(startEvent)
            
            val initialValue = flow.value
            val initialEvent = FlowEvent.Emission(initialValue, streamName, StreamType.STATE_FLOW)
            _eventFlow.emit(initialEvent)
            
            // Collect from the flow to track changes
            flow.collect { value ->
                val emissionEvent = FlowEvent.Emission(value, streamName, StreamType.STATE_FLOW)
                _eventFlow.emit(emissionEvent)
            }
        }
    }
    
    /**
     * Track a LiveData by observing it and reporting value changes to the visualizer
     */
    fun <T> trackLiveData(liveData: LiveData<T>, name: String = ""): LiveData<T> {
        if (!isTrackingEnabled) {
            return liveData
        }
        
        val streamId = ++streamCounter
        val streamName = if (name.isNotEmpty()) name else "LiveData-$streamId"
        
        // Remove any existing observer to prevent duplicates
        if (liveDataObservers.containsKey(liveData)) {
            val existingObserver = liveDataObservers[liveData]
            existingObserver?.let { liveData.removeObserver(it as Observer<T>) }
            liveDataObservers.remove(liveData)
        }
        
        // Emit started event
        trackerScope.launch {
            val startEvent = FlowEvent.Started<T>(streamName, StreamType.LIVE_DATA)
            _eventFlow.emit(startEvent)
        }
        
        // Create and attach observer
        val observer = Observer<T> { value ->
            trackerScope.launch {
                val emissionEvent = FlowEvent.Emission(value, streamName, StreamType.LIVE_DATA)
                _eventFlow.emit(emissionEvent)
            }
        }
        
        // Store observer to be able to clean up later
        @Suppress("UNCHECKED_CAST")
        liveDataObservers[liveData] = observer as Observer<Any?>
        
        // Start observing
        liveData.observeForever(observer)
        
        return liveData
    }
    
    /**
     * Stop tracking a specific LiveData
     */
    fun <T> stopTrackingLiveData(liveData: LiveData<T>) {
        if (liveDataObservers.containsKey(liveData)) {
            val observer = liveDataObservers[liveData]
            @Suppress("UNCHECKED_CAST")
            observer?.let { liveData.removeObserver(it as Observer<T>) }
            liveDataObservers.remove(liveData)
        }
    }
    
    /**
     * Reset the tracker, clearing all tracked streams
     */
    fun reset() {
        streamCounter = 0
        
        // Clean up LiveData observers
        liveDataObservers.forEach { (liveData, observer) ->
            @Suppress("UNCHECKED_CAST")
            liveData.removeObserver(observer as Observer<Any?>)
        }
        liveDataObservers.clear()
        
        // Clear MutableStateFlow references
        mutableStateFlows.clear()
    }
} 