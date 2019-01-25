package com.example.waveform;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.waveform.utils.BaseAudioSurfaceView;
import com.example.waveform.utils.Constant;
import com.example.waveform.utils.FileUtils;
import com.example.waveform.utils.MicManager;
import com.example.waveform.utils.NormalDialog;
import com.example.waveform.utils.TitleView;

public class DoubleChannelActivity extends AppCompatActivity implements View.OnClickListener {

    private BaseAudioSurfaceView bsv_doubleChannel,bsv_doubleChannel2;
    private ImageView iv_doubleChannel;
    private MicManager manager=null;
    private TitleView title_view;
    private Button btn_doubleChannel;
    private NormalDialog rePlayDialog;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MicManager.MODE_RECORD_FILE:
                    bsv_doubleChannel.addAudioData((byte[])msg.obj, msg.arg1, Constant.DOUBLE_CHANNEL_SAMPLEER_RATE, Constant.DOUBLE_CHANNLE_BIT_WIDTH, false);
                    bsv_doubleChannel2.addAudioData((byte[])msg.obj, msg.arg1, Constant.DOUBLE_CHANNEL_SAMPLEER_RATE, Constant.DOUBLE_CHANNLE_BIT_WIDTH, true);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.double_channel_layout);
        initView();
        manager=new MicManager(Constant.DOUBLE_FILE);
        manager.setHandler(handler);
        manager.setAudioParameters(Constant.DOUBLE_CHANNEL_SAMPLEER_RATE,Constant.DOUBLE_CAHNNEL_FORMAT,Constant.DOUBLE_CHANNEL_CONFIG );
    }

    private void initView(){
        btn_doubleChannel=(Button)findViewById(R.id.btn_doubleChannel);
        btn_doubleChannel.setOnClickListener(this);
        title_view=(TitleView) findViewById(R.id.title_view);
        title_view.setTitleText("双声道波形绘制");
        title_view.setBackOnClickListener(this);
        bsv_doubleChannel=(BaseAudioSurfaceView)findViewById(R.id.bsv_doubleChannel);
        bsv_doubleChannel2=(BaseAudioSurfaceView)findViewById(R.id.bsv_doubleChannel2);
        iv_doubleChannel=(ImageView) findViewById(R.id.iv_doubleChannel);
        iv_doubleChannel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_doubleChannel:
                if (rePlayDialog==null){
                    rePlayDialog=new NormalDialog();
                }
                rePlayDialog.show(getSupportFragmentManager(), "", "确认真的要重新录制吗？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_NEGATIVE:
                                rePlayDialog.dismiss();
                                break;
                            case DialogInterface.BUTTON_POSITIVE:
                                manager.destroy();
                                bsv_doubleChannel.reDrawThread();
                                bsv_doubleChannel2.reDrawThread();
                                btn_doubleChannel.setVisibility(View.GONE);
                                iv_doubleChannel.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
                                manager.startRecord(MicManager.MODE_RECORD_FILE);
                                break;
                        }
                    }
                });
                break;
            case R.id.iv_back:
                manager.destroy();
                bsv_doubleChannel.stopDrawThread();
                bsv_doubleChannel2.stopDrawsThread();
                this.finish();
                break;
            case R.id.iv_doubleChannel:
                if (manager.getOperationStatus()==MicManager.MODE_RECORD_FILE){
                    manager.stopRecord();
                    btn_doubleChannel.setVisibility(View.VISIBLE);
                    iv_doubleChannel.setImageDrawable(getResources().getDrawable(R.drawable.start_play));
                }else{
                    btn_doubleChannel.setVisibility(View.GONE);
                    manager.startRecord(MicManager.MODE_RECORD_FILE);
                    iv_doubleChannel.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.destroy();
        bsv_doubleChannel.stopDrawThread();
        bsv_doubleChannel2.stopDrawsThread();
    }
}
