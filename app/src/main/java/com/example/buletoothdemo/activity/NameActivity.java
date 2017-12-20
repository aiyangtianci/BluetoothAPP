package com.example.buletoothdemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.buletoothdemo.R;
import com.example.buletoothdemo.util.BluetoothUtil;

/**
 * Created by 修改蓝牙名 on 2017/9/5.
 */

public class NameActivity extends AppCompatActivity{

    EditText editText;

    String mDevicename="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_name);

        editText = (EditText) findViewById(R.id.name);
        if (getIntent()!=null){
            editText.setText(mDevicename=getIntent().getStringExtra("name"));
        }



        Button button = (Button) findViewById(R.id.ok);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothUtil.mBluetoothAdapter().setName(mDevicename);
                finish();
            }
        });
    }
}
