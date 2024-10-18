package com.hanstools;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeyChain {

  // 对照 iso 的 keychain
  // accountName 在这里作为 keyStore 的 alias
  // serviceName 在这里作为 存储文件的文件名字
  public boolean createKey(String alias) {
    try {
      KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);

      // 检查密钥是否已经存在
      if (!keyStore.containsAlias(alias)) {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        keyGenerator.init(new KeyGenParameterSpec.Builder(
          alias,
          KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
          .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
          .setUserAuthenticationRequired(false)
          .build());

        keyGenerator.generateKey();
        return true; // 新密钥创建成功
      } else {
        return true;
      }
    } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | KeyStoreException | CertificateException | IOException e) {
      e.printStackTrace();
      return false; // 发生错误，返回false
    }
  }
  public String encryptText(String alias, String textToEncrypt) {
    try {
      KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);
      SecretKey key = (SecretKey) keyStore.getKey(alias, null);

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
      cipher.init(Cipher.ENCRYPT_MODE, key);
      byte[] iv = cipher.getIV();
      byte[] encryption = cipher.doFinal(textToEncrypt.getBytes("UTF-8"));

      String ivString = Base64.encodeToString(iv, Base64.DEFAULT);
      String encryptedString = Base64.encodeToString(encryption, Base64.DEFAULT);
      return ivString + ":" + encryptedString;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  public String decryptText(String alias, String encryptedData) {
    try {
      KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);
      SecretKey key = (SecretKey) keyStore.getKey(alias, null);

      String[] parts = encryptedData.split(":");
      byte[] iv = Base64.decode(parts[0], Base64.DEFAULT);
      byte[] cipherText = Base64.decode(parts[1], Base64.DEFAULT);

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
      cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
      byte[] decrypted = cipher.doFinal(cipherText);

      return new String(decrypted, StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  public boolean saveEncryptedData(Context context, String encryptedData,String fileName) {
    try {
      FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
      fos.write(encryptedData.getBytes());
      fos.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  public String getEncryptedData(Context context, String fileName) {
    try {
      StringBuilder stringBuilder = new StringBuilder();
      FileInputStream fis = context.openFileInput(fileName);
      InputStreamReader inputStreamReader = new InputStreamReader(fis);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line);
      }

      fis.close();
      return stringBuilder.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;

  }

  public boolean saveContent(Context context,String content, String accountName,String serviceName){
    if(createKey(accountName)){
      String p = encryptText(accountName,content);
      if(p == null) return false;
      return saveEncryptedData(context,p,serviceName);
    }
    return  false;
  }
  public String getContent(Context context,String accountName,String serviceName){
    if(createKey(accountName)){
      String p = getEncryptedData(context,serviceName);
      if(p == null) return null;
      return decryptText(accountName,p);
    }
    return null;
  }

  public boolean deleteContent(Context context,String accountName,String serviceName){
    try {
      context.deleteFile(serviceName);
      // keyStore 删除
      KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);
      keyStore.deleteEntry(accountName);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
