package com.example.buletoothdemo.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.example.buletoothdemo.entity.DatasEntity;

import java.io.IOException;
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
    public static BluetoothAdapter mBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }


    /**
     * 广播拦截
     *
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
    public static boolean isOpenBluetooth() {
        if (BluetoothUtil.mBluetoothAdapter().isEnabled()) {//蓝牙已打开
            startGetBound();
            startSerch();
            return true;
        } else {
            return false;
        }
    }


    /**
     * 蓝牙开关
     */
    public static void switchBluetooth(Activity activity) {
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
    public static void startGetBound() {
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
    public static void connectBound() {
        if (Comment.bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Comment.bluetoothDevice.createBond();
                        } else {
                            Method method = BluetoothDevice.class.getMethod("createBond");
                            method.invoke(Comment.bluetoothDevice);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 连接蓝牙
     */
    public static void connectSocket(final Handler handler) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Comment.bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    try {
                        if (Comment.bluetoothSocket != null) {
                            Comment.bluetoothSocket.close();
                            Comment.bluetoothSocket = null;
                        }
                        if (Build.VERSION.SDK_INT >= 10) {
                            Comment.bluetoothSocket = Comment.bluetoothDevice.createInsecureRfcommSocketToServiceRecord(Comment.SPP_UUID);
                        } else {
                            Comment.bluetoothSocket = Comment.bluetoothDevice.createRfcommSocketToServiceRecord(Comment.SPP_UUID);
                        }

                        if (!Comment.bluetoothSocket.isConnected()) {
                            Comment.bluetoothSocket.connect();//UUID创建的客户端，去连接服务端。如果蓝牙端口协议不对称会异常。
                            handler.sendEmptyMessage(Comment.CONNECT);
                        }
                    } catch (Exception e) {
                        try {   //尝试第二种方式连接
                            Comment.bluetoothSocket = (BluetoothSocket) Comment.bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(Comment.bluetoothDevice, 1);
                            if (!Comment.bluetoothSocket.isConnected()) {
                                Comment.bluetoothSocket.connect();//UUID创建的客户端，去连接服务端。如果蓝牙端口协议不对称会异常。
                                handler.sendEmptyMessage(Comment.CONNECT);
                            }
                        } catch (Exception e1) {
                            try {
                                Comment.bluetoothSocket.close();//最终失败
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
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
     *
     * @return
     */
    public static BluetoothSocket connectDevice(final Handler handler) {
        BluetoothSocket socket = null;
        try {
            Comment.SPP_UUID = UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");
            socket = Comment.bluetoothDevice.createRfcommSocketToServiceRecord(Comment.SPP_UUID);
            socket.connect();
            handler.sendEmptyMessage(Comment.CONNECT);
        } catch (Exception e) {
            handler.sendEmptyMessage(2);
            try {
                if (socket!=null)
                socket.close();
            } catch (Exception closeException) {

            }
        }
        return socket;
}

    /**
     * 解除配对
     */
    public static void unpairDevice() {
        Method removeBondMethod = null;
        try {
//            removeBondMethod = Comment.bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
//            removeBondMethod.invoke(Comment.bluetoothDevice, (Object[]) null);
            removeBondMethod = Comment.bluetoothDevice.getClass().getMethod("removeBond");
            removeBondMethod.invoke(Comment.bluetoothDevice);
            Log.d("aaa", "ok");
        } catch (Exception e) {
            Log.d("aaa", "error");
            e.printStackTrace();
        }
    }
}
