package com.nell.cocos

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import com.cocos.lib.GlobalObject

class MainActivity : AppCompatActivity() {
    val download1Url = "http://file.jinxianyun.com/default.zip"
    var btnDownload1: AppCompatButton? = null

    val listener: DownloadListener = object : DownloadListener {
        override fun callBack(taskId: Int, status: Int, progress: Int) {
            Log.i("nell--", "taskId:$taskId, status:$status, progress:$progress")
            if (DownloadUtil.instance.taskMap.get(download1Url) == taskId) {
                when (status) {
                    DownloadManager.STATUS_RUNNING -> {
                        btnDownload1?.text = "下载中${progress}%"
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        btnDownload1?.text = "解压中"
                        val zipFileString = "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath}/${download1Url.split("/").last()}"
                        val outPathString = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
                        FileUtil.unzip(
                            zipFileString,
                            outPathString,
                            object : FileUtil.ZipProgress {
                                override fun onProgress(progress: Int) {
                                    btnDownload1?.text = "解压中$progress"
                                }
                                override fun onDone() {
                                    btnDownload1?.text = "打开游戏"
                                    btnDownload1?.setOnClickListener {
                                        val intent = Intent(GlobalObject.getActivity(), CocosGameActivity::class.java)
                                        intent.putExtra("path", "${outPathString}/${download1Url.split("/").last().split(".").first()}")
                                        startActivity(intent)
                                    }
                                }
                            }
                        )
                    }
                    DownloadManager.STATUS_FAILED -> {
                        btnDownload1?.text = "下载失败"
                    }
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalObject.setActivity(this)

        requestPermissions(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), 20)

        DownloadUtil.instance.addListener(listener)

        btnDownload1 = findViewById<AppCompatButton>(R.id.btn_download).apply {
            setOnClickListener {
                DownloadUtil.instance.download(download1Url)
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadUtil.instance.removeListener(listener)
    }
}