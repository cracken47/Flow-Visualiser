package com.flow.visualiser.plugin

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.regex.Pattern

/**
 * Code scanner for detecting reactive streams in the codebase
 * This is a simplified implementation for demonstration purposes
 * A real implementation would integrate with Android Studio's PSI
 */
class CodeScanner private constructor(
    private val projectRoot: File,
    private val scanIntervalMs: Long,
    private val includeThirdPartyLibraries: Boolean
) {
    private val TAG = "CodeScanner"
    
    private val scannerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var scanJob: Job? = null
    
    private val kotlinFileExtension = ".kt"
    private val javaFileExtension = ".java"
    
    // Regular expressions for detecting reactive streams in code
    private val flowPattern = Pattern.compile(
        "(val|var|lateinit var)\\s+\\w+\\s*[:=]\\s*(\\w+\\.)*Flow<.*>"
    )
    private val stateFlowPattern = Pattern.compile(
        "(val|var|lateinit var)\\s+\\w+\\s*[:=]\\s*(\\w+\\.)*StateFlow<.*>"
    )
    private val mutableStateFlowPattern = Pattern.compile(
        "(val|var|lateinit var)\\s+\\w+\\s*[:=]\\s*(\\w+\\.)*MutableStateFlow<.*>\\(.*\\)"
    )
    private val liveDataPattern = Pattern.compile(
        "(val|var|lateinit var)\\s+\\w+\\s*[:=]\\s*(\\w+\\.)*LiveData<.*>"
    )
    private val mutableLiveDataPattern = Pattern.compile(
        "(val|var|lateinit var)\\s+\\w+\\s*[:=]\\s*(\\w+\\.)*MutableLiveData<.*>\\(.*\\)"
    )
    
    // Scanner results
    private val detectedStreams = mutableListOf<DetectedStream>()
    private val listeners = mutableListOf<(List<DetectedStream>) -> Unit>()
    
    /**
     * Start scanning for reactive streams in the codebase
     */
    fun startScanning() {
        if (scanJob != null) {
            Log.w(TAG, "Scanner is already running")
            return
        }
        
        Log.i(TAG, "Starting code scanner with interval: $scanIntervalMs ms")
        
        scanJob = scannerScope.launch {
            while (true) {
                try {
                    val streams = scanCodebase()
                    reportResults(streams)
                } catch (e: Exception) {
                    Log.e(TAG, "Error scanning codebase: ${e.message}")
                }
                
                delay(scanIntervalMs)
            }
        }
    }
    
    /**
     * Stop the scanner
     */
    fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
        Log.i(TAG, "Code scanner stopped")
    }
    
    /**
     * Add a listener to be notified of scanner results
     */
    fun addListener(listener: (List<DetectedStream>) -> Unit) {
        listeners.add(listener)
    }
    
    /**
     * Remove a listener
     */
    fun removeListener(listener: (List<DetectedStream>) -> Unit) {
        listeners.remove(listener)
    }
    
    /**
     * Manually scan a specific file
     */
    fun scanFile(filePath: String): List<DetectedStream> {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            return emptyList()
        }
        
        return scanSingleFile(file)
    }
    
    /**
     * Scan the entire codebase for reactive streams
     */
    private fun scanCodebase(): List<DetectedStream> {
        detectedStreams.clear()
        
        Log.d(TAG, "Scanning codebase from root: ${projectRoot.absolutePath}")
        scanDirectory(projectRoot)
        
        return detectedStreams.toList()
    }
    
    /**
     * Scan a directory recursively
     */
    private fun scanDirectory(directory: File) {
        if (!directory.isDirectory) {
            return
        }
        
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                // Skip third-party libraries if configured to do so
                if (includeThirdPartyLibraries || !isThirdPartyLibrary(file)) {
                    scanDirectory(file)
                }
            } else if (isSourceFile(file)) {
                val streams = scanSingleFile(file)
                detectedStreams.addAll(streams)
            }
        }
    }
    
    /**
     * Check if a file is a source file (Kotlin or Java)
     */
    private fun isSourceFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(kotlinFileExtension) || name.endsWith(javaFileExtension)
    }
    
    /**
     * Check if a directory appears to be a third-party library
     */
    private fun isThirdPartyLibrary(directory: File): Boolean {
        val path = directory.absolutePath
        return path.contains("/build/") ||
            path.contains("/.gradle/") ||
            path.contains("/gradle/") ||
            path.contains("/generated/")
    }
    
    /**
     * Scan a single file for reactive streams
     */
    private fun scanSingleFile(file: File): List<DetectedStream> {
        val results = mutableListOf<DetectedStream>()
        val content = file.readText()
        val lines = content.lines()
        
        // Scan for flow declarations
        scanWithPattern(flowPattern, lines, file, StreamType.FLOW).let { results.addAll(it) }
        
        // Scan for StateFlow declarations
        scanWithPattern(stateFlowPattern, lines, file, StreamType.STATE_FLOW).let { results.addAll(it) }
        scanWithPattern(mutableStateFlowPattern, lines, file, StreamType.STATE_FLOW).let { results.addAll(it) }
        
        // Scan for LiveData declarations
        scanWithPattern(liveDataPattern, lines, file, StreamType.LIVE_DATA).let { results.addAll(it) }
        scanWithPattern(mutableLiveDataPattern, lines, file, StreamType.LIVE_DATA).let { results.addAll(it) }
        
        return results
    }
    
    /**
     * Scan file lines with a regex pattern
     */
    private fun scanWithPattern(
        pattern: Pattern,
        lines: List<String>,
        file: File,
        streamType: StreamType
    ): List<DetectedStream> {
        val results = mutableListOf<DetectedStream>()
        
        lines.forEachIndexed { lineNumber, line ->
            val matcher = pattern.matcher(line)
            if (matcher.find()) {
                val match = matcher.group(0)
                val variableName = extractVariableName(match)
                
                results.add(
                    DetectedStream(
                        name = variableName ?: "Unknown",
                        type = streamType,
                        filePath = file.absolutePath,
                        lineNumber = lineNumber + 1,
                        snippet = line.trim()
                    )
                )
            }
        }
        
        return results
    }
    
    /**
     * Extract variable name from a declaration
     */
    private fun extractVariableName(declaration: String): String? {
        // Simple regex to extract variable name
        val namePattern = Pattern.compile("(val|var|lateinit var)\\s+(\\w+)")
        val matcher = namePattern.matcher(declaration)
        
        return if (matcher.find()) {
            matcher.group(2)
        } else {
            null
        }
    }
    
    /**
     * Report scan results to listeners
     */
    private fun reportResults(streams: List<DetectedStream>) {
        listeners.forEach { listener ->
            listener(streams)
        }
    }
    
    companion object {
        /**
         * Create a new code scanner
         */
        fun create(
            projectRoot: File,
            scanIntervalMs: Long = 5000L,
            includeThirdPartyLibraries: Boolean = false
        ): CodeScanner {
            return CodeScanner(projectRoot, scanIntervalMs, includeThirdPartyLibraries)
        }
    }
    
    /**
     * Represents a detected reactive stream in the codebase
     */
    data class DetectedStream(
        val name: String,
        val type: StreamType,
        val filePath: String,
        val lineNumber: Int,
        val snippet: String
    )
    
    /**
     * Types of reactive streams that can be detected
     */
    enum class StreamType {
        FLOW,
        STATE_FLOW,
        LIVE_DATA
    }
} 