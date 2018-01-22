package com.example.buletoothdemo.util;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.UUID;

public class Comment {

    /**
     * 蓝牙UUID
     */
    public static UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * 选择配对后的设备，保持与应用生命周期相同
     */
    public static  BluetoothDevice bluetoothDevice;

    /**
     * 不管是蓝牙连接方还是服务器方，得到socket对象后都传入
     */
    public static BluetoothSocket bluetoothSocket;

    /**
     * 蓝牙开关
     */
    public static final int SWITCH = 101;

    /**
     * 蓝牙搜索
     */
    public static final int FOUND = 102;

    /**
     * 蓝牙搜索完毕
     */
    public static final int FINISHED = 103;

    /**
     * 蓝牙配对
     */
    public static final int BOND =104;
    /**
     * 蓝牙连接
     */
    public static final int CONNECT =105;

    /**
     * 选择图片后的请求码
     */
    public static final int IMAGE_CODE = 0;

    /**
     * 修改蓝牙名的请求码
     */
    public static final int NAME_CODE = 1;

}
