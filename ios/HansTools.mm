#import "HansTools.h"
#import <LocalAuthentication/LocalAuthentication.h>
#import <UIKit/UIKit.h>
@interface HansTools()
@property (nonatomic,strong)RCTPromiseResolveBlock selectQRImageResolve;
@property (nonatomic,strong)RCTPromiseRejectBlock selectQRImageReject;
@end
@implementation HansTools
RCT_EXPORT_MODULE()

#ifdef RCT_NEW_ARCH_ENABLED
// getVersion
- (NSString *)getVersion {
    NSDictionary *infoDict = [[NSBundle mainBundle] infoDictionary];
    NSString *appVersion = [infoDict objectForKey:@"CFBundleShortVersionString"]; // example: 1.0.0
    NSString *buildNumber = [infoDict objectForKey:@"CFBundleVersion"];

    return [NSString stringWithFormat:@"%@(%@)",appVersion,buildNumber];
}
// getBioType
- (NSString *)getBioType {
    LAContext *context = [[LAContext alloc] init];
    NSError *error = nil;
    if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
        switch (context.biometryType) {
            case LABiometryTypeFaceID:
                return @"face";
            case LABiometryTypeTouchID:
                return @"fingerprint";
            default:
                return @"none";
        }
    }

    return @"none";
}
// getLang
- (NSString *)getLang {
    return [[NSLocale currentLocale] objectForKey:NSLocaleLanguageCode];
}
// copyTextToClipboard
- (void)copyTextToClipboard:(NSString *)text {
    UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
    pasteboard.string = text;
}
// readTextFromClipboard
- (NSString *)readTextFromClipboard {
    UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
    return pasteboard.string;
}
// impact
- (void)impact:(NSString *)style {
    if([style isEqualToString:@"light"]){
        UIImpactFeedbackGenerator *generator = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleLight];
        [generator prepare];
        [generator impactOccurred];
    }
    else if([style isEqualToString:@"medium"]){
        UIImpactFeedbackGenerator *generator = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleMedium];
        [generator prepare];
        [generator impactOccurred];
    }
    else if([style isEqualToString:@"heavy"]){
        UIImpactFeedbackGenerator *generator = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleHeavy];
        [generator prepare];
        [generator impactOccurred];
    }
    else if([style isEqualToString:@"success"]){
        UINotificationFeedbackGenerator *generator = [[UINotificationFeedbackGenerator alloc] init];
        [generator prepare];
        [generator notificationOccurred:UINotificationFeedbackTypeSuccess];
    }
    else if([style isEqualToString:@"error"]){
        UINotificationFeedbackGenerator *generator = [[UINotificationFeedbackGenerator alloc] init];
        [generator prepare];
        [generator notificationOccurred:UINotificationFeedbackTypeError];
    }
    else if([style isEqualToString:@"warning"]){
        UINotificationFeedbackGenerator *generator = [[UINotificationFeedbackGenerator alloc] init];
        [generator prepare];
        [generator notificationOccurred:UINotificationFeedbackTypeWarning];
    }
}
- (void)addKeyChain:(NSString *)password options:(JS::NativeHansTools::KeyChainOptions &)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  NSString *accountName = options.accountName();
  NSString *serviceName = options.serviceName();
  NSDictionary *keychainItem = @{
          (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
          (__bridge id)kSecAttrAccount: accountName,
          (__bridge id)kSecAttrService: serviceName,
          (__bridge id)kSecValueData: [password dataUsingEncoding:NSUTF8StringEncoding]
      };

      SecItemDelete((__bridge CFDictionaryRef)keychainItem);
      OSStatus status = SecItemAdd((__bridge CFDictionaryRef)keychainItem, NULL);

      if (status == errSecSuccess) {
          resolve(@YES);
      } else {
          NSError *error = [NSError errorWithDomain:NSOSStatusErrorDomain code:status userInfo:nil];
          reject(@"save_error", @"Failed to save the password", error);
      }
}


- (void)deleteKeyChain:(JS::NativeHansTools::KeyChainOptions &)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  // 定义 Keychain 查询字典
  NSString *accountName = options.accountName();
  NSString *serviceName = options.serviceName();
  NSDictionary *keychainQuery = @{
      (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
      (__bridge id)kSecAttrAccount: accountName,
      (__bridge id)kSecAttrService: serviceName
  };

  // 尝试删除 Keychain 项
  OSStatus status = SecItemDelete((__bridge CFDictionaryRef)keychainQuery);

  // 处理操作结果
  if (status == errSecSuccess) {
      resolve(@YES);  // 删除成功
  } else {
      NSError *error = [NSError errorWithDomain:NSOSStatusErrorDomain code:status userInfo:nil];
      reject(@"delete_error", @"Failed to delete the password", error);
  }
}


