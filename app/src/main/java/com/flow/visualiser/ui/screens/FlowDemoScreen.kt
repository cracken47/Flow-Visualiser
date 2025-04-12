package com.flow.visualiser.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flow.visualiser.core.trackFlow
import com.flow.visualiser.core.trackLiveData
import com.flow.visualiser.core.trackOperator
import com.flow.visualiser.core.trackStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

/**
 * Screen with various reactive stream examples for demonstration purposes
 */
@Composable
fun FlowDemoScreen(
    modifier: Modifier = Modifier,
    demoViewModel: DemoViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Track which examples are running
    var isBasicFlowRunning by remember { mutableStateOf(false) }
    var isStateFlowRunning by remember { mutableStateOf(false) }
    var isLiveDataRunning by remember { mutableStateOf(false) }
    var isCombineFlowRunning by remember { mutableStateOf(false) }
    var isErrorFlowRunning by remember { mutableStateOf(false) }
    
    // Get the current lifecycle owner
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Clean up LiveData observers when the screen is disposed
    DisposableEffect(lifecycleOwner) {
        onDispose {
            demoViewModel.cleanUp()
        }
    }
    
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reactive Stream Examples",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Basic Flow Example
        DemoCard(
            title = "Basic Flow",
            description = "A simple flow that emits 5 integers with a 1-second delay between each emission.",
            isRunning = isBasicFlowRunning,
            onRun = {
                if (!isBasicFlowRunning) {
                    isBasicFlowRunning = true
                    scope.launch {
                        try {
                            createBasicFlow()
                                .trackFlow("Basic Flow")
                                .catch { error -> 
                                    // Just catch the error so the app doesn't crash
                                    println("Error caught: ${error.message}")
                                }
                                .collect { /* Just collect to trigger flow */ }
                        } finally {
                            isBasicFlowRunning = false
                        }
                    }
                }
            }
        )
        
        // StateFlow Example
        DemoCard(
            title = "StateFlow",
            description = "Updates a StateFlow with new values every second, showing state changes.",
            isRunning = isStateFlowRunning,
            onRun = {
                if (!isStateFlowRunning) {
                    isStateFlowRunning = true
                    demoViewModel.runStateFlowDemo()
                    scope.launch {
                        delay(6000) // Run for 6 seconds
                        isStateFlowRunning = false
                    }
                }
            }
        )
        
        // LiveData Example
        DemoCard(
            title = "LiveData",
            description = "Updates a LiveData with new values every second, showing value changes.",
            isRunning = isLiveDataRunning,
            onRun = {
                if (!isLiveDataRunning) {
                    isLiveDataRunning = true
                    demoViewModel.runLiveDataDemo()
                    scope.launch {
                        delay(6000) // Run for 6 seconds
                        isLiveDataRunning = false
                    }
                }
            }
        )
        
        // Flow Transformations
        DemoCard(
            title = "Flow with Transformations",
            description = "A flow with map, filter, and other transformations applied.",
            isRunning = isBasicFlowRunning,
            onRun = {
                if (!isBasicFlowRunning) {
                    isBasicFlowRunning = true
                    scope.launch {
                        try {
                            createBasicFlow()
                                .trackFlow("Source Flow")
                                .map { it * 10 }
                                .trackOperator("After map: value * 10")
                                .filter { it > 20 }
                                .trackOperator("After filter: value > 20")
                                .onEach { delay(500) }
                                .collect { /* Just collect to trigger flow */ }
                        } finally {
                            isBasicFlowRunning = false
                        }
                    }
                }
            }
        )
        
        // Flow Combination Example
        DemoCard(
            title = "Flow Combination",
            description = "Examples of combining flows with zip, combine, and merge operators.",
            isRunning = isCombineFlowRunning,
            onRun = {
                if (!isCombineFlowRunning) {
                    isCombineFlowRunning = true
                    scope.launch {
                        try {
                            val flow1 = flow {
                                for (i in 1..3) {
                                    delay(800)
                                    emit("A$i")
                                }
                            }.trackFlow("Flow A")
                            
                            val flow2 = flow {
                                for (i in 1..4) {
                                    delay(1000)
                                    emit("B$i")
                                }
                            }.trackFlow("Flow B")
                            
                            // Use zip operator to combine emissions
                            flow1.zip(flow2) { a, b -> "$a + $b" }
                                .trackOperator("After zip")
                                .collect { /* Just collect to trigger flow */ }
                            
                            delay(1000) // Wait a bit before showing the combine example
                            
                            // Use combine operator
                            flow1.combine(flow2) { a, b -> "$a & $b" }
                                .trackOperator("After combine")
                                .collect { /* Just collect to trigger flow */ }
                                
                            delay(1000) // Wait a bit before showing the merge example
                            
                            // Use merge operator
                            merge(flow1, flow2)
                                .trackOperator("After merge")
                                .collect { /* Just collect to trigger flow */ }
                        } finally {
                            isCombineFlowRunning = false
                        }
                    }
                }
            }
        )
        
        // Error Handling Example
        DemoCard(
            title = "Error Handling",
            description = "Flow with error handling using catch and onCompletion operators.",
            isRunning = isErrorFlowRunning,
            onRun = {
                if (!isErrorFlowRunning) {
                    isErrorFlowRunning = true
                    scope.launch {
                        try {
                            createErrorFlow()
                                .trackFlow("Error-prone Flow")
                                .catch { error ->
                                    // Handle the error by emitting a fallback value
                                    emit("Error caught: ${error.message}")
                                }
                                .trackOperator("After catch operator")
                                .onCompletion { error ->
                                    // This is called when the flow completes, with or without error
                                    println("Flow completed with error: $error")
                                }
                                .collect { /* Just collect to trigger flow */ }
                        } finally {
                            isErrorFlowRunning = false
                        }
                    }
                }
            }
        )
    }
}

