package com.flow.visualiser.model

/**
 * Defines the different types of reactive streams that can be tracked
 */
enum class StreamType {
    FLOW,
    STATE_FLOW,
    LIVE_DATA,
    RX_OBSERVABLE,
    RX_SUBJECT,
    CHANNEL
} 