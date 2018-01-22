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
    private LinearLayout send_message_ll;
    private BTBroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mannager);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new BTBroadcastReceiver(mHandler);
        registerReceiver(receiver, BluetoothUtil.makeFilters());
    }

    /**
     * 处理解绑
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Comment.BOND) {
                if ((int) msg.obj == BluetoothDevice.BOND_NONE) {
                    DialogUtil.CancelProgress();
                    ToastUtil.showShort(BluetoothManngerActivity.this, "取消配对成功");
                    finish();
                }
            }if (msg.what ==Comment.CONNECT){
                ToastUtil.showShort(BluetoothManngerActivity.this,"连接成功！");
            }if (msg.what==0){
                ToastUtil.showShort(BluetoothManngerActivity.this, "传输成功!");
            }if (msg.what==1){
                ToastUtil.showShort(BluetoothManngerActivity.this, "传输失败!");
            }if (msg.what==2){
                ToastUtil.showShort(BluetoothManngerActivity.this, "连接失败!");
            }

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    //初始化
    private void init() {
        isDeviceBound();
        device_name = (TextView) findViewById(R.id.device_name);
        if (Comment.bluetoothDevice.getName()==null||Comment.bluetoothDevice.getName().equals("null")){
            device_name.setText(Comment.bluetoothDevice.getAddress());
        }else{
            device_name.setText(Comment.bluetoothDevice.getName());
        }

        unpair_ll = (LinearLayout) findViewById(R.id.unpair_ll);
        unpair_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDeviceBound();
                DialogUtil.ShowProgress(BluetoothManngerActivity.this, "正在取消...");
                BluetoothUtil.unpairDevice();

            }
        });
        send_message_ll =(LinearLayout)findViewById(R.id.send_message_ll);
        send_message_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDeviceBound();
                sendMessageDialog();

            }
        });
        send_photo_ll = (LinearLayout) findViewById(R.id.send_photo_ll);
        send_photo_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDeviceBound();
                sendPhotoDialog();


            }
        });
        printer_ll = (LinearLayout) findViewById(R.id.printer_ll);
        printer_ll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                isDeviceBound();
                 new ConnectBluetoothTask().execute();
            }
        });
    }

    //弹框选照片
    private void sendPhotoDialog() {
        DialogUtil.ShowAlertDialog(this, "提示", "请选择图片", new DialogInterface.OnClickListener() {//选择照片
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //选择图片
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, Comment.IMAGE_CODE);
            }
        });
    }
    //弹框发消息
    private void sendMessageDialog(){
        DialogUtil.ShowAlertDialog(this, "提示", "是否发送消息给蓝牙：" + device_name.getText().toString(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SendSocketService.sendMessage("你好，收到一条消息来自蓝牙："+Comment.bluetoothDevice.getAddress(),mHandler);
            }
        });

    }

    /**
     * 检查设备是否存在
     */
    public void isDeviceBound() {
        if (Comment.bluetoothDevice == null) {
            ToastUtil.showShort(BluetoothManngerActivity.this, "该蓝牙需要重新配对");
            finish();
        }
    }

    /**
     * 页面跳转处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Comment.IMAGE_CODE) {
            if (resultCode == this.RESULT_OK) {
                Uri uri = data.getData();
                SendSocketService.sendMessageByFile(this, uri,mHandler);//蓝牙发送！！！！！！
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class ConnectBluetoothTask extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket> {

        @Override
        protected void onPreExecute() {
            DialogUtil.ShowProgress(BluetoothManngerActivity.this, "请稍候...");
            super.onPreExecute();
        }

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... params) {
            if (Comment.bluetoothSocket != null) {
                try {
                    Comment.bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Comment.bluetoothSocket = BluetoothUtil.connectDevice(mHandler);
            return Comment.bluetoothSocket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket socket) {
            DialogUtil.CancelProgress();
            if (socket == null || !socket.isConnected()) {
                ToastUtil.showShort(BluetoothManngerActivity.this, "连接打印机失败");
            } else {
                ToastUtil.showShort(BluetoothManngerActivity.this, "开始打印");
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bluetooth_log);
                PrintUtil.printTest(Comment.bluetoothSocket, bitmap);
            }
            super.onPostExecute(socket);
        }
    }
}
