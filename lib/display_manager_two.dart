import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';

class DisplayTwo {
  final int? displayId;
  final int? flags;
  final int? rotation;
  final String? name;

  DisplayTwo({
    this.displayId,
    this.flags,
    this.rotation,
    this.name,
  });

  factory DisplayTwo.fromJson(Map<String, dynamic> json) {
    return DisplayTwo(
      displayId: json['displayId'],
      flags: json['flags'],
      rotation: json['rotation'],
      name: json['name'],
    );
  }
}

class DisplayManagerTwo {
  static const MethodChannel _channel = MethodChannel('presentation_displays_two_plugin');
  static const MethodChannel _mainToPresentationChannel =
      MethodChannel('main_to_presentation_channel');
  static const MethodChannel _presentationToMainChannel =
      MethodChannel('presentation_to_main_channel');

  Future<List<DisplayTwo>?> getDisplays({String? category}) async {
    try {
      final String result = await _channel.invokeMethod('listDisplay', {'category': category});
      List<dynamic> origins = jsonDecode(result);
      List<DisplayTwo> displays = [];
      for (var element in origins) {
        displays.add(DisplayTwo.fromJson(element));
      }
      return displays;
    } catch (e) {
      print('Error getting displays: $e');
      return null;
    }
  }

  Future<bool> showPresentation({required int displayId, required String routerName}) async {
    try {
      final bool result = await _channel.invokeMethod('showPresentation', {
        'displayId': displayId,
        'routerName': routerName,
      });
      return result;
    } catch (e) {
      print('Error showing presentation: $e');
      return false;
    }
  }

  Future<bool> hidePresentation({required int displayId}) async {
    try {
      final bool result = await _channel.invokeMethod('hidePresentation', {'displayId': displayId});
      return result;
    } catch (e) {
      print('Error hiding presentation: $e');
      return false;
    }
  }

  Future<bool> sendDataToPresentation(dynamic data) async {
    try {
      final bool result =
          await _mainToPresentationChannel.invokeMethod('receivedDataFromMain', {'data': data});
      return result;
    } catch (e) {
      print('Error sending data to presentation: $e');
      return false;
    }
  }

  void setDataReceivedFromPresentationCallback(Function(dynamic) callback) {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'receivedDataFromPresentation') {
        final data = call.arguments['data'];
        callback(data);
      }
    });
  }

  Stream<int?> get connectedDisplaysChangedStream {
    return const EventChannel('presentation_displays_two_events')
        .receiveBroadcastStream()
        .cast<int?>();
  }
}
