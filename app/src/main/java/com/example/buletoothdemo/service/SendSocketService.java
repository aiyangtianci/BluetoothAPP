package com.example.buletoothdemo.service;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.example.buletoothdemo.util.BluetoothUtil;
import com.example.buletoothdemo.util.Comment;
import com.example.buletoothdemo.util.DialogUtil;
import com.example.buletoothdemo.util.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 发送消息的服务
 */
public class SendSocketService {

    /**
     * 发送文本消息
     *
     * @param message
     */
    public static void sendMessage(final String message, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream os = null;
                try {
                    if (Comment.bluetoothSocket == null || !Comment.bluetoothSocket.isConnected()) {
                        Comment.bluetoothSocket = BluetoothUtil.connectDevice(handler);
                    }
                    os = Comment.bluetoothSocket.getOutputStream();
                    os.write(message.getBytes());
                    os.flush();
                    handler.sendEmptyMessage(0);
                } catch (Exception e) {
                    handler.sendEmptyMessage(1);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 发送文件
     */
    public static void sendMessageByFile(Context context, Uri filePath, final Handler handler) {
        if (Comment.bluetoothSocket == null) {
            ToastUtil.showShort(context, "蓝牙连接失败...");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Comment.bluetoothSocket == null || !Comment.bluetoothSocket.isConnected()) {
                    Comment.bluetoothSocket = BluetoothUtil.connectDevice(handler);
                }
            }
        }).start();
        Dialog dialog = new DialogUtil.DefineDialog(context, 100, 0, filePath);
        dialog.show();
        try {
            File file = new File(filePath.toString());
            if (!file.exists()) return;
            //将文件写入流
            FileInputStream fis = new FileInputStream(file);
            //每次上传1M的内容
            byte[] b = new byte[1024];
            int length;
            int fileSize = 0;//实时监测上传进度

            OutputStream outputStream = Comment.bluetoothSocket.getOutputStream();
            while ((length = fis.read(b)) != -1) {
                fileSize += length;
                listenr.setProgress((int) (fileSize / file.length() * 100));
                //2、把文件写入socket输出流
                outputStream.write(b, 0, length);
            }
            //关闭文件流
            fis.close();
            //该方法无效
            //outputStream.write("\n".getBytes());
            outputStream.flush();
            handler.sendEmptyMessage(0);
        } catch (IOException e) {
            handler.sendEmptyMessage(1);
            e.printStackTrace();
        }
    }

    static setProgessIml listenr;

    public interface setProgessIml {
        void setProgress(int size);
    }

    public void setProgressListener(setProgessIml listenr) {
        this.listenr = listenr;
    }


}
