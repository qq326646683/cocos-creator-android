/****************************************************************************
 * Copyright (c) 2020 Xiamen Yaji Software Co., Ltd.
 *
 * http://www.cocos.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/

package com.cocos.lib;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.cocos.bridge.Cocos2MainService;
import com.cocos.bridge.CocosBridgeHelper;

import java.io.File;
import java.lang.reflect.Field;

public class CocosActivity extends Activity implements SurfaceHolder.Callback, ServiceConnection {
    public static IAIDLCocos2Main mIAIDLCocos2Main;

    private boolean mDestroyed;
    private SurfaceHolder mSurfaceHolder;
    private FrameLayout mFrameLayout;
    private CocosSurfaceView mSurfaceView;
    private CocosWebViewHelper mWebViewHelper = null;
    private CocosVideoHelper mVideoHelper = null;
    private CocosOrientationHelper mOrientationHelper = null;

    private boolean engineInit = false;

    private CocosKeyCodeHandler mKeyCodeHandler;
    private CocosSensorHandler mSensorHandler;


    private native void onCreateNative(Activity activity, AssetManager assetManager, String obbPath, int sdkVersion, String filePath);

    private native void onSurfaceCreatedNative(Surface surface);

    private native void onSurfaceChangedNative(int width, int height);

    private native void onSurfaceDestroyNative();

    private native void onPauseNative();

    private native void onResumeNative();

    private native void onStopNative();

    private native void onStartNative();

    private native void onLowMemoryNative();

    private native void onWindowFocusChangedNative(boolean hasFocus);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalObject.setActivity(this);
        CocosHelper.registerBatteryLevelReceiver(this);
        CocosHelper.init(this);
        CanvasRenderingContext2DImpl.init(this);
        onLoadNativeLibraries();
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView();
        onCreateNative(this, getAssets(), getAbsolutePath(getObbDir()), Build.VERSION.SDK_INT, filePath());

        mKeyCodeHandler = new CocosKeyCodeHandler(this);
        mSensorHandler = new CocosSensorHandler(this);

        setImmersiveMode();

        Utils.hideVirtualButton();

        mOrientationHelper = new CocosOrientationHelper(this);

        Intent intent = new Intent(this, Cocos2MainService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    protected String filePath() {
        return "";
    }

    private void setImmersiveMode() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        try {
            Field field = lp.getClass().getField("layoutInDisplayCutoutMode");
            //Field constValue = lp.getClass().getDeclaredField("LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER");
            Field constValue = lp.getClass().getDeclaredField("LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES");
            field.setInt(lp, constValue.getInt(null));

            // https://developer.android.com/training/system-ui/immersive
            int flag = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

            flag |= View.class.getDeclaredField("SYSTEM_UI_FLAG_IMMERSIVE_STICKY").getInt(null);
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(flag);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static String getAbsolutePath(File file) {
        return (file != null) ? file.getAbsolutePath() : null;
    }

    protected void initView() {
        ViewGroup.LayoutParams frameLayoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mFrameLayout = new FrameLayout(this);
        mFrameLayout.setLayoutParams(frameLayoutParams);
        setContentView(mFrameLayout);

        mSurfaceView = new CocosSurfaceView(this);
        mSurfaceView.getHolder().addCallback(this);
        mFrameLayout.addView(mSurfaceView);

        if (mWebViewHelper == null) {
            mWebViewHelper = new CocosWebViewHelper(mFrameLayout);
        }

        if (mVideoHelper == null) {
            mVideoHelper = new CocosVideoHelper(this, mFrameLayout);
        }
    }

    public CocosSurfaceView getSurfaceView() {
        return this.mSurfaceView;
    }

    @Override
    protected void onDestroy() {
        mDestroyed = true;
        if (mSurfaceHolder != null) {
            onSurfaceDestroyNative();
            mSurfaceHolder = null;
        }
        super.onDestroy();
        unbindService(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorHandler.onPause();
        mOrientationHelper.onPause();
        onPauseNative();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorHandler.onResume();
        mOrientationHelper.onResume();
        onResumeNative();
    }

    @Override
    protected void onStop() {
        super.onStop();
        onStopNative();
    }

    @Override
    protected void onStart() {
        super.onStart();
        onStartNative();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (!mDestroyed) {
            onLowMemoryNative();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mDestroyed) {
            onWindowFocusChangedNative(hasFocus);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mDestroyed) {
            mSurfaceHolder = holder;
            onSurfaceCreatedNative(holder.getSurface());

            engineInit = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!mDestroyed) {
            mSurfaceHolder = holder;
            onSurfaceChangedNative(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
        if (!mDestroyed) {
            onSurfaceDestroyNative();
            engineInit = false;
        }
    }

    private void onLoadNativeLibraries() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String libName = bundle.getString("android.app.lib_name");
            System.loadLibrary(libName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            if (mIAIDLCocos2Main == null) return;

            mIAIDLCocos2Main.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mIAIDLCocos2Main = null;
            // 重新绑定远程服务
            Intent intent = new Intent(CocosActivity.this, Cocos2MainService.class);
            bindService(intent, CocosActivity.this, Context.BIND_AUTO_CREATE);
        }
    };

    private IAIDLCallBack iAidlCallBack = new IAIDLCallBack.Stub() {

        @Override
        public void main2Cocos(String action, String argument, String callbackId) {
            CocosBridgeHelper.log("Cocos进程收到主进程消息", "action: " + action + ", argument: " + argument + ", callbackId: " + callbackId);
            CocosBridgeHelper.getInstance().nativeCallCocos(action, argument, callbackId);
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mIAIDLCocos2Main = IAIDLCocos2Main.Stub.asInterface(service);
        try {
            // 注册死亡代理
            service.linkToDeath(mDeathRecipient, 0);
            mIAIDLCocos2Main.setAIDLCallBack(iAidlCallBack);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mIAIDLCocos2Main = null;
    }
}
