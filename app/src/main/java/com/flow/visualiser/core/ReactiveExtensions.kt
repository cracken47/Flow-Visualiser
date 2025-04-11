package com.flow.visualiser.core

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Extension function to easily track a Flow.
 * 
 * Example usage:
 * ```
 * myFlow
 *     .trackFlow("My Custom Flow")
 *     .collect { value ->
 *         // Process the value
 *     }
 * ```
 * 
 * @param name Optional name for this flow for identification purposes
 * @return A new flow that emits the same values but reports all events to the visualizer
 */
fun <T> Flow<T>.trackFlow(name: String = ""): Flow<T> {
    return ReactiveStreamTracker.trackFlow(this, name)
}

/**
 * Extension function to track a StateFlow.
 * 
 * Example usage:
 * ```
 * val stateFlow = _stateFlow.trackStateFlow("User Preferences")
 * ```
 * 
 * @param name Optional name for this StateFlow for identification purposes
 * @return A wrapped StateFlow that reports all state changes to the visualizer
 */
fun <T> StateFlow<T>.trackStateFlow(name: String = ""): StateFlow<T> {
    return ReactiveStreamTracker.trackStateFlow(this, name)
}

/**
 * Extension function to track a LiveData.
 * 
 * Example usage:
 * ```
 * val liveData = userRepository.userData.trackLiveData("User Data")
 * ```
 * 
 * @param name Optional name for this LiveData for identification purposes
 * @return The original LiveData that now reports all value changes to the visualizer
 */
fun <T> LiveData<T>.trackLiveData(name: String = ""): LiveData<T> {
    return ReactiveStreamTracker.trackLiveData(this, name)
}

/**
 * Extension function to stop tracking a LiveData.
 * 
 * Example usage:
 * ```
 * userRepository.userData.stopTracking()
 * ```
 */
fun <T> LiveData<T>.stopTracking() {
    ReactiveStreamTracker.stopTrackingLiveData(this)
}

/**
 * Extension function to easily track Flow events for a specific flow operator in a chain.
 * 
 * Example usage:
 * ```
 * myFlow
 *     .map { it * 2 }
 *     .trackOperator("After map transformation")
 *     .filter { it > 10 }
 *     .collect { ... }
 * ```
 * 
 * @param operatorDescription Description of the operator or stage being tracked
 * @return A new flow that emits the same values but reports all events to the visualizer
 */
fun <T> Flow<T>.trackOperator(operatorDescription: String): Flow<T> {
    return ReactiveStreamTracker.trackFlow(this, operatorDescription)
} 