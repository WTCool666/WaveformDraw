package com.example.waveform.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    private static final String TAG="FileUtils ";

    public static final int PLAYING=401;
    public static final int PLAY_STOP=402;
    public static final int GET_WAVEFORM_DATA_ING=403;
    public static final int GET_WAVEFORM_DATA_STOP=404;


    public static  final String FILE_LENGTH= "FILE_LENGTH";
    private static class GetWavDataAndPlayRunnable implements  Runnable{
        private ThreadPoolUtils threadPoolUtils;
        private Handler mHandler;

        public void setThreadUtils(ThreadPoolUtils threadPoolUtils){
            this.threadPoolUtils=threadPoolUtils;
        }
        public void setHandler(Handler mHandler){
            this.mHandler=mHandler;
        }

        private String fileName;
        private MicManager mRecordManager;
        private boolean running=true;
        private int fileLength;
        private int skipSize=0;
        GetWavDataAndPlayRunnable(String fileName,int sampleRateInHz,int audioFormat,int channelConfig_out,int skipSize){
            this.skipSize=skipSize;
            this.fileName=fileName;
            mRecordManager=new MicManager(fileName);
            mRecordManager.setAudioParameters(sampleRateInHz, audioFormat, channelConfig_out);
        }

        public void stop(){
            running=false;
        }

        @Override
        public void run() {
            File file = new File(fileName);
            if (file == null) {
                if (mRecordManager!=null){
                    mRecordManager.destroy();
                }
                if (threadPoolUtils!=null){
                    threadPoolUtils.shutdown();
                }
                LogUtils.v(TAG + "GetWavDataAndPlayRunnable file is null");
                return ;
            }
            InputStream instream = null;
            BufferedInputStream bis = null;

            try {
                instream = new FileInputStream(file);
                if (instream != null) {
                    bis = new BufferedInputStream(instream);
                    bis.skip(skipSize);
                    byte[] buffer = new byte[640];
                    fileLength=bis.available();
                    while(running){
                        int size=bis.read(buffer);
                        if (size==-1){
                            break;
                        }
                        int samplerCount = getIndexTimesCount(buffer,16,size);
                        Message message=new Message();
                        message.what=PLAYING;
                        Bundle bundle=new Bundle();
                        bundle.putInt(FILE_LENGTH, fileLength);
                        message.setData(bundle);
                        message.arg1=size;
                        message.arg2=samplerCount;
                        mHandler.sendMessage(message);
                        if (mRecordManager!=null){
                            mRecordManager.playAudioTrack(buffer, size);
                        }
                    }
                }
            } catch (IOException e) {
                LogUtils.v(TAG + e.getMessage());
            } finally {
                try {
                    if (bis != null)
                        bis.close();
                    if (instream != null)
                        instream.close();
                } catch (IOException e) {
                    LogUtils.v(TAG + e.getMessage());
                }
            }

            if (running){
                if (mHandler!=null){
                    Message message=new Message();
                    message.what=PLAY_STOP;
                    mHandler.sendMessage(message);
                }
            }
            if (mRecordManager!=null){
                mRecordManager.destroy();
            }
            if (threadPoolUtils!=null){
                threadPoolUtils.shutdown();
            }
        }
    }

    //读取wav数据并且播放出来
    private static GetWavDataAndPlayRunnable getWavDataAndPlayRunnable=null;
    public static void getWavDataAndPlay(String fileName,Handler handler,int sampleRateInHz,int audioFormat,int channelConfig_out,int skipSize) {
        stopPalyWav();
        ThreadPoolUtils threadPoolUtils=new ThreadPoolUtils();
        getWavDataAndPlayRunnable=new GetWavDataAndPlayRunnable( fileName,sampleRateInHz,audioFormat,channelConfig_out,skipSize);
        getWavDataAndPlayRunnable.setThreadUtils(threadPoolUtils);
        getWavDataAndPlayRunnable.setHandler(handler);
        threadPoolUtils.execute(getWavDataAndPlayRunnable);
    }

    public static void stopPalyWav(){
        if (getWavDataAndPlayRunnable!=null){
            getWavDataAndPlayRunnable.stop();
            getWavDataAndPlayRunnable=null;
        }
    }

    private static class GetWaveFormDataRunnable implements  Runnable{
        private ThreadPoolUtils threadPoolUtils;
        private Handler mHandler;

        public void setThreadUtils(ThreadPoolUtils threadPoolUtils){
            this.threadPoolUtils=threadPoolUtils;
        }
        public void setHandler(Handler mHandler){
            this.mHandler=mHandler;
        }

        private String fileName;
        private boolean running=true;
        private int [] allData=null;
        private int needSamplerCount;
        private int tempSize=0;
        private int bitWidth=16;
        GetWaveFormDataRunnable(String fileName,int bitWidth){
            this.fileName=fileName;
            this.bitWidth=bitWidth;
        }

        public void stop(){
            running=false;
            tempSize=0;
        }

        @Override
        public void run() {
            File file = new File(fileName);
            if (file == null) {
                if (threadPoolUtils!=null){
                    threadPoolUtils.shutdown();
                }
                LogUtils.v(TAG + "GetWaveFormDataRunnable file is null");
                return ;
            }
            InputStream instream = null;
            BufferedInputStream bis = null;
            try {
                instream = new FileInputStream(file);
                if (instream != null) {
                    bis = new BufferedInputStream(instream);
                    byte[] buffer = new byte[640];
                    needSamplerCount= (bis.available())/(bitWidth/8)/Constant.INDEX_TIMES;
                    allData=new int[needSamplerCount];
                    while(running){
                        int size=bis.read(buffer);
                        if (size==-1){
                            break;
                        }
                        int[] tempBuf = getIndexTimesWaveformData(buffer,bitWidth,size);
                        if (tempBuf==null){
                            continue;
                        }
                        int len=tempBuf.length;
                        if (tempSize+ tempBuf.length>=needSamplerCount){
                            len=needSamplerCount-tempSize;
                        }
                        System.arraycopy(tempBuf,0 , allData, tempSize,len);
                        tempSize+=tempBuf.length;
                        Message message=new Message();
                        message.what=GET_WAVEFORM_DATA_ING;
                        message.obj=tempBuf;
                        message.arg1=tempBuf.length;
                        mHandler.sendMessage(message);
                    }
                }
            } catch (IOException e) {
                LogUtils.v(TAG + e.getMessage());
            } finally {
                try {
                    if (bis != null)
                        bis.close();
                    if (instream != null)
                        instream.close();
                } catch (IOException e) {
                    LogUtils.v(TAG + e.getMessage());
                }
            }
            if (running){
                if (mHandler!=null){
                    Message message=new Message();
                    message.what=GET_WAVEFORM_DATA_STOP;
                    message.obj=allData;
                    message.arg1=needSamplerCount;
                    mHandler.sendMessage(message);
                }
            }
            if (threadPoolUtils!=null){
                threadPoolUtils.shutdown();
            }
        }
    }

    private static GetWaveFormDataRunnable mGetWaveFormDataRunnable=null;
    public static void getWaveformData(String fileName,Handler handler,int bitWidth) {
        stopGetWaveform();
        ThreadPoolUtils threadPoolUtils=new ThreadPoolUtils();
        mGetWaveFormDataRunnable=new GetWaveFormDataRunnable(fileName,bitWidth);
        mGetWaveFormDataRunnable.setThreadUtils(threadPoolUtils);
        mGetWaveFormDataRunnable.setHandler(handler);
        threadPoolUtils.execute(mGetWaveFormDataRunnable);
    }

    public static void stopGetWaveform(){
        if (mGetWaveFormDataRunnable!=null){
            mGetWaveFormDataRunnable.stop();
            mGetWaveFormDataRunnable=null;
        }
    }

    private static byte[] tempWaveformData=new byte[1024];
    private static  int tempCount=0;
    private static int[] getIndexTimesWaveformData(byte[] data,int bitWidth,int size){
        if(data==null){
            return null;
        }
        if(data.length==0){
            return null;
        }
        if(bitWidth!=16 && bitWidth!=8 && bitWidth!=24 && bitWidth!=32){
            LogUtils.v(TAG+"getIndexTimesWaveformData bitWidth is unknow type");
            return null;
        }
        int byteCountPreSampler=bitWidth/8;
        int count=size+tempCount;
//        byte[] tempDatas=new byte[count];
        //先复制剩余的字节数
//        System.arraycopy(tempWaveformData, 0, tempDatas, 0,tempCount);
//        System.arraycopy(data, 0, tempDatas, tempCount,size);
        System.arraycopy(data, 0, tempWaveformData, tempCount,size);
        int samplerCount = (count/byteCountPreSampler);
        //需要的采样点的个数
        int tempSize=samplerCount/Constant.INDEX_TIMES;
        int[] tempSampler= new int[tempSize];
        int tempData =0;
        int j=0;
        for(int i=0;i<tempSize;i++){
            tempData=0;
            for(int k=0;k<byteCountPreSampler;k++) {
                int tempBuf = 0;
                if ((j + k) < count) {
//                    tempBuf = (tempWaveformData[j + k] << (k * 8));
                    tempBuf = (tempWaveformData[j + k] << (k * 8));
                }
                tempData = (tempData | tempBuf);
//                LogUtils.v("=tempBuf="+tempBuf+" k="+k+" tempData="+tempData+" i="+i);
            }

            tempSampler[i]=tempData;
            j+=(byteCountPreSampler*Constant.INDEX_TIMES);
        }
        //剩余的字节数
        tempCount = count%(Constant.INDEX_TIMES*byteCountPreSampler);
//        System.arraycopy(tempDatas, count-tempCount, tempWaveformData, 0,tempCount);
        System.arraycopy(tempWaveformData, count-tempCount, tempWaveformData, 0,tempCount);
        return tempSampler;
    }

    private static int getIndexTimesCount(byte[] data,int bitWidth,int size){
        if(data==null){
            return -1;
        }
        if(data.length==0){
            return -1;
        }
        if(bitWidth!=16 && bitWidth!=8 && bitWidth!=24 && bitWidth!=32){
            LogUtils.v(TAG+"getIndexTimesCount bitWidth is unknow type");
            return -1;
        }
        int byteCountPreSampler=bitWidth/8;
        int count=size+tempCount;
        int samplerCount = (count/byteCountPreSampler);
        //需要的采样点的个数
        int tempSize=samplerCount/Constant.INDEX_TIMES;
        //剩余的字节数
        tempCount = count%(Constant.INDEX_TIMES*byteCountPreSampler);
        return tempSize;
    }


}
