import { useEffect, useState } from 'react';
import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import { addKeyChain, authenticateWithBiometricsAndroid, authenticateWithBiometricsIOS, copyTextToClipboard, deleteKeyChain, getBioType, getKeyChain, getLang, getSafeAreaInsets, getVersion, impact, readTextFromClipboard, selectAndDecodeQRImage, setStatusBarAndNavigationBar } from 'react-native-hans-tools';
import type { SafeAreaInsets } from '../../src/NativeHansTools';
// import { addKeyChain, authenticateWithBiometricsAndroid, copyTextToClipboard, getBioType, getKeyChain, getLang, getVersion, impact, readTextFromClipboard, selectAndDecodeQRImage, setStatusBarAndNavigationBar } from 'react-native-hans-tools';

export default function App() {
  const [version, setVersion] = useState('');
  const [bioType, setBioType] = useState('');
  const [lang, setLang] = useState('');
  const [clipboardText, setClipboardText] = useState('');
  const [qrText, setQrText] = useState('');
  const [keyChain, setKeyChain] = useState('');
  const [safeAreaInsets, setSafeAreaInsets] = useState<SafeAreaInsets>({
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
  });
  useEffect(() => {
    setVersion(getVersion());
    setBioType(getBioType());
    setLang(getLang());
    setClipboardText(readTextFromClipboard());
    getSafeAreaInsets().then((safeAreaInsets) => {
      setSafeAreaInsets(safeAreaInsets);
    });
    setStatusBarAndNavigationBar({
      statusBarTheme: 'dark',
      statusBarColor: '#ffffff',
      navigationBarColor: '#ffffff',
      navigationBarStyle: 'dark',
      hideStatusBar: false,
    });
  }, []);
  return (
    <View style={styles.container}>
      <Text>version: {version}</Text>
      <Text>bioType: {bioType}</Text>
      <Text>lang: {lang}</Text>
      <Text>clipboardText: {clipboardText}</Text>
      <Text>safeAreaInsets: {JSON.stringify(safeAreaInsets)}</Text>
      <TouchableOpacity style={styles.button} onPress={() => copyTextToClipboard('Hello, world!')}>
        <Text>Copy</Text>
      </TouchableOpacity>
      <View style={{
        flexDirection: 'row',
      }}>
        <TouchableOpacity style={styles.button} onPress={() => impact('light')}>
          <Text>Light</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => impact('medium')}>
          <Text>Medium</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => impact('heavy')}>
          <Text>Heavy</Text>
        </TouchableOpacity>
      </View>
      <View style={{
        flexDirection: 'row',
      }}>
        <TouchableOpacity style={styles.button} onPress={() => impact('success')}>
          <Text>Success</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => impact('error')}>
          <Text>Error</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => impact('warning')}>
          <Text>Warning</Text>
        </TouchableOpacity>
      </View>
      <View style={{
        flexDirection: 'row',
      }}>
        <TouchableOpacity style={styles.button} onPress={() => addKeyChain('123456', {
          accountName: '123456',
          serviceName: '123456',
        })}>
          <Text>Add</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => getKeyChain({
          accountName: '123456',
          serviceName: '123456',
        }).then((keyChain) => {
          setKeyChain(keyChain);
        }).catch((error) => {
          console.error(error);
        })}>
          <Text>Get: {keyChain}</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => deleteKeyChain({
          accountName: '123456',
          serviceName: '123456',
        })}>
          <Text>Delete</Text>
        </TouchableOpacity>
      </View>
      <TouchableOpacity style={styles.button} onPress={() => selectAndDecodeQRImage().then((qrText) => {
        setQrText(qrText);
      }).catch((error) => {
        console.error(error);
      })}>
        <Text>Select and Decode QR Image</Text>
      </TouchableOpacity>
      <Text>QR Text: {qrText}</Text>
      <TouchableOpacity style={styles.button} onPress={() => authenticateWithBiometricsIOS('请验证指纹')}>
        <Text>Authenticate with Biometrics IOS</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.button} onPress={() => authenticateWithBiometricsAndroid({
        title: '请验证指纹',
        subtitle: '请验证指纹',
        negativeButtonText: '取消',
        requireConfirmation: true,
      }).then((result) => {
        console.log(result);
      }).catch((error) => {
        console.error(error);
      })}>
        <Text>Authenticate with Biometrics Android</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    backgroundColor: '#00ff00',
    marginVertical: 20,
  },
  button: {
    backgroundColor: '#00ff00',
    padding: 10,
    marginVertical: 20,
    marginHorizontal: 10,
  },
});
