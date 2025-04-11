package com.flow.visualiser.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flow.visualiser.model.StreamType
import com.flow.visualiser.viewmodel.FlowEventUI
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlowEventCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun flowEventCard_displays_properly() {
        // Create a sample FlowEventUI
        val testEvent = FlowEventUI(
            id = "test-id",
            time = "12:34:56.789",
            type = "EMISSION",
            content = "Test value",
            color = "#4CAF50",
            streamName = "Test Flow",
            streamType = StreamType.FLOW
        )
        
        // Set the content of the compose rule to be a FlowEventCard with the test event
        composeTestRule.setContent {
            FlowEventCard(event = testEvent)
        }
        
        // Print the tree for debugging
        composeTestRule.onRoot().printToLog("FLOW_EVENT_CARD")
    }
} 