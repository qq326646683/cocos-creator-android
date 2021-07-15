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
       implementation 'com.github.qq326646683:cocos-creator-android:1.0.0'
}
```

### 一、如何使用
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

### 二、后续计划
cocos游戏和android通信，因为牵扯到多进程，通信变的麻烦，后续计划将这部分内容封装在module library，方便使用者调用

### 三、[Android实战——Cocos游戏容器搭建篇](https://github.com/qq326646683/tech-article/blob/master/android/Android实战——Cocos游戏容器搭建篇.md)