/**
 * A card for displaying a reactive stream example with description and run button
 */
@Composable
private fun DemoCard(
    title: String,
    description: String,
    isRunning: Boolean,
    onRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onRun,
                    enabled = !isRunning
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                    Text(text = if (isRunning) "Running..." else "Run Example")
                }
            }
        }
    }
}

/**
 * ViewModel for the demo screen that holds StateFlow and LiveData objects
 */
class DemoViewModel : ViewModel() {
    // StateFlow example
    private val _counterStateFlow = MutableStateFlow(0)
    val counterStateFlow: StateFlow<Int> = _counterStateFlow
        .trackStateFlow("Counter StateFlow")
    
    // LiveData example
    private val _counterLiveData = MutableLiveData(0)
    val counterLiveData: LiveData<Int> = _counterLiveData
        .trackLiveData("Counter LiveData")
    
    private var stateFlowActive = false
    private var liveDataActive = false
    
    fun runStateFlowDemo() {
        if (!stateFlowActive) {
            stateFlowActive = true
            
            viewModelScope.launch {
                try {
                    for (i in 1..5) {
                        _counterStateFlow.value = i
                        delay(1000)
                    }
                } finally {
                    stateFlowActive = false
                }
            }
        }
    }
    
    fun runLiveDataDemo() {
        if (!liveDataActive) {
            liveDataActive = true
            
            viewModelScope.launch {
                try {
                    for (i in 1..5) {
                        _counterLiveData.postValue(i)
                        delay(1000)
                    }
                } finally {
                    liveDataActive = false
                }
            }
        }
    }
    
    fun cleanUp() {
        // Any cleanup needed for LiveData observers
    }
}

// Example Flow generators
private fun createBasicFlow(): Flow<Int> = flow {
    for (i in 1..5) {
        delay(1000)
        emit(i)
    }
}.onStart { println("Flow started") }
 .onCompletion { println("Flow completed") }

private fun createErrorFlow(): Flow<String> = flow {
    for (i in 1..5) {
        delay(1000)
        
        if (i == 3) {
            throw RuntimeException("Simulated error at emission $i")
        }
        
        emit("Value $i")
    }
} 