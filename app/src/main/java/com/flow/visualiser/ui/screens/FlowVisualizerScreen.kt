package com.flow.visualiser.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flow.visualiser.core.ReactiveStreamTracker
import com.flow.visualiser.core.trackFlow
import com.flow.visualiser.model.StreamType
import com.flow.visualiser.ui.components.FlowEventCard
import com.flow.visualiser.viewmodel.FlowVisualizerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Main screen for the reactive stream Visualizer
 */
@Composable
fun FlowVisualizerScreen(
    modifier: Modifier = Modifier,
    viewModel: FlowVisualizerViewModel = viewModel()
) {
    val events by viewModel.flowEvents.collectAsState()
    val showFlows by viewModel.showFlowEvents.collectAsState()
    val showStateFlows by viewModel.showStateFlowEvents.collectAsState()
    val showLiveData by viewModel.showLiveDataEvents.collectAsState()
    val activeStreams by viewModel.activeStreams.collectAsState()
    
    val scope = rememberCoroutineScope()
    var isFlowRunning by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Text(
            text = "Reactive Stream Visualizer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        // Example controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!isFlowRunning) {
                        isFlowRunning = true
                        scope.launch {
                            try {
                                // Create and track a simple example flow
                                // Add catch to prevent app crash while still showing the error in the UI
                                createExampleFlow()
                                    .trackFlow("Example Flow")
                                    .catch { error -> 
                                        // Just catch the error so the app doesn't crash
                                        // The error will still be displayed in the visualizer
                                        println("Error caught: ${error.message}")
                                    }
                                    .collect { 
                                        // Just collect to trigger the flow
                                    }
                            } finally {
                                isFlowRunning = false
                            }
                        }
                    }
                },
                enabled = !isFlowRunning
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                Text(text = "Run Example Flow")
            }
            
            FilledTonalButton(
                onClick = { viewModel.clearEvents() }
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
                Text(text = "Clear Events")
            }
        }
        
        // Stream type filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showFlows,
                    onCheckedChange = { viewModel.toggleStreamTypeVisibility(StreamType.FLOW, it) }
                )
                Text("Flows")
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showStateFlows,
                    onCheckedChange = { viewModel.toggleStreamTypeVisibility(StreamType.STATE_FLOW, it) }
                )
                Text("StateFlows")
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showLiveData,
                    onCheckedChange = { viewModel.toggleStreamTypeVisibility(StreamType.LIVE_DATA, it) }
                )
                Text("LiveData")
            }
        }
        
        // Active streams counter
        if (activeStreams.isNotEmpty()) {
            Text(
                text = "Active streams: ${activeStreams.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Flow events display
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true // Show newest events at the top
        ) {
            val filteredEvents = viewModel.getFilteredEvents()
            items(filteredEvents.reversed(), key = { it.id }) { event ->
                FlowEventCard(event = event)
            }
        }
    }
}

/**
 * Creates an example flow that emits numbers and potentially errors for demonstration
 */
private fun createExampleFlow(): Flow<Int> = flow {
    for (i in 1..5) {
        delay(1000) // Emit every second
        
        // Randomly throw an error 20% of the time after the first value
        if (i > 1 && Random.nextFloat() < 0.2f) {
            throw RuntimeException("Random error occurred in example flow")
        }
        
        emit(i)
    }
} 