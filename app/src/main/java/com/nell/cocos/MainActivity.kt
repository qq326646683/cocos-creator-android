package com.nell.cocos

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.cocos.bridge.CocosBridgeHelper
import com.cocos.bridge.CocosDataListener
import com.cocos.lib.GlobalObject

class MainActivity : AppCompatActivity() {
    val download1Url = "http://file.jinxianyun.com/default8.zip"
    val download2Url = "http://file.jinxianyun.com/hellococos.zip"

    var btnDownload1: Button? = null
    var btnDownload2: Button? = null

    private val downloadListener: DownloadListener = object : DownloadListener {
        override fun callBack(taskId: Int, status: Int, progress: Int) {
            updateUI(download1Url, btnDownload1, taskId, status, progress)
            updateUI(download2Url, btnDownload2, taskId, status, progress)
        }
    }

    private val cocosListenerInMain: CocosDataListener = CocosDataListener { action, argument, callbackId ->
        CocosBridgeHelper.log("接收InMain", action)
        if (action == "action_appVersion") {
            CocosBridgeHelper.getInstance().main2Cocos(action, packageManager.getPackageInfo(packageName, 0).versionName, callbackId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalObject.setActivity(this)
        requestPermissions(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), 20)

        DownloadUtil.instance.addListener(downloadListener)
        CocosBridgeHelper.getInstance().addMainListener(cocosListenerInMain)

        btnDownload1 = findViewById<AppCompatButton>(R.id.btn_download1).apply {
            setOnClickListener {
                DownloadUtil.instance.download(download1Url)
            }
        }
        btnDownload2 = findViewById<AppCompatButton>(R.id.btn_download2).apply {
            setOnClickListener {
                DownloadUtil.instance.download(download2Url)
            }
        }

        CocosBridgeHelper.log("主进程", "---")

    }

    fun updateUI(url: String, btn: Button?, taskId: Int, status: Int, progress: Int) {
        if (DownloadUtil.instance.taskMap.get(url) == taskId) {
            when (status) {
                DownloadManager.STATUS_RUNNING -> {
                    btn?.text = "下载中${progress}%"
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    btn?.text = "解压中"
                    val zipFileString =
                        "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath}/${
                            url.split("/").last()
                        }"
                    val outPathString =
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
                    FileUtil.unzip(
                        zipFileString,
                        outPathString,
                        object : FileUtil.ZipProgress {
                            override fun onProgress(progress: Int) {
                                btn?.text = "解压中$progress"
                            }

                            override fun onDone() {
                                btn?.text = "打开游戏"
                                btn?.setOnClickListener {
                                    val intent = Intent(
                                        this@MainActivity,
                                        CocosGameActivity::class.java
                                    )
                                    intent.putExtra(
                                        "path",
                                        "${outPathString}/${
                                            url.split("/").last().split(".").first()
                                        }"
                                    )
                                    startActivity(intent)
                                }
                            }
                        }
                    )
                }
                DownloadManager.STATUS_FAILED -> {
                    btn?.text = "下载失败"
                }
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        DownloadUtil.instance.removeListener(downloadListener)
        CocosBridgeHelper.getInstance().removeMainListener(cocosListenerInMain)

    }
}