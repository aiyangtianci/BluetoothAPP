package com.example.buletoothdemo.service;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.buletoothdemo.util.BluetoothUtil;
import com.example.buletoothdemo.util.Comment;
import com.example.buletoothdemo.util.DialogUtil;
import com.example.buletoothdemo.util.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Luhao on 2016/9/28.
 * 发送消息的服务
 */
public class SendSocketService {

    /**
     * 发送文本消息
     *
     * @param message
     */
    public static void sendMessage(String message) {
        if (Comment.bluetoothSocket == null || TextUtils.isEmpty(message)) return;
        try {
            message += "\n";
            OutputStream outputStream = Comment.bluetoothSocket.getOutputStream();
            outputStream.write(message.getBytes("utf-8"));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送文件
     */
    public static void sendMessageByFile(Context context,Uri filePath) {
        if (Comment.bluetoothSocket == null){
//            BluetoothUtil.connectSocket(context);
            ToastUtil.showShort(context,"蓝牙已配对但连接失败...");
        }
        Dialog dialog = new DialogUtil.DefineDialog(context,100,0,filePath);
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
                listenr.setProgress((int)(fileSize / file.length() * 100));
                //2、把文件写入socket输出流
                outputStream.write(b, 0, length);
            }
            //关闭文件流
            fis.close();
            //该方法无效
            //outputStream.write("\n".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static setProgessIml listenr;
    public interface setProgessIml{
        void setProgress(int size);
    }
    public  void setProgressListener(setProgessIml listenr){
        this.listenr =listenr;
    }


}
