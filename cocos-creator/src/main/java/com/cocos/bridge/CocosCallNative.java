package com.cocos.bridge;


import android.os.RemoteException;
import com.cocos.lib.CocosActivity;

// 用来cocos游戏调Native
public class CocosCallNative {
    public static void invoke(String action, String argument, String callbackId) {
        CocosBridgeHelper.log("cocos进程获得游戏端数据", "argument： " + argument + ",action: " + action + ",callbackId: " + callbackId);
        CocosBridgeHelper.getInstance().dispatchCocosListener(action, argument, callbackId);
        try {
            CocosActivity.mIAIDLCocos2Main.cocos2Main(action, argument, callbackId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

