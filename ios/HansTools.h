#import <UIKit/UIKit.h>
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNHansToolsSpec.h"

@interface HansTools : NSObject <NativeHansToolsSpec,RCTBridgeModule,UINavigationControllerDelegate, UIImagePickerControllerDelegate>
#else
#import <React/RCTBridgeModule.h>

@interface HansTools : NSObject <RCTBridgeModule,UINavigationControllerDelegate, UIImagePickerControllerDelegate>
#endif

@end
