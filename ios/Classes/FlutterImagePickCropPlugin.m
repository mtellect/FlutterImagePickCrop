#import "FlutterImagePickCropPlugin.h"
#import <flutter_image_pick_crop/flutter_image_pick_crop-Swift.h>

@implementation FlutterImagePickCropPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterImagePickCropPlugin registerWithRegistrar:registrar];
}
@end
