# Flow Visualizer

A powerful debugging and visualization tool for Kotlin Flow, StateFlow, LiveData, and other reactive streams in Android applications.

## Features

- Real-time visualization of reactive stream emissions, completions, and errors
- Support for multiple reactive stream types:
  - Kotlin Flow
  - StateFlow
  - LiveData
- Visual tracking of active streams and event history
- Easy integration with zero code changes
- Automatic code scanning to detect reactive streams in your codebase
- Integration with Android Studio for debugging
- Comprehensive test suite

## Screenshots

![Flow Visualizer Screenshot](screenshots/flow_visualizer.png)

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Kotlin 2.0.0 or higher
- Android SDK 24+

### Installation

1. Clone this repository:
```bash
git clone https://github.com/cracken47/Flow-Visualiser.git
```

2. Open the project in Android Studio and run it on your device or emulator.

## Usage

The Flow Visualizer automatically detects and visualizes your reactive streams with **zero code modifications**. Simply integrate the library with your app and run it:

- All Kotlin Flow instances are automatically detected and tracked
- All StateFlow instances are automatically detected and tracked
- All LiveData instances are automatically detected and tracked
- Other reactive streams are automatically detected and tracked

You don't need to add any special imports, annotations, or tracking functions. The visualizer works by scanning your codebase and instrumenting the reactive streams at runtime.

## Example Screens

The app provides several example screens:

1. **Visualizer Screen** - Shows all reactive stream events in real-time
2. **Examples Screen** - Contains various example patterns:
   - Basic Flow operations
   - StateFlow and LiveData examples
   - Flow transformations
   - Flow combinations (zip, combine, merge)
   - Error handling

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Kotlin Coroutines and Flow libraries
- Jetpack Compose for the UI 