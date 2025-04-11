package com.flow.visualiser.plugin

/**
 * Configuration for the Flow Visualizer plugin
 */
class FlowVisualizerConfig private constructor(
    val maxEvents: Int,
    val enableLogging: Boolean,
    val defaultTheme: FlowVisualizerTheme,
    val notificationEnabled: Boolean,
    val codeScannerEnabled: Boolean,
    val scanIntervalMs: Long,
    val includeThirdPartyLibraries: Boolean,
    val debuggerIntegrationEnabled: Boolean
) {
    
    /**
     * Builder class for creating FlowVisualizerConfig instances
     */
    class Builder {
        private var maxEvents: Int = 100
        private var enableLogging: Boolean = false
        private var defaultTheme: FlowVisualizerTheme = FlowVisualizerTheme.SYSTEM
        private var notificationEnabled: Boolean = true
        private var codeScannerEnabled: Boolean = false
        private var scanIntervalMs: Long = 5000L
        private var includeThirdPartyLibraries: Boolean = false
        private var debuggerIntegrationEnabled: Boolean = true
        
        /**
         * Set the maximum number of events to keep in history
         */
        fun maxEvents(max: Int): Builder = apply {
            maxEvents = if (max > 0) max else 100
        }
        
        /**
         * Enable or disable debug logging
         */
        fun enableLogging(enabled: Boolean): Builder = apply {
            enableLogging = enabled
        }
        
        /**
         * Set the default theme for the visualizer UI
         */
        fun defaultTheme(theme: FlowVisualizerTheme): Builder = apply {
            defaultTheme = theme
        }
        
        /**
         * Enable or disable the notification for quick access
         */
        fun notificationEnabled(enabled: Boolean): Builder = apply {
            notificationEnabled = enabled
        }
        
        /**
         * Enable or disable the code scanner
         */
        fun codeScannerEnabled(enabled: Boolean): Builder = apply {
            codeScannerEnabled = enabled
        }
        
        /**
         * Set the interval for the code scanner in milliseconds
         */
        fun scanIntervalMs(intervalMs: Long): Builder = apply {
            scanIntervalMs = if (intervalMs > 0) intervalMs else 5000L
        }
        
        /**
         * Include third party libraries in the code scanner
         */
        fun includeThirdPartyLibraries(include: Boolean): Builder = apply {
            includeThirdPartyLibraries = include
        }
        
        /**
         * Enable or disable integration with the Android Studio debugger
         */
        fun debuggerIntegrationEnabled(enabled: Boolean): Builder = apply {
            debuggerIntegrationEnabled = enabled
        }
        
        /**
         * Build the configuration object
         */
        fun build(): FlowVisualizerConfig {
            return FlowVisualizerConfig(
                maxEvents = maxEvents,
                enableLogging = enableLogging,
                defaultTheme = defaultTheme,
                notificationEnabled = notificationEnabled,
                codeScannerEnabled = codeScannerEnabled,
                scanIntervalMs = scanIntervalMs,
                includeThirdPartyLibraries = includeThirdPartyLibraries,
                debuggerIntegrationEnabled = debuggerIntegrationEnabled
            )
        }
    }
    
    companion object {
        /**
         * Create a new builder for FlowVisualizerConfig
         */
        fun builder(): Builder = Builder()
        
        /**
         * Create a default configuration
         */
        fun default(): FlowVisualizerConfig = Builder().build()
    }
}

/**
 * Theme options for the Flow Visualizer UI
 */
enum class FlowVisualizerTheme {
    LIGHT,
    DARK,
    SYSTEM
} 