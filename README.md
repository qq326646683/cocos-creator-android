```
allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
}
```
```
dependencies {
       implementation 'com.github.qq326646683:cocos-creator-android:2.0.1'
}
```

### 一、如何集成
1. 文件读写、网络权限
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />
```
2. 下载游戏zip并解压
3. 继承CocosActivity，并将解压后的路径赋值给filePath
```kotlin
class CocosGameActivity: CocosActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun filePath() = intent.getStringExtra("path")
}
```
4. 清单文件
```
 <application>
        <meta-data
            android:name="android.app.lib_name"
            android:value="cocos" />
        <activity android:name=".CocosGameActivity" android:process=":cocos"/>
```

5. 本篇的module和事例app代码放在[gitlab](https://github.com/qq326646683/cocos-creator-android)

### 二、如何通信
1.实现弹出Android的Dialog,选择后把结果传给cocos显示(不需要主进程的数据，可以直接1->4)
android/CocosGameActivity.kt
```kotlin
private val showArray = arrayOf("刘德华", "周华健")
private val cocosListenerInCocos: CocosDataListener = CocosDataListener { action, argument, callbackId ->
  CocosBridgeHelper.log("接收InCocos", action)
  if (action == "action_showStarDialog") {
      runOnUiThread {
          AlertDialog.Builder(this)
              .setTitle("选择")
              .setItems(showArray) { _, index ->
                  CocosBridgeHelper.getInstance().nativeCallCocos(action, showArray[index], callbackId)
              }
              .create()
              .show()
      }
  }

}

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  CocosBridgeHelper.getInstance().addCocosListener(cocosListenerInCocos)
}
override fun onDestroy() {
  super.onDestroy()
  CocosBridgeHelper.getInstance().removeCocosListener(cocosListenerInCocos)
}
```

2.实现从主进程取数据给cocos显示
android/MainActivity.kt
```kotlin
private val cocosListenerInMain: CocosDataListener = CocosDataListener { action, argument, callbackId ->
  CocosBridgeHelper.log("接收InMain", action)
  if (action == "action_appVersion") {
      CocosBridgeHelper.getInstance().main2Cocos(action, packageManager.getPackageInfo(packageName, 0).versionName, callbackId)
  }
}
override fun onCreate(savedInstanceState: Bundle?) {
  CocosBridgeHelper.getInstance().addMainListener(cocosListenerInMain)
}

override fun onDestroy() {
  super.onDestroy()
  CocosBridgeHelper.getInstance().removeMainListener(cocosListenerInMain)
}
```

### 三、实现原理：
[Android实战——Cocos游戏容器搭建篇](https://github.com/qq326646683/tech-article/blob/master/android/Android实战——Cocos游戏容器搭建篇.md)

[Android实战——Cocos游戏容器通信篇](https://github.com/qq326646683/tech-article/blob/master/android/Android实战——Cocos游戏容器通信篇.md)
