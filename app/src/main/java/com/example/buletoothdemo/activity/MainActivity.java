package com.example.buletoothdemo.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.buletoothdemo.R;
import com.example.buletoothdemo.adapter.FoundAdapter;
import com.example.buletoothdemo.adapter.PairedAdapter;
import com.example.buletoothdemo.broadcast.BTBroadcastReceiver;
import com.example.buletoothdemo.entity.DatasEntity;
import com.example.buletoothdemo.util.BluetoothUtil;
import com.example.buletoothdemo.util.Comment;
import com.example.buletoothdemo.util.DialogUtil;
import com.example.buletoothdemo.util.ListViewHeightMesure;
import com.example.buletoothdemo.util.ToastUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //控件
    private Switch btnSearch;
    private TextView mMobileName;
    private LinearLayout swipe;
    private ListView mLvallDevices;
    private ListView mLvPairedDevices;
    private LinearLayout switch_ll;
    private ImageView rotate_img;
    private LinearLayout name_ll;
    //adapter
    private PairedAdapter mPairedAdapter;
    private FoundAdapter mGetarrayAdapter;
    //bluetooth
    private BTBroadcastReceiver receiver;
    private Animation rotate;
    private TextView nonebound_tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        //刷新数据
        reswipeAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //广播
        receiver = new BTBroadcastReceiver(mHandler);
        registerReceiver(receiver, BluetoothUtil.makeFilters());
        reswipeAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BluetoothUtil.mBluetoothAdapter().isDiscovering()) {
            BluetoothUtil.mBluetoothAdapter().cancelDiscovery();
            setTitle("玩蓝牙");
            rotate_img.clearAnimation();
        }
        unregisterReceiver(receiver);
    }

    /**
     * 绑定控件
     */
    private void findViewById() {
        swipe = (LinearLayout) findViewById(R.id.swipe);
        rotate_img = (ImageView) findViewById(R.id.rotate_img);
        swipe.setOnClickListener(this);
        btnSearch = (Switch) findViewById(R.id.btnSearch);
        mMobileName = (TextView) findViewById(R.id.mobilename);
        mLvPairedDevices = (ListView) findViewById(R.id.alreadyDevices);
        mLvallDevices = (ListView) findViewById(R.id.allDevices);

        switch_ll = (LinearLayout) findViewById(R.id.switch_ll);
        switch_ll.setOnClickListener(this);
        name_ll = (LinearLayout) findViewById(R.id.name_ll);
        name_ll.setOnClickListener(this);

        //已适配的数据
        mPairedAdapter = new PairedAdapter(this);
        mLvPairedDevices.setAdapter(mPairedAdapter);
        mLvPairedDevices.setOnItemClickListener(paireItemListener);
        //未适配的数据
        mGetarrayAdapter = new FoundAdapter(this);
        mLvallDevices.setAdapter(mGetarrayAdapter);
        mLvallDevices.setOnItemClickListener(allItemListener);
        rotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        nonebound_tv = (TextView) findViewById(R.id.nonebound_tv);
    }

    //未配对列表点击事件
    private ListView.OnItemClickListener allItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Comment.bluetoothDevice = mGetarrayAdapter.getItem(i);
            if (BluetoothUtil.mBluetoothAdapter().isDiscovering()) {
                BluetoothUtil.mBluetoothAdapter().cancelDiscovery();
                setTitle("玩蓝牙");
                rotate_img.clearAnimation();
            }
            BluetoothUtil.connectBound();
        }
    };
    //已配对列表点击事件
    private ListView.OnItemClickListener paireItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Comment.bluetoothDevice = mPairedAdapter.getItem(i);
            if (BluetoothUtil.mBluetoothAdapter().isDiscovering()) {
                BluetoothUtil.mBluetoothAdapter().cancelDiscovery();
                setTitle("玩蓝牙");
                rotate_img.clearAnimation();
            }
            startActivity(new Intent(MainActivity.this, BluetoothManngerActivity.class));
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switch_ll://开关
                BluetoothUtil.switchBluetooth(this);
                break;
            case R.id.swipe://刷新
                clearAdapter();
                reswipeAdapter();
                break;
            case R.id.name_ll:
                startActivity(new Intent(MainActivity.this, NameActivity.class).putExtra("name", BluetoothUtil.mBluetoothAdapter().getName()));
                break;
        }
    }

    /**
     * 刷新数据
     */
    private void reswipeAdapter() {
        if (BluetoothUtil.mBluetoothAdapter() == null) {
            ToastUtil.showShort(this, "本地蓝牙不可用");
            return;
        }
        if (BluetoothUtil.isOpenBluetooth()) {
            btnSearch.setChecked(true);
            setTitle("蓝牙设备搜索中...");
            rotate_img.startAnimation(rotate);
            BluetoothUtil.startGetBound();
            mPairedAdapter.notifyDataSetChanged();
            ListViewHeightMesure.setAdapterHeight(mLvPairedDevices);
        }
        mMobileName.setText(BluetoothUtil.mBluetoothAdapter().getName() == null ? "未知设备" : BluetoothUtil.mBluetoothAdapter().getName());
    }

    /**
     * 清除数据
     */
    private void clearAdapter() {
        DatasEntity.mPairedDevices.clear();
        DatasEntity.mBluetoothDevices.clear();
        mPairedAdapter.notifyDataSetChanged();
        mGetarrayAdapter.notifyDataSetChanged();
        ListViewHeightMesure.setAdapterHeight(mLvallDevices);
        ListViewHeightMesure.setAdapterHeight(mLvPairedDevices);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Comment.SWITCH:
                    switch ((int) msg.obj) {
                        case BluetoothAdapter.STATE_OFF:
                            nonebound_tv.setText("可用设备(" + DatasEntity.mBluetoothDevices.size() + ")");
                            btnSearch.setChecked(false);
                            clearAdapter();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            reswipeAdapter();
                            break;
                    }
                    break;
                case Comment.FOUND:
                    DatasEntity.mBluetoothDevices.add((BluetoothDevice) msg.obj); // 添加到列表
                    nonebound_tv.setText("可用设备(" + DatasEntity.mBluetoothDevices.size() + ")");
                    mGetarrayAdapter.notifyDataSetChanged();
                    ListViewHeightMesure.setAdapterHeight(mLvallDevices);
                    break;

                case Comment.FINISHED:
                    setTitle("玩蓝牙");
                    rotate_img.clearAnimation();
                    break;

                case Comment.BOND:
                    switch ((int) msg.obj) {
                        case BluetoothDevice.BOND_BONDING://正在配对
                            DialogUtil.ShowProgress(MainActivity.this, "配对中...");
                            break;
                        case BluetoothDevice.BOND_BONDED://配对结束
                            DialogUtil.CancelProgress();
                            clearAdapter();
                            reswipeAdapter();
                            ToastUtil.showShort(MainActivity.this,"连接中...");
                            BluetoothUtil.connectSocket(mHandler);
                            break;
                        case BluetoothDevice.BOND_NONE://取消配对/未配对
                            ToastUtil.showShort(MainActivity.this, "已取消配对");
                            break;
                    }
                    break;
                case Comment.CONNECT:
                    ToastUtil.showShort(MainActivity.this,"连接成功！");
                    break;

            }
        }
    };

}
