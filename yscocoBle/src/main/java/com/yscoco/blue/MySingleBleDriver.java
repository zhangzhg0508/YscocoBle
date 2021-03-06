package com.yscoco.blue;
/**
 * Created by Administrator on 2017/12/9 0009.
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.yscoco.blue.base.BaseSingleBleDriver;
import com.yscoco.blue.enums.DeviceState;
import com.yscoco.blue.listener.BleDataListener;
import com.yscoco.blue.listener.BleStateListener;
import com.yscoco.blue.utils.BleDataUtils;
import com.yscoco.blue.utils.LogBlueUtils;

import java.util.HashSet;

/**
 * 作者：karl.wei
 * 创建日期： 2017/12/9 0009 13:46
 * 邮箱：karl.wei@yscoco.com
 * QQ:2736764338
 * 类介绍：BaseBlueDriver实现类
 */
public class MySingleBleDriver extends BaseSingleBleDriver {
    /*通知监听*/
    protected HashSet<BleStateListener> mListeners;
    protected HashSet<BleDataListener> mNotifyListeners;
    private static MySingleBleDriver myDriver ;
    public static MySingleBleDriver getInstance(Context context){
        if(myDriver==null){
            synchronized (MySingleBleDriver.class) {
                if(myDriver==null) {
                    myDriver = new MySingleBleDriver(context);
                }
            }
        }
        return myDriver;
    }
    private MySingleBleDriver(Context context){
        super(context);
    }
    @Override
    public void dataHandler(String uuid,String address, byte[] data,int what) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            b.append(String.format("%02X ", data[i]).toString().trim());
        }
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("uuid",uuid);
        bundle.putByteArray("value",data);
        bundle.putString("data",b.toString());
        bundle.putString("mac", address);
        msg.setData(bundle);
        msg.what = what;
//        handlerMsg(address,msg);
        mHandler.sendMessage(msg);
    }

    @Override
    public void handlerMsg(String address, Message msg) {
        int what = msg.what;
        switch (what){
            /*开始连接*/
            case MyBtManager.CONNECTING:
                removeReconnect(address);
                if(mListeners!=null) {
                    for (BleStateListener listner : mListeners) {
                        listner.deviceStateChange(address, DeviceState.CONNECTING);
                    }
                }
                break;
            /*设备连接成功*/
            case MyBtManager.CONNECTED:
                if(mListeners!=null) {
                    for (BleStateListener listner : mListeners) {
                        listner.deviceStateChange(address, DeviceState.CONNECT);
                    }
                }
                break;
            /*设备断开连接中*/
            case MyBtManager.DISCONNECTING:
                if(mListeners!=null) {
                    for (BleStateListener listner : mListeners) {
                        listner.deviceStateChange(address, DeviceState.DISCONNECTING);
                    }
                }
                break;
            /*设备断开连接*/
            case MyBtManager.DISCONNECT:
                LogBlueUtils.e("移除设备"+address);
                if(mBtManager!=null&&mBtManager.getmMac().equals(address)){
                    mBtManager = null;
                }
                if(mListeners!=null) {
                    for (BleStateListener listner : mListeners) {
                        listner.deviceStateChange(address, DeviceState.DISCONNECT);
                    }
                }
                break;
            case MyBtManager.RE_CONNECT:
                addReconnect(address);
                if(mListeners!=null) {
                    for (BleStateListener listner : mListeners) {
                        listner.reConnected(address);
                    }
                }
                break;
            /*在通道开启成功后显示成功*/
            case MyBtManager.NOTIFY_ON:
                if(mListeners!=null) {
                    for (BleStateListener listner : mListeners) {
                        listner.onNotifySuccess(address);
                    }
                }
                break;
            case MyBtManager.NOTIFY_VALUE:
                Bundle bundle = msg.getData();
                String uuid = bundle.getString("uuid");
                byte[] data = bundle.getByteArray("value");
                if(mNotifyListeners!=null&&mNotifyListeners.size()>0) {
                    BleDataUtils.notify(uuid,address,data, mNotifyListeners);
                }
                break;
            case MyBtManager.READ_VALUE:
                Bundle bundle1 = msg.getData();
                String uuid1 = bundle1.getString("uuid");
                byte[] data1 = bundle1.getByteArray("value");
                if(mNotifyListeners!=null&&mNotifyListeners.size()>0) {
                    BleDataUtils.read(uuid1,address,data1, mNotifyListeners);
                }
                break;
            case MyBtManager.NOTIFY_RSSI:
                int rssi =((int) msg.obj);
                if(mNotifyListeners!=null&&mNotifyListeners.size()>0) {
                    for (BleDataListener listner : mNotifyListeners) {
                    }
                }
                break;
                default:
        }
    }


    @Override
    public void addBleStateListener(BleStateListener listener) {
        if (mListeners == null) {
            mListeners = new HashSet<>();
        }
        mListeners.add(listener);
    }
    @Override
    public void removeBleStateListener(BleStateListener listener) {
        if(mListeners!=null) {
            mListeners.remove(listener);
        }
    }
    @Override
    public void addBleDataListener(BleDataListener listener) {
        if (mNotifyListeners == null) {
            mNotifyListeners = new HashSet<>();
        }
        mNotifyListeners.add(listener);
    }
    @Override
    public void removeBleDataListener(BleDataListener listener) {
        if(mNotifyListeners!=null) {
            mNotifyListeners.remove(listener);
        }
    }
}
