package com.hanstools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import com.facebook.react.module.annotations.ReactModule
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


@ReactModule(name = HansToolsModule.NAME)
class HansToolsModule(reactContext: ReactApplicationContext) : NativeHansToolsSpec(reactContext), ActivityEventListener {

  override fun getName(): String {
    return NAME
  }
  //getVersion
  override fun getVersion(): String {
    val context: Context = reactApplicationContext
    val packageName = context.packageName
    val versionName = context.packageManager.getPackageInfo(packageName, 0).versionName
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      // 使用 getLongVersionCode()，适用于 API 28 及以上
      context.packageManager.getPackageInfo(packageName, 0).longVersionCode
    } else {
      // 使用 versionCode，适用于 API 28 以下
      context.packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
    }
    return "$versionName ($versionCode)"
  }
  //getBioType
  override fun getBioType(): String {
    val biometricManager: BiometricManager = BiometricManager.from(reactApplicationContext)
    return if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
      "biometrics"
    } else {
      "none" // Not supported
    }
  }
  // getLang
  override fun getLang(): String {
    // 兼容 API 24 以下的版本
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      reactApplicationContext.resources.configuration.locales[0].language
    } else {
      reactApplicationContext.resources.configuration.locale.language
    }
  }
  // copyTextToClipboard
  override fun copyTextToClipboard(text: String) {
    val clipboard = reactApplicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
  }
  // readTextFromClipboard
  override fun readTextFromClipboard(): String {
    val clipboard = reactApplicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.text.toString()
  }
  // setStatusBarAndNavigationBar
  override fun setStatusBarAndNavigationBar(options: ReadableMap) {
    val activity = reactApplicationContext.currentActivity
    if (activity != null) {
      val window = activity.window
      val statusBarTheme = options.getString("statusBarTheme");
      val statusBarColor = options.getString("statusBarColor");
      val navigationBarColor = options.getString("navigationBarColor");
      val navigationBarStyle = options.getString("navigationBarStyle");
      val hideStatusBar = options.getBoolean("hideStatusBar");
      runOnUiThread {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_FULLSCREEN)
        var flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (statusBarTheme.equals("dark")) flag = flag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          if (navigationBarStyle.equals("dark")) flag =
            flag or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        window.decorView.systemUiVisibility = flag
        var windowFlag = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        if (hideStatusBar) {
          windowFlag = windowFlag or WindowManager.LayoutParams.FLAG_FULLSCREEN
        }
        window.addFlags(windowFlag)
        window.statusBarColor =
          if (statusBarColor == null) Color.TRANSPARENT else Color.parseColor(statusBarColor)
        window.navigationBarColor =
          if (navigationBarColor == null) Color.TRANSPARENT else Color.parseColor(navigationBarColor)
      }
    }
  }
  // impact
  @RequiresApi(Build.VERSION_CODES.Q)
  override fun impact(type:String){
    val context = reactApplicationContext
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
    var effect: VibrationEffect
    when (type) {
      "light" -> {
        effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        } else {
          VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
        }
      }
      "medium" -> {
        effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
        } else {
          VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
        }
      }
      "heavy" -> {
        effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
        } else {
          VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
        }
      }
      "success" -> {
        effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
      }
      "error" -> {
        effect = VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE);
      }
      "warning" -> {
        val timings = longArrayOf(0, 100, 50, 100) // pause, vibrate, pause, vibrate
        val amplitudes =
          intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
        effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
      }
      else -> {
        effect = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
      }
    }
    vibrator.vibrate(effect)
  }
  // selectAndDecodeQRImage
  override fun selectAndDecodeQRImage(promise: Promise) {
    val activity = reactApplicationContext.currentActivity
    this.promise = promise;
    if (activity != null) {
      val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT)
      intent.type = "image/*"
      intent.addCategory(android.content.Intent.CATEGORY_OPENABLE)
      activity.startActivityForResult(intent, 1)

    }
  }
  // addKeyChain
  override fun addKeyChain(password:String, options: ReadableMap,promise: Promise) {
    try {
      val keyChain = KeyChain()
      val accountName = options.getString("accountName")
      val serviceName = options.getString("serviceName")
      val flag = keyChain.saveContent(reactApplicationContext, password, accountName, serviceName)
      promise.resolve(flag)
    }
    catch (e: Exception) {
      return promise.reject("E_FAILED_TO_SAVE_KEYCHAIN", e)
    }
  }
  // getKeyChain
  override fun getKeyChain(options: ReadableMap, promise: Promise) {
    try {
      val keyChain = KeyChain()
      val accountName = options.getString("accountName")
      val serviceName = options.getString("serviceName")
      val password = keyChain.getContent(reactApplicationContext, accountName, serviceName)
      promise.resolve(password)
    }
    catch (e: Exception) {
      return promise.reject("E_FAILED_TO_GET_KEYCHAIN", e)
    }
  }
  // deleteKeyChain
  override fun deleteKeyChain(options: ReadableMap, promise: Promise) {
    try {
      val keyChain = KeyChain()
      val accountName = options.getString("accountName")
      val serviceName = options.getString("serviceName")
      val flag = keyChain.deleteContent(reactApplicationContext, accountName, serviceName)
      promise.resolve(flag)
    }
    catch (e: Exception) {
      return promise.reject("E_FAILED_TO_DELETE_KEYCHAIN", e)
    }
  }
  // authenticateWithBiometrics
  override fun authenticateWithBiometrics(options: ReadableMap, promise: Promise) {
    val title: String = if (options.hasKey("title")) options.getString("title") ?: "Authenticate" else "Authenticate"
    val subtitle = if (options.hasKey("subtitle")) options.getString("subtitle") ?: "" else ""
    val negativeButtonText = if (options.hasKey("negativeButtonText")) options.getString("negativeButtonText") ?: "Cancel" else "Cancel"
    val requireConfirmation = options.hasKey("requireConfirmation") && options.getBoolean("requireConfirmation")
    runOnUiThread{
      val executor = ContextCompat.getMainExecutor(reactApplicationContext)
      val activity = currentActivity as FragmentActivity?
      if (activity == null) {
        promise.reject("ACTIVITY_NULL", "Activity is null")
      }
      else{
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle(title)
          .setSubtitle(subtitle)
          .setNegativeButtonText(negativeButtonText)
          .setConfirmationRequired(requireConfirmation)
          .build()

        val biometricPrompt: BiometricPrompt =  BiometricPrompt(activity, executor, object : AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            promise.reject("E_AUTHENTICATION_ERROR", errString.toString())
          }

          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            promise.resolve(true)
          }

          override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            promise.reject("E_AUTHENTICATION_FAILED", "Authentication failed")
          }
        })
        biometricPrompt.authenticate(promptInfo)
      }
    }

  }
  // getSafeAreaInsets
  override fun getSafeAreaInsets(promise: Promise) {

  }
  //authenticateWithBiometricsIOS
  override fun authenticateWithBiometricsIOS(reason:String,promise: Promise) {
  }

  var promise: Promise? = null
  init {
    reactContext.addActivityEventListener(this)
  }
  override fun onNewIntent(intent: Intent?) {
    // 这里没有新意图需要处理，但必须实现这个方法
  }
  // onActivityResult
  override fun onActivityResult(p0: Activity?, p1: Int, p2: Int, p3: Intent?) {
    // 解析返回的图片
    if (p1 == 1 && p2 == Activity.RESULT_OK) {
      val uri = p3?.data
      val image: InputImage
      if (uri != null) {
        image = InputImage.fromFilePath(reactApplicationContext, uri);
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        val scanner = BarcodeScanning.getClient(options)
        scanner.process(image).addOnSuccessListener { barcodes ->
          if (barcodes.size > 0) {
            val barcode = barcodes[0]
            promise!!.resolve(barcode.displayValue)
          } else {
            promise!!.reject("E_FAILED_TO_PICK_IMAGE", "No QR code found")
          }
        }.addOnFailureListener { e ->
          promise!!.reject("E_FAILED_TO_PICK_IMAGE", e)
        }
      }
      else{
        promise?.reject("E_FAILED_TO_PICK_IMAGE", "No image selected")
      }
    } else {
      promise?.reject("E_FAILED_TO_PICK_IMAGE", "No image selected")
    }
  }
  companion object {
    const val NAME = "HansTools"
  }
}
