package com.nell.cocos

import android.os.Bundle
import com.cocos.lib.CocosActivity
import java.io.File

class CocosGameActivity: CocosActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun filePath() = intent.getStringExtra("path")
}