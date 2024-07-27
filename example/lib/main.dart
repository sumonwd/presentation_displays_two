import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:presentation_displays_two/display_manager_two.dart';

void main() {
  runApp(const MyApp());
}

@pragma('vm:entry-point')
void secondaryDisplayMain() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(MaterialApp(
    theme: ThemeData(primarySwatch: Colors.blue),
    initialRoute: '/secondary',
    routes: {
      '/secondary': (context) => const SecondaryScreen(),
    },
  ));
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Presentation Displays Demo',
      theme: ThemeData(primarySwatch: Colors.blue),
      initialRoute: '/',
      routes: {
        '/': (context) => const MainScreen(),
        '/secondary': (context) => const SecondaryScreen(),
      },
    );
  }
}

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  final DisplayManagerTwo _displayManager = DisplayManagerTwo();
  List<DisplayTwo> _displays = [];
  DisplayTwo? _selectedDisplay;
  String _receivedData = 'No data received yet';

  @override
  void initState() {
    super.initState();
    _loadDisplays();
    _displayManager.setDataReceivedFromPresentationCallback(_handleDataFromPresentation);
  }

  Future<void> _loadDisplays() async {
    final displays = await _displayManager.getDisplays();
    setState(() {
      _displays = displays ?? [];
      _selectedDisplay = _displays.isNotEmpty ? _displays[0] : null;
    });
  }

  void _handleDataFromPresentation(dynamic data) {
    print('Received data from presentation: $data');
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Received from presentation: $data')),
    );
  }

  Future<void> _showPresentation() async {
    if (_selectedDisplay != null) {
      final result = await _displayManager.showPresentation(
        displayId: _selectedDisplay!.displayId!,
        routerName: '/secondary',
      );
      if (result) {
        print('Presentation shown successfully');
      } else {
        print('Failed to show presentation');
      }
    }
  }

  Future<void> _hidePresentation() async {
    if (_selectedDisplay != null) {
      final result = await _displayManager.hidePresentation(
        displayId: _selectedDisplay!.displayId!,
      );
      if (result) {
        print('Presentation hidden successfully');
      } else {
        print('Failed to hide presentation');
      }
    }
  }

  Future<void> _sendDataToPresentation() async {
    final result = await _displayManager.sendDataToPresentation(jsonEncode({
      'date': DateTime.now().toString(),
      'message': 'sdfsdf',
      'isTip': false,
    }));
    if (result) {
      print('Data sent successfully');
    } else {
      print('Failed to send data');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Presentation Displays Demo')),
      body: Column(
        children: [
          DropdownButton<DisplayTwo>(
            value: _selectedDisplay,
            items: _displays.map((display) {
              return DropdownMenuItem<DisplayTwo>(
                value: display,
                child: Text('Display ${display.displayId}: ${display.name}'),
              );
            }).toList(),
            onChanged: (DisplayTwo? newValue) {
              setState(() {
                _selectedDisplay = newValue;
              });
            },
          ),
          ElevatedButton(
            onPressed: _showPresentation,
            child: const Text('Show Presentation'),
          ),
          ElevatedButton(
            onPressed: _hidePresentation,
            child: const Text('Hide Presentation'),
          ),
          ElevatedButton(
            onPressed: _sendDataToPresentation,
            child: const Text('Send Data to Presentation'),
          ),
        ],
      ),
    );
  }
}

class SecondaryScreen extends StatefulWidget {
  const SecondaryScreen({super.key});

  @override
  State<SecondaryScreen> createState() => _SecondaryScreenState();
}

class _SecondaryScreenState extends State<SecondaryScreen> {
  static const MethodChannel _channel = MethodChannel('main_to_presentation_channel');
  String _receivedData = 'No data received yet';
  bool _isTip = false;
  final GlobalKey _alertKey = GlobalKey();

  @override
  void initState() {
    super.initState();
    _channel.setMethodCallHandler(_handleMethodCall);
  }

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    if (call.method != 'updateSecondaryDisplay') {
      throw PlatformException(
        code: 'Unimplemented',
        details: 'The method ${call.method} is not implemented on the Dart side.',
      );
    }

    final data = jsonDecode(call.arguments['data'] as String);
    final bool isTip = data['isTip'] as bool? ?? false;

    setState(() {
      _receivedData = 'Received: ${call.arguments['data']}';
      _isTip = isTip;
    });

    if (isTip) {
      _showTipDialog();
    } else if (_alertKey.currentContext != null) {
      Navigator.of(context).pop();
    }

    return true;
  }

  Future<void> _sendDataToMain() async {
    try {
      await _channel.invokeMethod('sendDataToMain', {'data': 'Hello from secondary screen!'});
      print('Data sent to main successfully');
    } catch (e) {
      print('Failed to send data to main: $e');
    }
  }

  Future<void> _showTipDialog() async {
    return showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext context) {
        return AlertDialog(
          key: _alertKey,
          title: const Text('Dialog Title'),
          content: const SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text('This is the dialog content.'),
                Text('You can customize this as needed.'),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Close'),
              onPressed: () {
                Navigator.of(context).pop();
                setState(() => _isTip = false);
              },
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.amber,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Secondary Display', style: Theme.of(context).textTheme.displaySmall),
            const SizedBox(height: 20),
            Text(_receivedData),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _sendDataToMain,
              child: const Text('Send Data to Main'),
            ),
          ],
        ),
      ),
    );
  }
}
