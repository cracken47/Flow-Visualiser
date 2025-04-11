package com.flow.visualiser.plugin

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Helper class for integrating with the Android Studio debugger
 * This is a placeholder implementation for demonstration purposes
 * A real implementation would use the Java Debug Interface (JDI) or
 * Android Studio's plugin API for debugger integration
 */
object DebuggerHelper {
    private const val TAG = "DebuggerHelper"
    
    private var isConnected = false
    private var debugSessionId: String? = null
    
    // Keep track of breakpoints set on reactive streams
    private val streamBreakpoints = mutableMapOf<String, StreamBreakpoint>()
    
    /**
     * Connect to the debugger
     * 
     * @param sessionId Unique identifier for the debug session
     */
    fun connect(sessionId: String) {
        if (isConnected) {
            Log.w(TAG, "Already connected to debugger")
            return
        }
        
        debugSessionId = sessionId
        isConnected = true
        Log.i(TAG, "Connected to debugger session: $sessionId")
    }
    
    /**
     * Disconnect from the debugger
     */
    fun disconnect() {
        if (!isConnected) {
            return
        }
        
        // Clean up breakpoints
        streamBreakpoints.clear()
        
        debugSessionId = null
        isConnected = false
        Log.i(TAG, "Disconnected from debugger")
    }
    
    /**
     * Check if connected to the debugger
     */
    fun isConnected(): Boolean = isConnected
    
    /**
     * Set a breakpoint on a flow emission
     * 
     * @param flow The flow to monitor
     * @param name Name of the flow for identification
     */
    fun <T> watchFlow(flow: Flow<T>, name: String) {
        if (!isConnected) {
            Log.w(TAG, "Not connected to debugger")
            return
        }
        
        // In a real implementation, this would register a breakpoint with the debugger
        val breakpointId = "flow-$name-${System.currentTimeMillis()}"
        streamBreakpoints[breakpointId] = StreamBreakpoint(
            id = breakpointId,
            name = name,
            type = "Flow",
            condition = null
        )
        
        Log.i(TAG, "Set breakpoint on Flow: $name (ID: $breakpointId)")
    }
    
    /**
     * Set a breakpoint on a StateFlow value change
     * 
     * @param stateFlow The StateFlow to monitor
     * @param name Name of the StateFlow for identification
     * @param condition Optional condition for when to break
     */
    fun <T> watchStateFlow(stateFlow: StateFlow<T>, name: String, condition: String? = null) {
        if (!isConnected) {
            Log.w(TAG, "Not connected to debugger")
            return
        }
        
        // In a real implementation, this would register a conditional breakpoint
        val breakpointId = "stateflow-$name-${System.currentTimeMillis()}"
        streamBreakpoints[breakpointId] = StreamBreakpoint(
            id = breakpointId,
            name = name,
            type = "StateFlow",
            condition = condition
        )
        
        Log.i(TAG, "Set breakpoint on StateFlow: $name (ID: $breakpointId)")
    }
    
    /**
     * Remove a breakpoint by ID
     */
    fun removeBreakpoint(breakpointId: String) {
        if (!isConnected || !streamBreakpoints.containsKey(breakpointId)) {
            return
        }
        
        streamBreakpoints.remove(breakpointId)
        Log.i(TAG, "Removed breakpoint: $breakpointId")
    }
    
    /**
     * Get all active breakpoints
     */
    fun getActiveBreakpoints(): List<StreamBreakpoint> {
        return streamBreakpoints.values.toList()
    }
    
    /**
     * Represents a breakpoint set on a reactive stream
     */
    data class StreamBreakpoint(
        val id: String,
        val name: String,
        val type: String,
        val condition: String?
    )
} 