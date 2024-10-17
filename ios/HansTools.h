
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNHansToolsSpec.h"

@interface HansTools : NSObject <NativeHansToolsSpec>
#else
#import <React/RCTBridgeModule.h>

@interface HansTools : NSObject <RCTBridgeModule>
#endif

@end
