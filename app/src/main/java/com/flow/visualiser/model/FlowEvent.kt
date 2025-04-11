package com.flow.visualiser.model

import java.util.UUID

/**
 * Represents an event in a reactive stream to be visualized
 */
sealed class FlowEvent<T> {
    val id: String = UUID.randomUUID().toString()
    val timestamp: Long = System.currentTimeMillis()
    abstract val streamName: String
    abstract val streamType: StreamType

    /**
     * Represents a value emitted by a stream
     */
    data class Emission<T>(
        val value: T, 
        override val streamName: String = "",
        override val streamType: StreamType = StreamType.FLOW
    ) : FlowEvent<T>()

    /**
     * Represents an error in the stream
     */
    data class Error<T>(
        val throwable: Throwable,
        override val streamName: String = "",
        override val streamType: StreamType = StreamType.FLOW
    ) : FlowEvent<T>()

    /**
     * Represents the completion of a stream
     */
    class Completion<T>(
        override val streamName: String = "",
        override val streamType: StreamType = StreamType.FLOW
    ) : FlowEvent<T>()
    
    /**
     * Represents when a stream has started
     */
    class Started<T>(
        override val streamName: String = "",
        override val streamType: StreamType = StreamType.FLOW
    ) : FlowEvent<T>()
    
    /**
     * Represents when a stream has been cancelled
     */
    class Cancelled<T>(
        override val streamName: String = "",
        override val streamType: StreamType = StreamType.FLOW
    ) : FlowEvent<T>()
} 