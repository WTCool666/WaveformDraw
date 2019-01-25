package com.example.waveform;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
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
import com.example.waveform.utils.NormalDialog;
import com.example.waveform.utils.TitleView;

import java.io.File;

public class SingleFileActivity extends AppCompatActivity implements View.OnClickListener {

    private BaseAudioSurfaceView bsv_singleFile;
    private ImageView iv_singleFile;
    private File file;
    private Button btn_singleFile;
    private NormalDialog rePlayDialog;
    private TitleView title_view;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case FileUtils.GET_WAVEFORM_DATA_ING:
                    break;
                case FileUtils.GET_WAVEFORM_DATA_STOP:
                    bsv_singleFile.addAudioDatas((int[]) msg.obj,msg.arg1 , Constant.SINGLE_CHANNEL_SAMPLEER_RATE, Constant.SINGLE_CHANNLE_BIT_WIDTH, false);
                    break;
                case FileUtils.PLAY_STOP:
                    status=PALYED;
                    tempCount=0;
                    iv_singleFile.setImageDrawable(getResources().getDrawable(R.drawable.start_play));
                    bsv_singleFile.reDraws();
                    break;
                case FileUtils.PLAYING:
                    Bundle playBundle = msg.getData();
                    if (playBundle!=null){
                        int size = msg.arg1;
                        if (size>0){
                            if (bsv_singleFile!=null){
                                tempCount+=size;
                                bsv_singleFile.startHasReadAudioDataThread(msg.arg2);
                            }
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.single_file_layout);
        initView();
        file= new File(Environment.getExternalStorageDirectory(),Constant.SINGLE_FILE);
        FileUtils.getWaveformData(file.getAbsolutePath(),handler , Constant.SINGLE_CHANNLE_BIT_WIDTH);
    }

    private void initView(){
        btn_singleFile=(Button) findViewById(R.id.btn_singleFile);
        title_view=findViewById(R.id.title_view);
        title_view.setBackOnClickListener(this);
        title_view.setTitleText("单声道文件波形绘制");
        btn_singleFile.setOnClickListener(this);
        bsv_singleFile=(BaseAudioSurfaceView)findViewById(R.id.bsv_singleFile);
        iv_singleFile=(ImageView) findViewById(R.id.iv_singleFile);
        iv_singleFile.setOnClickListener(this);
    }

    private final int PLAYING=0;
    private final int PALYED=1;
    private int status=PALYED;
    private int tempCount=0;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                tempCount=0;
                FileUtils.stopGetWaveform();
                FileUtils.stopPalyWav();
                bsv_singleFile.stopDrawsThread();
                this.finish();
                break;
            case R.id.iv_singleFile:
                if (status==PLAYING){
                    FileUtils.stopPalyWav();
                    status=PALYED;
                    btn_singleFile.setVisibility(View.VISIBLE);
                    iv_singleFile.setImageDrawable(getResources().getDrawable(R.drawable.start_play));
                }else if(status==PALYED){
                    status=PLAYING;
                    btn_singleFile.setVisibility(View.GONE);
                    iv_singleFile.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
                    FileUtils.getWavDataAndPlay(file.getAbsolutePath(), handler,Constant.SINGLE_CHANNEL_SAMPLEER_RATE , Constant.SINGLE_CAHNNEL_FORMAT, Constant.SINGLE_CHANNEL_CONFIG, tempCount);
                }
                break;
            case R.id.btn_singleFile:
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
                                bsv_singleFile.reDraws();
                                status=PLAYING;
                                tempCount=0;
                                FileUtils.getWavDataAndPlay(file.getAbsolutePath(), handler,Constant.SINGLE_CHANNEL_SAMPLEER_RATE , Constant.SINGLE_CAHNNEL_FORMAT, Constant.SINGLE_CHANNEL_CONFIG, tempCount);
                                btn_singleFile.setVisibility(View.GONE);
                                iv_singleFile.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
                                break;
                        }
                    }
                });

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tempCount=0;
        FileUtils.stopGetWaveform();
        FileUtils.stopPalyWav();
        bsv_singleFile.stopDrawsThread();
    }
}
