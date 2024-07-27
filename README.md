# presentation_displays_two

A Flutter plugin for managing secondary displays and presentations on Android devices.

## Features

- List available displays
- Show presentations on secondary displays
- Hide presentations
- Send data between the main display and secondary displays
- Receive notifications when displays are connected or disconnected

## Getting Started

To use this plugin, add `presentation_displays_two` as a dependency in your `pubspec.yaml` file.

```yaml
dependencies:
  presentation_displays_two:
    git:
      url: https://github.com/sumonwd/presentation_displays_two.git
```

## Usage

First, import the package in your Dart code:

```dart
import 'package:presentation_displays_two/presentation_displays_two.dart';
```

### Initializing the DisplayManagerTwo

```dart
final DisplayManagerTwo displayManager = DisplayManagerTwo();
```

### Listing Available Displays

```dart
List<DisplayTwo>? displays = await displayManager.getDisplays();
```

You can also specify a category:

```dart
List<DisplayTwo>? presentationDisplays = await displayManager.getDisplays(
  category: DISPLAY_CATEGORY_PRESENTATION
);
```

### Showing a Presentation

```dart
bool success = await displayManager.showPresentation(
  displayId: secondaryDisplay.displayId,
  routerName: '/secondary',
);
```

### Hiding a Presentation

```dart
bool success = await displayManager.hidePresentation(
  displayId: secondaryDisplay.displayId,
);
```

### Sending Data to the Secondary Display

```dart
bool success = await displayManager.sendDataToPresentation('Hello from main display!');
```

### Receiving Data from the Secondary Display

Set up a callback to receive data from the secondary display:

```dart
displayManager.setDataReceivedFromPresentationCallback((data) {
  print('Received data from presentation: $data');
});
```

### Listening for Display Connection Changes

```dart
displayManager.connectedDisplaysChangedStream.listen((event) {
  if (event == 1) {
    print('A display was connected');
  } else if (event == 0) {
    print('A display was disconnected');
  }
});
```

## Secondary Display Implementation

To implement the secondary display, create a `SecondaryScreen` widget:

```dart
class SecondaryScreen extends StatefulWidget {
  @override
  _SecondaryScreenState createState() => _SecondaryScreenState();
}

class _SecondaryScreenState extends State<SecondaryScreen> {
  static const MethodChannel _channel = MethodChannel('main_to_presentation_channel');
  String _receivedData = 'No data received yet';

  @override
  void initState() {
    super.initState();
    _channel.setMethodCallHandler(_handleMethodCall);
  }

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'updateSecondaryDisplay':
        setState(() {
          _receivedData = 'Received: ${call.arguments['data']}';
        });
        return true;
      default:
        throw PlatformException(
          code: 'Unimplemented',
          details: 'The method ${call.method} is not implemented on the Dart side.',
        );
    }
  }

  Future<void> _sendDataToMain() async {
    try {
      await _channel.invokeMethod('sendDataToMain', {'data': 'Hello from secondary screen!'});
      print('Data sent to main successfully');
    } catch (e) {
      print('Failed to send data to main: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    // Implement your secondary display UI here
  }
}
```

Don't forget to set up the route for the secondary display in your `main.dart`:

```dart
void main() {
  runApp(MyApp());
}

@pragma('vm:entry-point')
void secondaryDisplayMain() {
  runApp(MaterialApp(
    home: SecondaryScreen(),
  ));
}
```

## Notes

- This plugin is currently supported on Android only.
- Make sure to handle permissions properly in your Android app for accessing display information.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.