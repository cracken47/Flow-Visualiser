# Flow Visualiser Integration Guide

This guide explains how to integrate the Flow Visualiser into your Android application to debug and monitor Kotlin Flow, StateFlow, and LiveData streams.

## Key Features

- **Zero-Code Tracking**: Automatically discovers and tracks flows without any code modifications
- **Standalone Mode**: Visualizer runs in its own window, separate from your app
- **Non-intrusive**: Will not affect your app's UI or performance
- **Reflection-Based Discovery**: Uses reflection to find flows in Activities, Fragments, and ViewModels
- **Floating Trigger**: Optional floating button to access visualizer from anywhere

## Integration Steps

### Step 1: Add the Dependency

Add Flow Visualiser to your app's build.gradle file:

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.cracken47:flow-visualiser:1.0.0")
}
```

Or in Groovy:

```groovy
// build.gradle
dependencies {
    implementation 'io.github.cracken47:flow-visualiser:1.0.0'
}
```

### Step 2: Initialize in Your Application Class

Add Flow Visualiser initialization to your Application class:

```kotlin
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Flow Visualiser - that's it!
        // It will automatically discover and track all flows in your app
        FlowVisualizer.init(this)
    }
}
```

Don't forget to register your Application class in AndroidManifest.xml:

```xml
<application
    android:name=".MyApplication"
    ...>
```

### Step 3: Add Permission for Overlay (Optional, for Floating Trigger)

If you want to use the floating trigger button, add this permission to your AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

And request the permission at runtime:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:$packageName")
    )
    startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
}
```

## How Automatic Flow Tracking Works

Flow Visualiser automatically discovers and tracks reactive streams in your app without requiring any code modifications:

1. It uses reflection to scan Activities, Fragments, and ViewModels for Flow-related fields
2. When a StateFlow, MutableStateFlow, or LiveData is found, it's automatically tracked
3. Each discovered flow is given a name based on its containing class and field name
4. The tracking is done without modifying your app's behavior or performance

This means you can simply initialize the library once, and all your flows will be tracked automatically!

## Opening the Visualizer

### Option 1: Use the Notification

Once initialized, Flow Visualiser creates a notification that provides quick access. Simply tap the notification to open the visualizer.

### Option 2: Add a Floating Trigger

Add a floating button that users can tap to open the visualizer:

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Add the floating trigger button
        FlowVisualiserTrigger.addTo(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Remove the trigger when activity is destroyed
        FlowVisualiserTrigger.remove()
    }
}
```

### Option 3: Launch Manually

Launch the visualizer programmatically:

```kotlin
// In your activity or fragment
val launchButton = findViewById<Button>(R.id.launch_visualizer)
launchButton.setOnClickListener {
    FlowVisualizer.launch(context)
}
```

## Manual Tracking (Optional)

While automatic tracking should discover most flows, you can still explicitly track flows if needed:

```kotlin
// For regular Flows
myFlow.trackFlow("My Flow").collect { /* ... */ }

// For StateFlows
val stateFlow = myStateFlow.trackStateFlow("State Flow")

// For MutableStateFlows
private val _state = MutableStateFlow(State()).trackMutableStateFlow("Mutable State")

// For LiveData
val liveData = myLiveData.trackLiveData("My LiveData")
```

## Troubleshooting

### No Flows Are Being Captured

If no flows are being captured:

1. Make sure you've initialized FlowVisualizer in your Application class
2. Verify that your flows are in Activities, Fragments, or ViewModels (where automatic discovery looks)
3. For more complex cases, try the manual tracking methods

### App Crashing When Using the Floating Trigger

Make sure you've:
1. Added the SYSTEM_ALERT_WINDOW permission
2. Requested the permission at runtime
3. Called FlowVisualiserTrigger.remove() in onDestroy()

### Performance Issues

If you're experiencing performance issues:
1. Disable automatic tracking for specific screens:
   ```kotlin
   // Temporarily disable automatic tracking
   FlowVisualizer.setAutomaticTrackingEnabled(false)
   ```
2. Reduce the maxEvents in FlowVisualizerConfig
3. Be selective about which flows you track

## Need Help?

If you encounter any issues, please open an issue on GitHub:
https://github.com/cracken47/Flow-Visualiser/issues 