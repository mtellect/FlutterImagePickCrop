import 'dart:async';

import 'package:flutter/services.dart';

class FlutterImagePickCrop {
  static const MethodChannel _channel =
      const MethodChannel('flutter_image_pick_crop');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> pickAndCropImage(String option) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("Options", () => option);
    final String filePath =
        await _channel.invokeMethod('pickAndCropImage', args);
    return filePath;
    //return filePath == null ? null : new File(filePath);
  }
}
