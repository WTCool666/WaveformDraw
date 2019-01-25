package com.example.waveform;

import android.content.DialogInterface;
import android.media.AudioFormat;
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
import com.example.waveform.utils.MicManager;
import com.example.waveform.utils.NormalDialog;
import com.example.waveform.utils.TitleView;

public class SingleChannelActivity extends AppCompatActivity implements View.OnClickListener {

    private BaseAudioSurfaceView bsv_singleChannel;
    private ImageView iv_singleChannel;
    private MicManager manager=null;
    private TitleView title_view;
    private NormalDialog rePlayDialog;
    private Button btn_singleChannel;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MicManager.MODE_RECORD_FILE:
                    bsv_singleChannel.addAudioData((byte[])msg.obj, msg.arg1, Constant.SINGLE_CHANNEL_SAMPLEER_RATE, Constant.SINGLE_CHANNLE_BIT_WIDTH, false);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.single_channel_layout);
        initView();
        manager=new MicManager(Constant.SINGLE_FILE);
        manager.setHandler(handler);
        manager.setAudioParameters(Constant.SINGLE_CHANNEL_SAMPLEER_RATE,Constant.SINGLE_CAHNNEL_FORMAT ,Constant.SINGLE_CHANNEL_CONFIG );
    }

    private void initView(){
        btn_singleChannel=(Button)findViewById(R.id.btn_singleChannel);
        btn_singleChannel.setOnClickListener(this);
        title_view=(TitleView) findViewById(R.id.title_view);
        title_view.setTitleText("单声道波形绘制");
        title_view.setBackOnClickListener(this);
        bsv_singleChannel=(BaseAudioSurfaceView)findViewById(R.id.bsv_singleChannel);
        iv_singleChannel=(ImageView) findViewById(R.id.iv_singleChannel);
        iv_singleChannel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bsv_singleChannel:
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
                                bsv_singleChannel.reDrawThread();
                                btn_singleChannel.setVisibility(View.GONE);
                                iv_singleChannel.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
                                manager.startRecord(MicManager.MODE_RECORD_FILE);
                                break;
                        }
                    }
                });
                break;
            case R.id.iv_back:
                manager.destroy();
                bsv_singleChannel.stopDrawThread();
                this.finish();
                break;
            case R.id.iv_singleChannel:
                if (manager.getOperationStatus()==MicManager.MODE_RECORD_FILE){
                    manager.stopRecord();
                    btn_singleChannel.setVisibility(View.VISIBLE);
                    iv_singleChannel.setImageDrawable(getResources().getDrawable(R.drawable.start_play));
                }else{
                    btn_singleChannel.setVisibility(View.GONE);
                    manager.startRecord(MicManager.MODE_RECORD_FILE);
                    iv_singleChannel.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.destroy();
        bsv_singleChannel.stopDrawThread();
    }
}
