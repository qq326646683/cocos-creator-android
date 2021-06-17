package com.nell.cocos

interface DownloadListener {
    fun callBack(taskId: Int, status: Int, progress: Int)
}