package com.flow.visualiser.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty

/**
 * Extension function to easily track a Flow.
 */
fun <T> Flow<T>.trackFlow(name: String = ""): Flow<T> {
    return ReactiveStreamTracker.trackFlow(this, name)
}

/**
 * Extension function to track operators in a Flow chain.
 */
fun <T> Flow<T>.trackOperator(operatorName: String): Flow<T> {
    return ReactiveStreamTracker.trackOperator(this, operatorName)
}

/**
 * Extension function to track a StateFlow.
 */
fun <T> StateFlow<T>.trackStateFlow(name: String = ""): StateFlow<T> {
    return ReactiveStreamTracker.trackStateFlow(this, name)
}

/**
 * Extension function to track a MutableStateFlow.
 */
fun <T> MutableStateFlow<T>.trackMutableStateFlow(name: String = ""): MutableStateFlow<T> {
    // Track this flow with the given name
    ReactiveStreamTracker.registerMutableStateFlow(this, name)
    return this
}

/**
 * Extension function to track a LiveData.
 */
fun <T> LiveData<T>.trackLiveData(name: String = ""): LiveData<T> {
    return ReactiveStreamTracker.trackLiveData(this, name)
}

/**
 * Extension function to stop tracking a LiveData.
 */
fun <T> LiveData<T>.stopTracking() {
    ReactiveStreamTracker.stopTrackingLiveData(this)
}

/**
 * Extension property to automatically track a StateFlow in a ViewModel.
 */
fun <T> ViewModel.trackingStateFlow(stateFlow: StateFlow<T>, name: String): TrackingStateFlowDelegate<T> {
    return TrackingStateFlowDelegate(stateFlow, name)
}

/**
 * Delegate class to handle property access for tracked StateFlows
 */
class TrackingStateFlowDelegate<T>(
    private val stateFlow: StateFlow<T>,
    private val name: String
) {
    private val trackedFlow = ReactiveStreamTracker.trackStateFlow(stateFlow, name)
    
    operator fun getValue(thisRef: Any?, property: KProperty<*>): StateFlow<T> {
        return trackedFlow
    }
} 