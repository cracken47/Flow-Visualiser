package com.flow.visualiser.plugin

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import androidx.core.content.ContextCompat

/**
 * A floating button trigger that can be added to any app to launch the Flow Visualiser
 * This provides a non-intrusive way to access the visualizer from any screen
 */
class FlowVisualiserTrigger private constructor(private val context: Context) {
    
    companion object {
        private var instance: FlowVisualiserTrigger? = null
        
        /**
         * Add a floating button trigger to the current activity
         * 
         * @param activity The activity to add the trigger to
         * @return The FlowVisualiserTrigger instance
         */
        fun addTo(activity: Activity): FlowVisualiserTrigger {
            return instance ?: FlowVisualiserTrigger(activity.applicationContext).also {
                instance = it
                it.show()
            }
        }
        
        /**
         * Remove the floating button trigger
         */
        fun remove() {
            instance?.hide()
            instance = null
        }
    }
    
    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val triggerButton: ImageButton = ImageButton(context)
    private var isShowing = false
    
    // Initial position coordinates
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    init {
        // Configure the trigger button appearance
        triggerButton.apply {
            setImageResource(android.R.drawable.ic_menu_share) // Replace with custom icon
            background = ContextCompat.getDrawable(context, android.R.drawable.btn_default)
            alpha = 0.7f
        }
        
        // Handle touch events for drag and tap
        triggerButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Record initial positions for drag
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Update position based on drag
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    
                    // Update the layout
                    if (isShowing) {
                        windowManager.updateViewLayout(triggerButton, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Detect if this was a tap or a drag
                    val deltaX = Math.abs(event.rawX - initialTouchX)
                    val deltaY = Math.abs(event.rawY - initialTouchY)
                    
                    if (deltaX < 10 && deltaY < 10) {
                        // This was a tap, launch the visualizer
                        FlowVisualizer.launch(context)
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    // Window layout parameters
    private val params = WindowManager.LayoutParams().apply {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        format = PixelFormat.TRANSLUCENT
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 100
    }
    
    /**
     * Show the floating trigger button
     */
    fun show() {
        if (!isShowing) {
            try {
                windowManager.addView(triggerButton, params)
                isShowing = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Hide the floating trigger button
     */
    fun hide() {
        if (isShowing) {
            try {
                windowManager.removeView(triggerButton)
                isShowing = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 