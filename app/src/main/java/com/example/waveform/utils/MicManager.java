package com.example.waveform.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MicManager {

	private final String TAG="MicManager";
//	private final String filename = "audioRecord.pcm";
//	private static MicManager instance = null;
	private AudioRecord mAudioRecorder = null;
	private AudioTrack mAudioTrack = null;
	private int sampleRateInHz = 16000;
	private int channelConfig_in = AudioFormat.CHANNEL_IN_MONO;
	private int channelConfig_out = AudioFormat.CHANNEL_OUT_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	private final int streamType = AudioManager.STREAM_MUSIC;
	private final int trackMode = AudioTrack.MODE_STREAM;
	private final int audioSource = MediaRecorder.AudioSource.DEFAULT;
	private int recordMinBufSize = 0;
	private int trackMinBufSize = 0;
	private byte[] recordBuf = null;
	private byte[] trackBuf = null;
	private File file = null;
	private int status = -1;
	public static final int MODE_OUT_MIC = 1;
	public static final int MODE_RECORD_FILE = 2;
	public static final int MODE_DEFAULT = 0;
	public static final int MODE_PLAY_FILE = 3;
	private FileOutputStream fout;
	private RecordRunnable recordRunnable = null;
	private PlayRunnable playRunnable = null;

	public MicManager(String filename) {
		file = new File(Environment.getExternalStorageDirectory(), filename);
	}

	private void initRecord() {
		recordMinBufSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig_in, audioFormat);
		LogUtils.i(TAG+"recordMinBufSize="+recordMinBufSize);
		if (recordMinBufSize == AudioRecord.ERROR || AudioRecord.ERROR_BAD_VALUE == recordMinBufSize) {
			LogUtils.e(TAG+"AudioRecord.getMinBufferSize failed!");
			return;
		}
		mAudioRecorder = new AudioRecord(audioSource, sampleRateInHz, channelConfig_in, audioFormat, recordMinBufSize);
		if(recordBuf==null){
			recordBuf=new byte[recordMinBufSize];
		}
	}

	private Handler handler;
	public void setHandler(Handler handler){
		this.handler=handler;
	}

	private void sendMessage(byte[] data,int size){
		Message message=new Message();
		message.obj=data;
		message.arg1=size;
		message.what=status;
		handler.sendMessage(message);
	}

	public void setAudioParameters(int sampleRateInHz,int bitWidth,int channel){
		this.sampleRateInHz=sampleRateInHz;
		this.audioFormat=bitWidth;
		this.channelConfig_in=channel;
		if (channelConfig_in==AudioFormat.CHANNEL_IN_STEREO){
			channelConfig_out=AudioFormat.CHANNEL_OUT_STEREO;
		}
	}

	private void initTrack() {
		trackMinBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig_out, audioFormat);
		if (trackMinBufSize == AudioRecord.ERROR || AudioRecord.ERROR_BAD_VALUE == trackMinBufSize) {
			LogUtils.e(TAG+"AudioTrack.getMinBufferSize failed!");
			return;
		}
		mAudioTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig_out, audioFormat, trackMinBufSize,
				trackMode);
		if (trackBuf == null) {
			trackBuf = new byte[trackMinBufSize * 2];
		}
	}

	public int getOperationStatus() {
		return this.status;
	}

	/*public static MicManager getInstance() {
		if (instance == null) {
			synchronized (MicManager.class) {
				if (instance == null) {
					instance = new MicManager();
				}
			}
		}
		return instance;
	}*/

	public void playAudioTrack(byte[] data, int size) {
		if (mAudioTrack == null) {
			initTrack();
		}
		synchronized (mAudioTrack) {
			try {
				mAudioTrack.write(data, 0, size);
				if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
					mAudioTrack.play();
				}
			} catch (IllegalStateException e) {
			}
		}
	}

	private void writeDataFile(byte[] data, int size) {
		/*if (file.exists()) {
			file.delete();
		}else {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		if (fout == null) {
			try {
				fout = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		try {
			fout.write(data, 0, size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public void startRecord(int mode) {
		status=mode;
		if (mAudioRecorder==null){
			initRecord();
		}
		if (playRunnable != null) {
			playRunnable.stop();
			playRunnable = null;
		}
		if (recordRunnable != null) {
			recordRunnable.stop();
			recordRunnable = null;
		}
		recordRunnable = new RecordRunnable();
		new Thread(recordRunnable).start();
	}

	public void stopRecord() {
		if (recordRunnable != null) {
			recordRunnable.stop();
			recordRunnable = null;
		}

		if (fout != null) {
			try {
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fout = null;
		}
		status=MODE_DEFAULT;
	}

	public void playRecordFile() {
		status=MODE_PLAY_FILE;
		initTrack();
		if (playRunnable != null) {
			playRunnable.stop();
			playRunnable = null;
		}
		if (recordRunnable != null) {
			recordRunnable.stop();
			recordRunnable = null;
		}
		playRunnable = new PlayRunnable();
		new Thread(playRunnable).start();
	}

	public void stopPlay() {
		if (playRunnable != null) {
			playRunnable.stop();
			playRunnable = null;
		}
		status=MODE_DEFAULT;
	}

	private class RecordRunnable implements Runnable {
		private boolean running = true;

		public void stop() {
			running = false;
		}

		@Override
		public void run() {
			if (mAudioRecorder == null) {
				LogUtils.i(TAG+"mAudioRecorder is null");
				return;
			}
			int readResult = 0;
			mAudioRecorder.startRecording();
			while (running) {
				readResult = mAudioRecorder.read(recordBuf, 0, recordMinBufSize);
				if (readResult < 0) {
					continue;
				}
				sendMessage(recordBuf,readResult);
				for(int i=0;i<readResult;i++){
//					LogUtils.v("recordBuf="+recordBuf[i]);
				}
				switch (status) {
				case MODE_OUT_MIC:
					playAudioTrack(recordBuf, readResult);
					break;
				case MODE_RECORD_FILE:
					writeDataFile(recordBuf, readResult);
					break;
				default:
					break;
				}
			}
			if (mAudioRecorder != null) {
				try {
					mAudioRecorder.stop();
					mAudioRecorder.release();
					mAudioRecorder = null;
				} catch (IllegalStateException e) {
				}
			}
		}
	}

	private class PlayRunnable implements Runnable {

		private boolean running = true;
		private FileInputStream in = null;

		public void stop() {
			running = false;
		}

		public void run() {
			try {
				in = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (in == null || mAudioTrack == null) {
				LogUtils.i(TAG+"play Thread exited");
				return;
			}
			int size = 0;
			byte[] buffer = new byte[240];
			while (running) {
				try {
					size = in.read(buffer);
					if (size <= 0) {
						break;
					}
					playAudioTrack(buffer, size);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(mAudioTrack != null){
				mAudioTrack.stop();
				mAudioTrack.release();
				mAudioTrack = null;
			}
		}
	}

	public void destroy() {
		if (recordRunnable != null) {
			recordRunnable.stop();
			recordRunnable = null;
		}
		if (fout != null) {
			try {
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fout = null;
		}
		if (mAudioRecorder != null) {
			try {
				mAudioRecorder.stop();
				mAudioRecorder.release();
			} catch (IllegalStateException e) {
			}
		}
		if (playRunnable != null) {
			playRunnable.stop();
			playRunnable = null;
		}
		if (mAudioTrack != null) {
			try {
				mAudioTrack.stop();
				mAudioTrack.release();
			} catch (IllegalStateException e) {
			}
		}
	}
}
