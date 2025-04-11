package com.flow.visualiser.plugin

import android.content.Context
import android.content.Intent
import android.util.Log
import com.flow.visualiser.MainActivity
import com.flow.visualiser.core.ReactiveStreamTracker

/**
 * Main plugin class for initializing and controlling the Flow Visualizer
 * This serves as the entry point for the Android Studio plugin integration
 */
object FlowVisualizer {
    private const val TAG = "FlowVisualizer"
    private var isInitialized = false
    private var config: FlowVisualizerConfig = FlowVisualizerConfig.default()
    
    // Store code scanner settings
    private var scanCodeEnabled = false
    private var scanIntervalMs = 5000L // 5 seconds by default
    
    /**
     * Initialize the Flow Visualizer plugin
     * 
     * @param context The application context
     * @param config Optional custom configuration
     * @param autoStart Whether to automatically start the visualizer UI (default: false)
     */
    @JvmOverloads
    fun init(
        context: Context, 
        config: FlowVisualizerConfig = FlowVisualizerConfig.default(),
        autoStart: Boolean = false
    ) {
        if (isInitialized) {
            Log.w(TAG, "FlowVisualizer is already initialized")
            return
        }
        
        Log.i(TAG, "Initializing FlowVisualizer plugin")
        this.config = config
        ReactiveStreamTracker.setConfig(config)
        
        isInitialized = true
        
        // Start notification service if enabled
        if (config.notificationEnabled) {
            FlowVisualizerNotificationService.start(context)
        }
        
        // Start code scanner if enabled in config
        if (config.codeScannerEnabled) {
            startCodeScanner()
        }
        
        if (autoStart) {
            launch(context)
        }
    }
    
    /**
     * Launch the Flow Visualizer UI
     * 
     * @param context The context to launch from
     */
    fun launch(context: Context) {
        if (!isInitialized) {
            Log.w(TAG, "FlowVisualizer is not initialized, call init() first")
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
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
        
        // Stop code scanner
        stopCodeScanner()
        
        ReactiveStreamTracker.reset()
        isInitialized = false
    }
    
    /**
     * Enable or disable tracking of reactive stream events
     */
    fun setTrackingEnabled(enabled: Boolean) {
        ReactiveStreamTracker.setTrackingEnabled(enabled)
    }
    
    /**
     * Enable or disable debug logging
     */
    fun setDebugLogging(enabled: Boolean) {
        // Implementation for controlling debug logging
    }
    
    /**
     * Start the code scanner that automatically detects reactive streams in the codebase
     */
    fun startCodeScanner() {
        if (!isInitialized) {
            Log.w(TAG, "FlowVisualizer is not initialized, call init() first")
            return
        }
        
        scanCodeEnabled = true
        Log.i(TAG, "Starting code scanner with interval: $scanIntervalMs ms")
        
        // Implementation would connect to Android Studio's code analysis system
        // This would require IDE plugin integration via IntelliJ plugin SDK
    }
    
    /**
     * Stop the code scanner
     */
    fun stopCodeScanner() {
        scanCodeEnabled = false
        Log.i(TAG, "Stopping code scanner")
    }
    
    /**
     * Set the scan interval for the code scanner
     */
    fun setScanInterval(intervalMs: Long) {
        scanIntervalMs = intervalMs
        Log.i(TAG, "Set code scanner interval to: $intervalMs ms")
    }
    
    /**
     * For IDE plugin integration: Process a file to look for reactive streams
     * This would be called by the Android Studio plugin
     */
    fun processSourceFile(filePath: String, fileContent: String) {
        if (!scanCodeEnabled || !isInitialized) {
            return
        }
        
        Log.d(TAG, "Processing source file: $filePath")
        
        // Implementation would scan the file content for reactive streams
        // This is a placeholder for the actual implementation that would
        // be done in an IDE plugin using PSI (Program Structure Interface)
    }
    
    /**
     * For IDE plugin integration: Handle debugger connection
     * This would be called when the debugger is attached to enable runtime tracking
     */
    fun connectDebugger(debugSessionId: String) {
        Log.i(TAG, "Connected to debugger session: $debugSessionId")
        
        // Implementation would integrate with Android Studio debugger
        // to provide real-time value inspection of reactive streams
    }
    
    /**
     * For IDE plugin integration: Disconnect from debugger
     */
    fun disconnectDebugger() {
        Log.i(TAG, "Disconnected from debugger")
    }
} 