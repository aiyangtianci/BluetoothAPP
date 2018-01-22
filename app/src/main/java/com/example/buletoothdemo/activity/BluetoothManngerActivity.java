package com.example.buletoothdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.buletoothdemo.R;
import com.example.buletoothdemo.broadcast.BTBroadcastReceiver;
import com.example.buletoothdemo.service.SendSocketService;
import com.example.buletoothdemo.util.BluetoothUtil;
import com.example.buletoothdemo.util.Comment;
import com.example.buletoothdemo.util.DialogUtil;
import com.example.buletoothdemo.util.PrintUtil;
import com.example.buletoothdemo.util.ProgressDialogUtil;
import com.example.buletoothdemo.util.ToastUtil;

import java.io.IOException;

/**
 * 操作管理页面
 */

public class BluetoothManngerActivity extends AppCompatActivity {
    private TextView device_name;
    private LinearLayout unpair_ll;
    private LinearLayout send_photo_ll;
    private LinearLayout printer_ll;
    private BTBroadcastReceiver receiver;

    //蓝牙打印
    private AsyncTask mConnectTask;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mannager);
        receiver = new BTBroadcastReceiver(mHandler);
        registerReceiver(receiver, BluetoothUtil.makeFilters());
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    //初始化
    private void init() {
        if (Comment.bluetoothDevice ==null){
            ToastUtil.showShort(this,"该蓝牙需要重新配对");
        }
        device_name = (TextView) findViewById(R.id.device_name);
        device_name.setText(Comment.bluetoothDevice.getAddress());
        unpair_ll = (LinearLayout) findViewById(R.id.unpair_ll);
        unpair_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Comment.bluetoothDevice ==null){
                    ToastUtil.showShort(BluetoothManngerActivity.this,"该蓝牙需要重新配对");
                }else{
                    BluetoothUtil.unpairDevice();
                }
            }
        });
        send_photo_ll = (LinearLayout) findViewById(R.id.send_photo_ll);
        send_photo_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Comment.bluetoothDevice ==null){
                    ToastUtil.showShort(BluetoothManngerActivity.this,"该蓝牙需要重新配对");
                }else{
                    sendPhotoDialog();
                }

            }
        });
        printer_ll = (LinearLayout) findViewById(R.id.printer_ll);
        printer_ll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (Comment.bluetoothDevice ==null){
                    ToastUtil.showShort(BluetoothManngerActivity.this,"该蓝牙需要重新配对");
                }else {
                    mConnectTask = new ConnectBluetoothTask().execute(Comment.bluetoothDevice);
                }
            }
        });
    }

    private void sendPhotoDialog() {
        DialogUtil.ShowAlertDialog(this,"提示","请选择图片",new DialogInterface.OnClickListener() {//选择照片
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //选择图片
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, Comment.IMAGE_CODE);
            }
        });
    }

    /**
     * 页面跳转处理
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Comment.IMAGE_CODE){
            if (resultCode == this.RESULT_OK){
                Uri uri = data.getData();
                SendSocketService.sendMessageByFile(this,uri);//蓝牙发送！！！！！！
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Comment.BOND){
                if ((int)msg.obj == BluetoothDevice.BOND_NONE){
                    ToastUtil.showShort(BluetoothManngerActivity.this,"已取消配对");
                }
            }
        }
    };

    class ConnectBluetoothTask extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket> {

        @Override
        protected void onPreExecute() {
            ProgressDialogUtil.show(BluetoothManngerActivity.this,"请稍候...");
            super.onPreExecute();
        }

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... params) {
            if(Comment.bluetoothSocket != null){
                try {
                    Comment.bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Comment.bluetoothSocket = BluetoothUtil.connectDevice(params[0]);
            return Comment.bluetoothSocket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket socket) {
            ProgressDialogUtil.cancel();
            if (socket == null || !socket.isConnected()) {
                ToastUtil.showLong(BluetoothManngerActivity.this,"连接打印机失败");
            } else {
                ToastUtil.showShort(BluetoothManngerActivity.this,"成功！");
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bluetooth_log);
                PrintUtil.printTest(Comment.bluetoothSocket, bitmap);
            }
            super.onPostExecute(socket);
        }
    }
}
