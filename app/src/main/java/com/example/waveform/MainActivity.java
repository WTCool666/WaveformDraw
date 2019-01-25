package com.example.waveform;

import android.content.Intent;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import com.example.waveform.utils.BaseAudioSurfaceView;
import com.example.waveform.utils.BasePermissionActivity;

public class MainActivity extends BasePermissionActivity implements View.OnClickListener {

    private Button btn_singleChannel,btn_singleFile,btn_doubleChannel,btn_doubleFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        btn_singleChannel=(Button) findViewById(R.id.btn_singleChannel);
        btn_singleFile=(Button)findViewById(R.id.btn_singleFile);
        btn_doubleChannel=(Button) findViewById(R.id.btn_doubleChannel);
        btn_doubleFile=(Button)findViewById(R.id.btn_doubleFile);
        btn_singleChannel.setOnClickListener(this);
        btn_singleFile.setOnClickListener(this);
        btn_doubleChannel.setOnClickListener(this);
        btn_doubleFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        switch (v.getId()){
            case R.id.btn_singleChannel:
                intent.setClass(this, SingleChannelActivity.class);
                break;
            case R.id.btn_singleFile:
                intent.setClass(this, SingleFileActivity.class);
                break;
            case R.id.btn_doubleChannel:
                intent.setClass(this, DoubleChannelActivity.class);
                break;
            case R.id.btn_doubleFile:
                intent.setClass(this, DoubleFileActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