- (void)getKeyChain:(JS::NativeHansTools::KeyChainOptions &)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  NSString *accountName = options.accountName();
  NSString *serviceName = options.serviceName();
  NSDictionary *query = @{
          (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
          (__bridge id)kSecAttrService: serviceName,
          (__bridge id)kSecAttrAccount: accountName,
          (__bridge id)kSecReturnData: @YES,
          (__bridge id)kSecMatchLimit: (__bridge id)kSecMatchLimitOne
      };

      CFTypeRef dataTypeRef = NULL;
      OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &dataTypeRef);
      if (status == errSecSuccess) {
          NSData *resultData = (__bridge_transfer NSData *)dataTypeRef;
          NSString *password = [[NSString alloc] initWithData:resultData encoding:NSUTF8StringEncoding];
          resolve(password);
      } else {
          NSError *error = [NSError errorWithDomain:NSOSStatusErrorDomain code:status userInfo:nil];
          reject(@"no_password", @"Failed to retrieve the password from Keychain", error);
      }
}

// only for ios
//getSafeAreaInsets
- (void)getSafeAreaInsets:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  // 检查系统版本是否支持 safeAreaInsets
  if (@available(iOS 11.0, *)) {
    dispatch_async(dispatch_get_main_queue(), ^{
      CGFloat top =  [[UIApplication sharedApplication] delegate].window.safeAreaInsets.top;
      CGFloat bottom =  [[UIApplication sharedApplication] delegate].window.safeAreaInsets.bottom;
      CGFloat left =  [[UIApplication sharedApplication] delegate].window.safeAreaInsets.left;
      CGFloat right =  [[UIApplication sharedApplication] delegate].window.safeAreaInsets.right;
      resolve(@{
        @"top": @(top),
        @"bottom": @(bottom),
        @"left": @(left),
        @"right": @(right)
      });
    });
  }
  else{
    resolve(@{
        @"top": @(0),
        @"left": @(0),
        @"bottom": @(0),
        @"right": @(0)
    });
  }
}
// only support android,no code for ios
- (void)setStatusBarAndNavigationBar:(JS::NativeHansTools::SpecSetStatusBarAndNavigationBarOptions &)options {
    //
}
- (void)authenticateWithBiometrics:(JS::NativeHansTools::BiometricsProps &)props resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject{
  
}

- (void)selectAndDecodeQRImage:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  self.selectQRImageResolve = resolve;
  self.selectQRImageReject = reject;
  dispatch_async(dispatch_get_main_queue(), ^{
    // 在主线程中执行选择图片的逻辑
    UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
    imagePicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    imagePicker.delegate = self;
    UIWindow *keyWindow = nil;

    if (@available(iOS 13.0, *)) {
      // 遍历所有连接的场景，找到当前激活的 UIWindowScene
      for (UIWindowScene *windowScene in [UIApplication sharedApplication].connectedScenes) {
        if (windowScene.activationState == UISceneActivationStateForegroundActive) {
            keyWindow = windowScene.windows.firstObject;
            break;
        }
      }
    } else {
      // iOS 13 以下版本，仍然使用 keyWindow
      keyWindow = [UIApplication sharedApplication].keyWindow;
    }
    if(keyWindow.rootViewController){
      [keyWindow.rootViewController presentViewController:imagePicker animated:YES completion:nil];
    }
  });
}

- (void)authenticateWithBiometricsIOS:(NSString *)reason resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject { 
  LAContext *context = [[LAContext alloc] init];
  NSError *error = nil;

  // 检查是否支持Face ID
  if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
      [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics
              localizedReason:reason // 使用从JS传入的localizedReason
                        reply:^(BOOL success, NSError *error) {
                            if (success) {
                              resolve(@YES);
                            } else {
                              resolve(@NO);
                            }
                        }];
  } else {
      if (error) {
          reject(@"BIOMETRICS_NOT_AVAILABLE", @"Biometrics not available or not enrolled", error);
      } else {
          reject(@"BIOMETRICS_NOT_AVAILABLE", @"Biometrics not available or not enrolled", nil);
      }
  }
}



// UIImagePickerControllerDelegate 方法
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<UIImagePickerControllerInfoKey,id> *)info {
  [picker dismissViewControllerAnimated:YES completion:nil];
  UIImage *selectedImage = info[UIImagePickerControllerOriginalImage];
  
  // 解析二维码
  CIDetector *detector = [CIDetector detectorOfType:CIDetectorTypeQRCode
                                            context:nil
                                            options:@{CIDetectorAccuracy: CIDetectorAccuracyHigh}];
  CIImage *ciImage = [[CIImage alloc] initWithImage:selectedImage];
  NSArray *features = [detector featuresInImage:ciImage];
  
  if (features.count > 0) {
    // 假设我们只关心图片中的第一个二维码
    CIQRCodeFeature *feature = [features firstObject];
    NSString *qrCodeString = feature.messageString;
    self.selectQRImageResolve(qrCodeString);
  } else {
    NSError *error = [NSError errorWithDomain:@"QRCodeScanner" code:200 userInfo:@{NSLocalizedDescriptionKey: @"No QR code found"}];
    self.selectQRImageReject(@"no_qr_codes", @"No QR codes were found in the image.", error);
  }
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeHansToolsSpecJSI>(params);
}
#endif

@end
