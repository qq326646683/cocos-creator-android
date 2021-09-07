package com.cocos.bridge;

import android.os.RemoteException;
import android.util.Log;

import com.cocos.lib.CocosHelper;
import com.cocos.lib.CocosJavascriptJavaBridge;
import com.cocos.lib.IAIDLCallBack;

import java.util.ArrayList;
import java.util.List;

public class CocosBridgeHelper {
    private static CocosBridgeHelper mInstance;

    private IAIDLCallBack mIAIDLCallBack;

    private List<CocosDataListener> mCocosDataListenerList_InMain = new ArrayList<>();
    private List<CocosDataListener> mCocosDataListenerList_InCocos = new ArrayList<>();

    /**
     * 主进程发数据给cocos进程，并发给cocos游戏
     *
     * @param action
     * @param argument
     */
    public void main2Cocos(String action, String argument, String callbackId) {
        try {
            mIAIDLCallBack.main2Cocos(action, argument, callbackId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void nativeCallCocos(String action, String argument, String callbackId) {
        CocosHelper.runOnGameThread(() -> CocosJavascriptJavaBridge.evalString(String.format("cc.nativeCallCocos('%s', '%s', '%s');", action, argument, callbackId)));
    }

    /**
     * 监听cocos发来的数据
     *
     * @param listener
     */
    public void addMainListener(CocosDataListener listener) {
        mCocosDataListenerList_InMain.add(listener);
    }

    public void addCocosListener(CocosDataListener listener) {
        mCocosDataListenerList_InCocos.add(listener);
    }

    public void removeMainListener(CocosDataListener listener) {
        mCocosDataListenerList_InMain.remove(listener);
    }

    public void removeCocosListener(CocosDataListener listener) {
        mCocosDataListenerList_InCocos.remove(listener);
    }

    public void dispatchMainListener(String action, String argument, String callbackId) {
        for (CocosDataListener listener : mCocosDataListenerList_InMain) {
            listener.onCocosData(action, argument, callbackId);
        }
    }

    public void dispatchCocosListener(String action, String argument, String callbackId) {
        for (CocosDataListener listener : mCocosDataListenerList_InCocos) {
            listener.onCocosData(action, argument, callbackId);
        }
    }

    public void setIAIDLCallBack(IAIDLCallBack iAIDLCallBack) {
        mIAIDLCallBack = iAIDLCallBack;
    }

    public static CocosBridgeHelper getInstance() {
        if (mInstance == null) {
            mInstance = new CocosBridgeHelper();
        }
        return mInstance;
    }

    public static void log(String tag, String msg) {
        Log.d("CocosBridge-pid:" + android.os.Process.myPid() + "-" + tag, msg);
    }
}
