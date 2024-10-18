import { Platform } from "react-native";
import type { BiometricsProps, BioType, ImpactStyle, KeyChainOptions, SafeAreaInsets, StatusBarAndNavigationBarOptions } from "./NativeHansTools";
const HansTools = require('./NativeHansTools').default;

export function getVersion(): string {
  return HansTools.getVersion();
}
export function getBioType(): BioType {
  return HansTools.getBioType();
}
export function getLang(): string {
  return HansTools.getLang();
}

export function copyTextToClipboard(text: string): void {
  return HansTools.copyTextToClipboard(text);
}

export function readTextFromClipboard(): string {
  return HansTools.readTextFromClipboard();
}

export function setStatusBarAndNavigationBar(options: StatusBarAndNavigationBarOptions): void {
  // 只有安卓执行
  if (Platform.OS === 'android') {
    HansTools.setStatusBarAndNavigationBar(options);
  }
}

export function impact(style: ImpactStyle): void {
  return HansTools.impact(style);
}
export function getSafeAreaInsets(): Promise<SafeAreaInsets> {
  if (Platform.OS === 'ios') {
    return HansTools.getSafeAreaInsets();
  }
  return Promise.resolve({
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
  });
}
export function selectAndDecodeQRImage(): Promise<string> {
  return HansTools.selectAndDecodeQRImage();
}

export function addKeyChain(password: string, options: KeyChainOptions): Promise<boolean> {
  return HansTools.addKeyChain(password, options);
}

export function getKeyChain(options: KeyChainOptions): Promise<string> {
  return HansTools.getKeyChain(options);
}

export function deleteKeyChain(options: KeyChainOptions): Promise<boolean> {
  return HansTools.deleteKeyChain(options);
}

export function authenticateWithBiometricsAndroid(props: BiometricsProps): Promise<boolean> {
  if (Platform.OS === 'android') {
    return HansTools.authenticateWithBiometrics(props);
  }
  return Promise.resolve(false);
}
export function authenticateWithBiometricsIOS(reason: string): Promise<boolean> {
  if (Platform.OS === 'ios') {
    return HansTools.authenticateWithBiometricsIOS(reason);
  }
  return Promise.resolve(false);
}