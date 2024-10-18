import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export type BioType = 'none' | 'face' | 'fingerprint' | 'biometrics';
export type ImpactStyle = 'light' | 'medium' | 'heavy' | 'success' | 'error' | 'warning';
export type StatusAndNavigationBarTheme = 'light' | 'dark';
export interface StatusBarAndNavigationBarOptions {
  statusBarTheme: StatusAndNavigationBarTheme,
  statusBarColor: string,
  navigationBarColor: string,
  navigationBarStyle: StatusAndNavigationBarTheme,
  hideStatusBar: boolean,
}
export interface KeyChainOptions {
  accountName: string,
  serviceName: string,
}
export interface BiometricsProps {
  title?: string
  subtitle?: string
  negativeButtonText?: string
  requireConfirmation?: boolean
}
export interface SafeAreaInsets {
  top: number,
  left: number,
  bottom: number,
  right: number,
}
// Spec 里面的传入参数不能存在  type StatusAndNavigationBarTheme = 'light' | 'dark'; 这种类型
// 如何工作： 先在 Spec 添加函数， ios 执行 pod install ，会生成对应的 objc 结构体，然后
// 在 HansTools.mm 文件中实现对应的函数， 
export interface Spec extends TurboModule {
  getVersion(): string;
  getBioType(): BioType;
  getLang(): string;
  copyTextToClipboard(text: string): void;
  readTextFromClipboard(): string;
  impact(style: string): void;
  selectAndDecodeQRImage(): Promise<string>;
  addKeyChain: (password: string, options: KeyChainOptions) => Promise<boolean>
  getKeyChain: (options: KeyChainOptions) => Promise<string>
  deleteKeyChain: (options: KeyChainOptions) => Promise<boolean>
  // only android
  setStatusBarAndNavigationBar(options: {
    statusBarTheme: string,
    statusBarColor: string,
    navigationBarColor: string,
    navigationBarStyle: string,
    hideStatusBar: boolean,
  }): void;
  authenticateWithBiometrics: (props: BiometricsProps) => Promise<boolean>
  // only ios
  getSafeAreaInsets: () => Promise<{ top: number, left: number, bottom: number, right: number }>
  authenticateWithBiometricsIOS: (reason: string) => Promise<boolean>
}

export default TurboModuleRegistry.getEnforcing<Spec>('HansTools');
