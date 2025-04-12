package com.flow.visualiser.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * AutomaticFlowTracker uses reflection to discover and track flows 
 * without requiring explicit tracking code.
 */
object AutomaticFlowTracker {
    private const val TAG = "AutomaticFlowTracker"
    private var isEnabled = true
    
    // Track fields that have already been tracked to avoid duplicate tracking
    private val trackedFields = mutableSetOf<String>()
    
    /**
     * Initialize automatic tracking
     */
    fun init(application: Application) {
        // Register activity lifecycle callbacks to track flows in activities
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Track flows in the activity
                trackActivityFlows(activity)
            }
            
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        
        Log.i(TAG, "Automatic flow tracking initialized")
    }
    
    /**
     * Enable or disable automatic tracking
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    /**
     * Track flows in an activity using reflection
     */
    fun trackActivityFlows(activity: Activity) {
        if (!isEnabled) return
        
        try {
            // Track ViewModels in the activity
            if (activity is ViewModelStoreOwner) {
                trackViewModelsInOwner(activity)
            }
            
            // Track fields directly in the activity
            trackFieldsInObject(activity, activity.javaClass.simpleName)
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking activity flows: ${e.message}")
        }
    }
    
    /**
     * Track ViewModels in a ViewModelStoreOwner
     */
    private fun trackViewModelsInOwner(owner: ViewModelStoreOwner) {
        if (!isEnabled) return
        
        try {
            // This is a simplification - in a real implementation, we'd need to 
            // access the ViewModelStore of the owner and extract ViewModels
            // For now, we'll just look for common ViewModel patterns
            
            // Scan fields that might be ViewModels
            val ownerClass = owner.javaClass
            val ownerName = ownerClass.simpleName
            
            for (field in ownerClass.declaredFields) {
                field.isAccessible = true
                
                val fieldType = field.type
                
                // Check if the field is a ViewModel or inherits from ViewModel
                if (ViewModel::class.java.isAssignableFrom(fieldType)) {
                    try {
                        val viewModel = field.get(owner) as? ViewModel
                        if (viewModel != null) {
                            val viewModelName = "${ownerName}_${field.name}_${viewModel.javaClass.simpleName}"
                            trackFieldsInObject(viewModel, viewModelName)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error accessing ViewModel field: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking ViewModels: ${e.message}")
        }
    }
    
    /**
     * Track all Flow-related fields in an object using reflection
     */
    private fun trackFieldsInObject(obj: Any, objectName: String) {
        if (!isEnabled) return
        
        try {
            val fields = getAllFields(obj.javaClass)
            
            for (field in fields) {
                try {
                    field.isAccessible = true
                    
                    // Skip static fields
                    if (Modifier.isStatic(field.modifiers)) continue
                    
                    // Skip fields we've already tracked
                    val fieldKey = "${obj.javaClass.name}.${field.name}"
                    if (fieldKey in trackedFields) continue
                    
                    // Check if the field is a StateFlow or Flow
                    when {
                        StateFlow::class.java.isAssignableFrom(field.type) -> {
                            val stateFlow = field.get(obj) as? StateFlow<*>
                            if (stateFlow != null) {
                                val flowName = "${objectName}_${field.name}"
                                trackedFields.add(fieldKey)
                                ReactiveStreamTracker.trackStateFlow(stateFlow, flowName)
                                Log.d(TAG, "Automatically tracking StateFlow: $flowName")
                            }
                        }
                        MutableStateFlow::class.java.isAssignableFrom(field.type) -> {
                            val mutableStateFlow = field.get(obj) as? MutableStateFlow<*>
                            if (mutableStateFlow != null) {
                                val flowName = "${objectName}_${field.name}"
                                trackedFields.add(fieldKey)
                                ReactiveStreamTracker.registerMutableStateFlow(mutableStateFlow, flowName)
                                Log.d(TAG, "Automatically tracking MutableStateFlow: $flowName")
                            }
                        }
                        Flow::class.java.isAssignableFrom(field.type) -> {
                            // Regular flows are harder to track automatically
                            // since we need a collection point
                            val flow = field.get(obj) as? Flow<*>
                            if (flow != null) {
                                val flowName = "${objectName}_${field.name}"
                                Log.d(TAG, "Found Flow: $flowName (not automatically tracked)")
                                // We can't directly track a Flow without a collection point
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors accessing individual fields
                    Log.d(TAG, "Error accessing field ${field.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking fields in object: ${e.message}")
        }
    }
    
    /**
     * Get all fields from a class and its superclasses
     */
    private fun getAllFields(clazz: Class<*>): List<Field> {
        val fields = mutableListOf<Field>()
        
        var currentClass: Class<*>? = clazz
        while (currentClass != null && currentClass != Any::class.java) {
            fields.addAll(currentClass.declaredFields)
            currentClass = currentClass.superclass
        }
        
        return fields
    }
} 