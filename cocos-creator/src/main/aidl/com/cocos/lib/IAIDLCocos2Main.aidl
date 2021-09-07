package com.cocos.lib;
import com.cocos.lib.IAIDLCallBack;

interface IAIDLCocos2Main {
    void cocos2Main(String action, String argument, String callbackId);
    void setAIDLCallBack(IAIDLCallBack iAIDLCallBack);
}