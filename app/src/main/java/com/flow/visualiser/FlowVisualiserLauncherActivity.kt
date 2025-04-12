package com.flow.visualiser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.flow.visualiser.ui.screens.FlowVisualizerScreen
import com.flow.visualiser.ui.theme.FlowVisualiserTheme

/**
 * Standalone launcher activity for Flow Visualiser
 * This activity can be used by other apps to launch the flow visualiser without
 * affecting their own UI or application structure.
 */
class FlowVisualiserLauncherActivity : ComponentActivity() {
    
    companion object {
        /**
         * Create an intent to launch the standalone Flow Visualiser
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, FlowVisualiserLauncherActivity::class.java).apply {
                // Set flags to launch as a new task - doesn't affect caller app
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FlowVisualiserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Display only the visualizer screen
                    FlowVisualizerScreen()
                }
            }
        }
    }
} 