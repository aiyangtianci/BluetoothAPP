package com.example.buletoothdemo.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.example.buletoothdemo.entity.DatasEntity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/**
 * 蓝牙工具类
 */

public class BluetoothUtil {

    /**
     * 蓝牙器
     */
    public static BluetoothAdapter mBluetoothAdapter(){
       return BluetoothAdapter.getDefaultAdapter();
    }


    /**
     * 广播拦截
     * @return IntentFilter
     */
    public static IntentFilter makeFilters() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//开关监听
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//查询
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//查询结束
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//绑定状况
        return intentFilter;
    }

    /**
     * 返回是否开启
     */
    public static boolean isOpenBluetooth(){
        if (BluetoothUtil.mBluetoothAdapter().isEnabled()) {//蓝牙已打开
            startGetBound();
            startSerch();
            return true;
        }else{
            return false;
        }
    }
    /**
     * 蓝牙开关
     */
    public static void switchBluetooth(Activity activity){
        if (!mBluetoothAdapter().isEnabled()) {
            boolean enable = mBluetoothAdapter().enable(); //直接打开
            if (!enable) {  //申请权限打开失败
                activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        } else {
            mBluetoothAdapter().disable();
        }
    }

    /**
     * 开启搜索的设备
     */
    public static void startSerch() {
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mBluetoothAdapter().isDiscovering()) {
            mBluetoothAdapter().cancelDiscovery();
        }
        DatasEntity.mBluetoothDevices.clear();
        mBluetoothAdapter().startDiscovery();
    }

    /**
     * 开启搜索已配对设备
     */
    public static void startGetBound(){
        DatasEntity.mPairedDevices.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {// 遍历到列表中
                DatasEntity.mPairedDevices.add(device);
            }
        }
    }
    /**
     * 发起配对
     */
    public static void connectBound(){
        if ( Comment.bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Comment.bluetoothDevice.createBond();
                    }
                }
            }).start();
        }
    }
    /**
     * 连接蓝牙
     */
    public static void connectSocket(Context context) {
        ToastUtil.showShort(context,"连接中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Comment.bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    try {
                        if (Comment.bluetoothSocket != null){
                            Comment.bluetoothSocket.close();
                            Comment.bluetoothSocket =null;
                        }
                        if (Build.VERSION.SDK_INT >= 10) {
                            Comment.bluetoothSocket = Comment.bluetoothDevice.createInsecureRfcommSocketToServiceRecord(Comment.SPP_UUID);
                        } else {
                            Comment.bluetoothSocket = Comment.bluetoothDevice.createRfcommSocketToServiceRecord(Comment.SPP_UUID);
                        }

                        if (!Comment.bluetoothSocket.isConnected()) {
                            Comment.bluetoothSocket.connect();//这里由SPP_UUID创建的客户端，去连接服务端。如果蓝牙端口不对称会异常。
                        }
                    } catch (Exception e) {
                        try {
                            Comment.bluetoothSocket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 连接蓝牙（2）
     * @param device
     * @return
     */
    public static BluetoothSocket connectDevice(BluetoothDevice device) {
        BluetoothSocket socket = null;
        try {
            socket = device.createRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException closeException) {
                return null;
            }
            return null;
        }
        return socket;
    }
    /**
     * 解除配对
     */
    public static void unpairDevice() {
        Method removeBondMethod = null;
        try {
            removeBondMethod = Comment.bluetoothDevice.getClass().getMethod("removeBond");
            removeBondMethod.invoke(Comment.bluetoothDevice);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}