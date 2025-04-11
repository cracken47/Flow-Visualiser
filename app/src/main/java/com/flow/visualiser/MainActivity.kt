package com.flow.visualiser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.flow.visualiser.ui.screens.FlowDemoScreen
import com.flow.visualiser.ui.screens.FlowVisualizerScreen
import com.flow.visualiser.ui.theme.FlowVisualiserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FlowVisualiserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlowVisualiserApp()
                }
            }
        }
    }
}

@Composable
fun FlowVisualiserApp() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Visualizer", "Examples")
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Visualizer") },
                    label = { Text("Visualizer") }
                )
                
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Examples") },
                    label = { Text("Examples") }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> FlowVisualizerScreen(Modifier.padding(paddingValues))
            1 -> FlowDemoScreen(Modifier.padding(paddingValues))
        }
    }
}