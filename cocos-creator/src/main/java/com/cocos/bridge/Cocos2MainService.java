package com.cocos.bridge;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


import androidx.annotation.Nullable;
import com.cocos.lib.IAIDLCallBack;
import com.cocos.lib.IAIDLCocos2Main;

public class Cocos2MainService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    IAIDLCocos2Main.Stub mBinder = new IAIDLCocos2Main.Stub() {

        @Override
        public void cocos2Main(String action, String argument, String callbackId) {
            CocosBridgeHelper.log("主进程收到Cocos进程消息", "action:" + action + "argument:" + argument + "callbackId:" + callbackId);
            CocosBridgeHelper.getInstance().dispatchMainListener(action, argument, callbackId);
        }

        @Override
        public void setAIDLCallBack(IAIDLCallBack iAIDLCallBack) {
            // 将cocos进程的IAIDLCallBack传到主进程，用来主进程发消息给Cocos进程
            CocosBridgeHelper.getInstance().setIAIDLCallBack(iAIDLCallBack);
        }
    };
}
