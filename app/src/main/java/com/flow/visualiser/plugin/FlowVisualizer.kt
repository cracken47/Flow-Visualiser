package com.flow.visualiser.plugin

import android.app.Application
import android.content.Context
import android.util.Log
import com.flow.visualiser.FlowVisualiserLauncherActivity
import com.flow.visualiser.core.AutomaticFlowTracker
import com.flow.visualiser.core.ReactiveStreamTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Main plugin class for initializing and controlling the Flow Visualizer
 * This serves as the entry point for external apps to use Flow Visualizer
 */
object FlowVisualizer {
    private const val TAG = "FlowVisualizer"
    private var isInitialized = false
    private var config: FlowVisualizerConfig = FlowVisualizerConfig.default()
    
    /**
     * Initialize the Flow Visualizer plugin
     * This should be called from your Application class or main activity
     * 
     * @param application Your application instance
     * @param config Optional custom configuration
     */
    @JvmOverloads
    fun init(
        application: Application, 
        config: FlowVisualizerConfig = FlowVisualizerConfig.default()
    ) {
        if (isInitialized) {
            Log.w(TAG, "FlowVisualizer is already initialized")
            return
        }
        
        Log.i(TAG, "Initializing FlowVisualizer plugin")
        this.config = config
        ReactiveStreamTracker.setConfig(config)
        
        isInitialized = true
        
        // Start notification service if enabled to provide quick access
        if (config.notificationEnabled) {
            FlowVisualizerNotificationService.start(application)
        }

        // Initialize automatic flow tracking - this will discover and track flows
        // in your app without requiring explicit tracking code
        AutomaticFlowTracker.init(application)
        Log.i(TAG, "Automatic flow tracking enabled - flows will be tracked without requiring code changes")
    }
    
    /**
     * Launch the Flow Visualizer UI in standalone mode
     * This launches a separate activity that won't interfere with your app's UI
     * 
     * @param context The context to launch from
     */
    fun launch(context: Context) {
        if (!isInitialized) {
            Log.w(TAG, "FlowVisualizer is not initialized, call init() first")
            return
        }
        
        val intent = FlowVisualiserLauncherActivity.createIntent(context)
        context.startActivity(intent)
    }
    
    /**
     * Shutdown and cleanup resources used by the Flow Visualizer
     */
    fun shutdown(context: Context) {
        if (!isInitialized) {
            return
        }
        
        Log.i(TAG, "Shutting down FlowVisualizer plugin")
        
        // Stop notification service if enabled
        if (config.notificationEnabled) {
            FlowVisualizerNotificationService.stop(context)
        }
        
        // Disable automatic tracking
        AutomaticFlowTracker.setEnabled(false)
        
        ReactiveStreamTracker.reset()
        isInitialized = false
    }
    
    /**
     * Enable or disable tracking of reactive stream events
     */
    fun setTrackingEnabled(enabled: Boolean) {
        ReactiveStreamTracker.setTrackingEnabled(enabled)
        AutomaticFlowTracker.setEnabled(enabled)
    }
    
    /**
     * Enable or disable automatic tracking (reflection-based discovery)
     * This is enabled by default
     */
    fun setAutomaticTrackingEnabled(enabled: Boolean) {
        AutomaticFlowTracker.setEnabled(enabled)
    }
    
    /**
     * Track a Flow explicitly
     * Note: With automatic tracking enabled, you typically don't need to call this method
     * 
     * @param flow The Flow to track
     * @param name A descriptive name for this flow
     * @return The same flow with tracking enabled
     */
    fun <T> trackFlow(flow: Flow<T>, name: String): Flow<T> {
        return ReactiveStreamTracker.trackFlow(flow, name)
    }
    
    /**
     * Track a StateFlow explicitly
     * Note: With automatic tracking enabled, you typically don't need to call this method
     * 
     * @param stateFlow The StateFlow to track
     * @param name A descriptive name for this StateFlow
     * @return The same StateFlow with tracking enabled
     */
    fun <T> trackStateFlow(stateFlow: StateFlow<T>, name: String): StateFlow<T> {
        return ReactiveStreamTracker.trackStateFlow(stateFlow, name)
    }
} 