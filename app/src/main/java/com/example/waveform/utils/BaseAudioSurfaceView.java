package com.example.waveform.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.waveform.R;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * bug:
 * 1 手机息屏后，画面无法恢复
 * 2 重新播放时，如果文件将大，可能还未重新绘制完成，就需要播放，这个时候有冲突。
 *
 */
public class BaseAudioSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

	private final String TAG="BaseAudioSurfaceView ";

	private Thread drawThread=null;
	private Thread drawsThread=null;
	private long previousTime=0;
	//两次绘图间隔的时间
	private final int DRAW_TIME = 5;//1000 / 200;
	//距离右边的距离,根据自己喜好，也可以为0.
	private final int MARGIN_RIGHT=30;

	private SurfaceHolder holder;
	//上下圆的画笔
	private Paint circlePaint;
	//波形线的画笔
	private Paint waveformPaint;
	private Paint textPaint;
	private String mShowText="L";
	//上下基线使用的高度
	private final int MARKER_LINE=40;
	//标识圆的半径
	private final int RADIOUS=MARKER_LINE/4;
	//基线与标识圆的x值
//	private int markerX=0;
	//绘制最大的宽度
	private int MAX_WIDHT=getWidth()-MARGIN_RIGHT;
	//绘制最大的高度
	private int MAX_HEIGHT=getHeight()-MARKER_LINE;
	private boolean isRunning=true;
	//获取采样点的index为100的倍数的采样点
//	private int INDEX_TIMES =100;
	//采样率,默认16k
	private int samplerRate=16000;
	//采样位宽,默认16bit
	private int bitWidth=16;
	private final int SECOND_PRESCREEN=20;
	//声道数，默认单声道
//	private final int channelCount=1;
	//每个屏幕绘制多少个采样点
	private float drawSamplerCountPreScreen = (float)(samplerRate/Constant.INDEX_TIMES*SECOND_PRESCREEN);
	//每隔多远描绘一次，默认一个屏幕只描绘20s*16000/100次
	private float divider = (float) (MAX_WIDHT/drawSamplerCountPreScreen);
	//y轴缩放的倍数,默认16bit所以是2个字节
	private float yAxisTimes = (float)(Short.MAX_VALUE/ MAX_HEIGHT);
	private boolean  rightChannel=false;
	private boolean isFullScreen=false;


	private class DrawHasReadAudioData implements  Runnable{

		private boolean isRunning=true;

       private Object lock=new Object();
		private void stopDraw(){
			isRunning=false;
		}

		private int hasReadAudioDataSize=0;
		private float hasReadAudioDataWidth=0;
		private int tempCount=0;
		private boolean tempFlag=true;
		public void setHasReadAudioDataSize(int size) {
			synchronized (lock){
				//LogUtils.v("setHasReadAudioDataSize getId()="+Thread.currentThread().getId());
				tempCount+=size;
//				LogUtils.v("tempCount="+tempCount+" size="+inBuf.size());
				if (tempCount>inBuf.size()){
					tempCount=inBuf.size();
				}
				lock.notify();
			}
		}

		public void reDraws(){
			synchronized (lock){
				hasReadAudioDataWidth=0;
				hasReadAudioDataSize=0;
				tempCount=0;
				LogUtils.v("reDraws");
				setWaveformPaintColor(R.color.waveformBorderLine);
				startDrawsThread();
				tempFlag=true;
			}

		}

		@Override
		public  void run() {
			while(isRunning){
				synchronized (lock){
					if (!holder.getSurface().isValid() || BaseAudioSurfaceView.this.isRunning){
						continue;
					}
					if (tempCount!=0){
						if (!isFullScreen){
							if(tempFlag){
								LogUtils.v(TAG+"draw backGround");
								for (int i=0;i<3;i++){
									tempFlag=false;
									BaseAudioSurfaceView.this.isRunning=true;
									drawWaveForm(inBuf);
									BaseAudioSurfaceView.this.isRunning=false;
								}
							}
						}
					}else{
						continue;
					}
					setWaveformPaintColor(R.color.waveformCenterLine);
//		int tempSize =tempCount/(bitWidth/8)/INDEX_TIMES;
//		Canvas canvas= holder.lockCanvas(null);

					Canvas canvas = holder.lockCanvas(
							new Rect((int)hasReadAudioDataWidth,0 , (int) ((tempCount-1) * divider)+1, getHeight()));// 关键:获取画布
//					initBaseView(canvas);
					///LogUtils.v("hasReadAudioDataWidth="+(int)hasReadAudioDataWidth+" "+hasReadAudioDataWidth+" tempCount * divider="+(int) ((tempCount-1) * divider)+" "+(tempCount-1) * divider+" tempCount="+tempCount);
//		canvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);// 清除画布
					if(canvas==null){
						LogUtils.v(TAG+"setHasReadAudioDataSize canvas is null");
						return;
					}
					hasReadAudioDataWidth =(tempCount) * divider;
					int tempHasReadAudioDataWidth= (int) hasReadAudioDataWidth;
//		LogUtils.v("left="+holder.getSurfaceFrame().left+" right"+holder.getSurfaceFrame().right);
					int upMinHeight=MARKER_LINE/2;
					int underMaxHeight=getHeight()-upMinHeight;
					int i =hasReadAudioDataSize;
//					LogUtils.v("i="+i);
					for ( ; i < tempCount ; i++) {
						float y=inBuf.get(i)/yAxisTimes + getHeight()/2;// 调节缩小比例，调节基准线

						float x=(i) * divider;
						if (x>=tempHasReadAudioDataWidth){
							hasReadAudioDataSize=i;
							if (isFullScreen){
								break;
							}
						}else{
							hasReadAudioDataSize=tempCount;
						}

						if(x >= MAX_WIDHT){
							x = MAX_WIDHT;
						}
						if(y<=upMinHeight){
							y = upMinHeight;
						}
						if(y>underMaxHeight){
							y = underMaxHeight;
						}
						float y1 = getHeight() - y;

						if(y1<=upMinHeight){
							y1 = upMinHeight;
						}
						if(y1>underMaxHeight){
							y1 = underMaxHeight;
						}
//						LogUtils.v("====i="+i+" x="+x+" y="+y+" y1="+y1);
						canvas.drawLine(x, y,  x,y1, waveformPaint);//中间出波形
//			canvas.drawLine(x, 20,  x,380, waveformPaint);
					}

//		LogUtils.v("hasReadAudioDataWidth "+hasReadAudioDataWidth);
					holder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
                   /* if (i==inBuf.size()){
                    	LogUtils.v("+++++++++++++++"+i);
                        setWaveformPaintColor(R.color.waveformBorderLine);

                        BaseAudioSurfaceView.this.reDraws();
                    }*/
					try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
				}
			}

		}
	}

	private Thread hasReadAudioDataThread;
	private DrawHasReadAudioData mDrawHasReadAudioData;
	public void startHasReadAudioDataThread(int size){
		if (hasReadAudioDataThread==null){
			mDrawHasReadAudioData=new DrawHasReadAudioData();
			mDrawHasReadAudioData.setHasReadAudioDataSize(size);
			hasReadAudioDataThread=new Thread(mDrawHasReadAudioData);
			hasReadAudioDataThread.start();
		}
		if (mDrawHasReadAudioData!=null){
			mDrawHasReadAudioData.setHasReadAudioDataSize(size);
		}
	}

	public void reDraws(){
		if (mDrawHasReadAudioData!=null){
			mDrawHasReadAudioData.reDraws();
		}
	}

	/**
	 *在开始绘制之前，需要调用这个函数设置音频参数，用来初始化绘制的一些参数。
	 * */
	private void setAudioParam(int samplerRate,int bitWidth,boolean  rightChannel){
		this.rightChannel=rightChannel;
		this.samplerRate=samplerRate;
		this.bitWidth=bitWidth;
//		LogUtils.v("width="+width+" getWidth()="+getWidth()+" height="+height+" getHeight()="+getHeight());
		MAX_WIDHT=getWidth()-MARGIN_RIGHT;
		MAX_HEIGHT=getHeight()-MARKER_LINE;
//        LogUtils.v("1 divider="+divider);
		drawSamplerCountPreScreen = (float) (samplerRate/Constant.INDEX_TIMES*SECOND_PRESCREEN);
		divider = (float) (MAX_WIDHT/drawSamplerCountPreScreen);
//		LogUtils.v("divider="+divider);
		yAxisTimes = getYAxisTimesByBitWidth(bitWidth);
		startDrawThread();
	}

	//	private float
	private void setAudioParams(int samplerRate,int bitWidth,boolean  rightChannel,int needSamplerCount){
		this.rightChannel=rightChannel;
		this.samplerRate=samplerRate;
		this.bitWidth=bitWidth;
		MAX_WIDHT=getWidth()-MARGIN_RIGHT;
		MAX_HEIGHT=getHeight()-MARKER_LINE;
		LogUtils.v(TAG+"tempCount="+needSamplerCount+" drawSamplerCountPreScreen="+drawSamplerCountPreScreen+" "+needSamplerCount/drawSamplerCountPreScreen);
		if (needSamplerCount>drawSamplerCountPreScreen){
			if ( (needSamplerCount/drawSamplerCountPreScreen) > 1.5){
				isFullScreen=true;
			}
			drawSamplerCountPreScreen=needSamplerCount;

		}
		divider = (float) (MAX_WIDHT/drawSamplerCountPreScreen);
		yAxisTimes = getYAxisTimesByBitWidth(bitWidth);

	}

	public void setmShowText(String mShowText) {
		this.mShowText = mShowText;
	}

	public void addAudioDatas(int[] tempDatas,int fileLength, int samplerRate,int bitWidth, boolean  rightChannel){
		setAudioParams(samplerRate,bitWidth ,rightChannel,fileLength );
		setWaveformPaintColor(R.color.waveformBorderLine);
		getTargetAudioBufs(tempDatas);
	}

	public void addAudioData(byte[] data,int size,int samplerRate,int bitWidth,boolean  rightChannel){
		setAudioParam(samplerRate,bitWidth ,rightChannel );
		getTargetAudioBuf(byteArray2SamplerArray(data,size));
//		getTargetAudioBuf(AudioUtils.byteArray2SamplerArray(data,size ,bitWidth ));
	}

	private final int BIT_8_WIDTH = 8;
	private final int BIT_16_WIDTH = 16;
	private final int BIT_24_WIDTH = 24;
	private final int BIT_32_WIDTH = 32;
	private float getYAxisTimesByBitWidth(int bitWidth){
        float tempYAxisTimes=0;
		switch (bitWidth){
			case BIT_8_WIDTH:
				tempYAxisTimes=(float)((float)Byte.MAX_VALUE/(float)MAX_HEIGHT);
//				LogUtils.v("tempYAxisTimes="+tempYAxisTimes);
				break;
			case BIT_16_WIDTH:
				tempYAxisTimes=(float)((float)Short.MAX_VALUE/(float) MAX_HEIGHT);
				break;
			case BIT_24_WIDTH:
				tempYAxisTimes=(float)((float)8388607/(float)MAX_HEIGHT);
				break;
			case BIT_32_WIDTH:
				tempYAxisTimes=(float)((float)Integer.MAX_VALUE/(float)MAX_HEIGHT);
				break;
		}
		return tempYAxisTimes;
	}

	private void setWaveformPaintColor(int id){
	    waveformPaint.setColor(getResources().getColor(id));
    }

	private int[] byteArray2SamplerArray(byte[] data,int size){
		int byteCountPreSampler = (bitWidth /8);
		if (data==null || size==0){
			return null;
		}
		int samplerCount=0;
		if (size% byteCountPreSampler==0){
			samplerCount =  size/byteCountPreSampler;
		}else{
			samplerCount =  size/byteCountPreSampler+1;
		}
		if (samplerCount==0){
			return null;
		}
		int tempData =0;
		int[] tempSamplerData=new int[samplerCount];

//		LogUtils.v("samplerCount="+samplerCount+" byteCountPreSampler="+byteCountPreSampler);
		int j=0;
		for(int i=0;i<samplerCount;i++){
			tempData=0;
			for(int k=0;k<byteCountPreSampler;k++){
//				LogUtils.v("================"+k);
				int tempBuf =0;
				if ((j+k)<data.length){
					tempBuf = ( data[j+k] << (k*8) );
				}
				tempData = (tempData | tempBuf);

			}
//			LogUtils.v("tempData="+tempData+" i="+i);
			tempSamplerData[i]=tempData;
			j+=byteCountPreSampler;
		}
		return tempSamplerData;
	}

	private void getTargetAudioBufs(int[] tempBuf){
//		LogUtils.v("getTargetAudioBuf");
		synchronized (inBuf) {
			for (int i = 0; i < tempBuf.length; i ++) {
				if (isRunning){
					if (rightChannel){
						if( (i+1) <tempBuf.length ){
							inBuf.add(tempBuf[i+1]);
						}
					}else{
//						LogUtils.v("tempBuf[i]="+tempBuf[i]);
						inBuf.add(tempBuf[i]);
					}
				}else{
					if (!inBuf.isEmpty()){
						inBuf.clear();
					}
					break;
				}
			}
		}
		startDrawsThread();
		tempBuf=null;
		LogUtils.v("inBuf.size="+inBuf.size());
	}


	private LinkedList<Integer> inBuf = new LinkedList<Integer>();//缓冲区数据
	//inBuf只保存采样点的index为INDEX_TIMES的倍数的采样点
	private void getTargetAudioBuf(int[] tempBuf){
//		LogUtils.v("getTargetAudioBuf");
		synchronized (inBuf) {
			for (int i = 0; i < tempBuf.length; i += Constant.INDEX_TIMES) {
				if (isRunning){
					if (rightChannel){
						if( (i+1) <tempBuf.length ){
							inBuf.add(tempBuf[i+1]);
						}
					}else{
//						LogUtils.v("----------------"+tempBuf[i]);
						inBuf.add(tempBuf[i]);
					}
				}else{
					if (!inBuf.isEmpty()){
						inBuf.clear();
					}
					break;
				}
			}
		}
	}


	public BaseAudioSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.holder = getHolder();
		holder.addCallback(this);
		initView();
	}

	private void initView() {
		circlePaint = new Paint();
		circlePaint.setColor(getResources().getColor(R.color.waveformCircle));
		circlePaint.setAntiAlias(true);

		waveformPaint = new Paint();
		waveformPaint.setColor(getResources().getColor(R.color.waveformCenterLine));// 画笔为color
		waveformPaint.setStrokeWidth(1/2);// 设置画笔粗细
		waveformPaint.setAntiAlias(true);
		waveformPaint.setFilterBitmap(true);
		waveformPaint.setStyle(Paint.Style.FILL);

		textPaint = new Paint();
		textPaint.setColor(getResources().getColor(R.color.waveformCenterLine));// 画笔为color
		textPaint.setTextSize(20);
		textPaint.setStrokeWidth(0);
	}

	private void startDrawWaveform(){
		if (inBuf.isEmpty()){
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}
		long currenTime = new Date().getTime();
		if(currenTime - previousTime >= DRAW_TIME){
			LinkedList<Integer> buf = new LinkedList<Integer>();
			synchronized (inBuf) {
				if (inBuf.size() == 0)
					return;
				Iterator iterator=inBuf.iterator();
				while(inBuf.size() > drawSamplerCountPreScreen){
					if (iterator.hasNext()){
						iterator.next();
						iterator.remove();
					}
				}
				/*while(inBuf.size() > drawSamplerCountPreScreen){
					inBuf.remove(0);
				}*/
				buf = (LinkedList<Integer>) inBuf.clone();// 保存
			}
			drawWaveForm(buf);
			previousTime = new Date().getTime();
		}
	}

	private float previousX=0;
	private float previousY=0;
	private void drawWaveForm(LinkedList<Integer> buf) {
//		LogUtils.v("====================");
		Canvas canvas= holder.lockCanvas();// 关键:获取画布

		if(canvas==null){
			return;
		}
		initBaseView(canvas);
		float baseLineX =(float) (buf.size()* divider);

		if(getWidth() - baseLineX <= MARGIN_RIGHT){//如果超过预留的右边距距离
			baseLineX = MAX_WIDHT;//画的位置x坐标
		}
		//变化的view
//		LogUtils.v("baseLineX="+baseLineX);
		canvas.drawCircle(baseLineX, RADIOUS, RADIOUS, circlePaint);// 上面小圆
		canvas.drawCircle(baseLineX, getHeight()-RADIOUS, RADIOUS, circlePaint);// 下面小圆
		canvas.drawLine(baseLineX, RADIOUS*2, baseLineX, getHeight()-RADIOUS*2, circlePaint);//垂直的线
		int upMinHeight=MARKER_LINE/2;
		int underMaxHeight=getHeight()-upMinHeight;
//		LogUtils.v("buf="+buf.size()+" rightChannle="+rightChannel);
		for (int i = 0; i <buf.size() ; i++) {
			if(isRunning){
				int bufData=buf.get(i);
				float y=0;
				if(bufData==0){
					y=getHeight()/2;// 调节缩小比例，调节基准线
				}else{
//					LogUtils.v("bufData="+bufData+" yAxisTimes="+yAxisTimes+" MAX_HEIGHT="+MAX_HEIGHT);
					y =bufData/yAxisTimes + getHeight()/2;
//					LogUtils.v("bufData/yAxisTimes="+bufData/yAxisTimes);
//					y =bufData/yAxisTimes;
//					LogUtils.v("y="+y+" getHeight()/2="+getHeight()/2);
				}

				float x=(i) * divider;

				if(x >= MAX_WIDHT){
					x = MAX_WIDHT;
				}
				if(y<=upMinHeight){
					y = upMinHeight;
				}
				if(y>underMaxHeight){
					y = underMaxHeight;
				}
				float y1 = getHeight() - y;

				if(y1<=upMinHeight){
					y1 = upMinHeight;
				}
				if(y1>underMaxHeight){
					y1 = underMaxHeight;
				}
				//				if(i>1000 && i<2000){

//				LogUtils.v("i="+i+" x="+x+" y="+y+" y1="+y1);

			/*if (x!=0 || y!=0){
				Rect tempRect=new Rect((int)(x-0.5), (int)(y-0.5), (int)(x+0.5), (int)(y+0.5));
				canvas.drawRect(tempRect, waveformPaint);
			}*/

				/*if(previousX!=0 || previousY!=0 || x!=0 || y!=getHeight()/2 || previousX!=x || previousY!=y){
				 *//*	path.moveTo(previousX,previousY);//设置起点
				float centerX=(x-previousX)/2+previousX;
				float centerY=(y-previousY)/2+previousY;
				path.quadTo(centerX,centerY,x,y);//3点画弧
				canvas.drawPath(path, waveformPaint);*//*

				canvas.drawLine(previousX, previousY,  x,y, waveformPaint);
			}
			previousX=x;
			previousY=y;*/
				canvas.drawLine(x, y,  x,y1, waveformPaint);//中间出波形
			}else{
				break;
			}

		}
//		previousX=0;
//		previousY=0;
//		LogUtils.v("========exit draw=============");
		holder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
	}


	public void reDrawThread(){
		if (drawThread!=null){
			isRunning=false;
			if (!inBuf.isEmpty()){
				inBuf.clear();
			}
			drawThread=null;
			Canvas canvas = holder.lockCanvas();// 关键:获取画布
			initBaseView(canvas);
			holder.unlockCanvasAndPost(canvas);
		}
	}

	public void stopDrawThread(){
		if (drawThread!=null || isRunning==true){
			isRunning=false;
			if (!inBuf.isEmpty()){
				inBuf.clear();
			}
			drawThread=null;
		}
	}

	private void startDrawThread(){
        if (drawThread==null || isRunning==false){
            isRunning=true;
            drawThread= new Thread(){
                public void run() {
                    while(isRunning){
                        startDrawWaveform();
                    }
                };
            };
            drawThread.start();
        }
    }

	private void startDrawsThread(){
		if (drawsThread==null || isRunning==false){
			isRunning=true;
			drawsThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while(isRunning){
						LogUtils.v("startDrawsThread start");
						setWaveformPaintColor(R.color.waveformBorderLine);
						drawWaveForm(inBuf);
						isRunning=false;
						break;
					}
					LogUtils.v("startDrawsThread exit");
				}
			});
			drawsThread.start();
		}
	}

	public void stopDrawsThread(){
		if (drawsThread!=null || isRunning==true){
			isRunning=false;
			if (!inBuf.isEmpty()){
				inBuf.clear();
			}
			drawsThread=null;
			LogUtils.v(TAG+"stopDrawsThread");
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		LogUtils.v(TAG+"surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		LogUtils.v(TAG+"surfaceCreated");
		initSurfaceView();
	}

	private  void initSurfaceView(){
		//初始化基础view
		Canvas canvas = holder.lockCanvas();
		if(canvas==null){
			return;
		}
		initBaseView(canvas);
		holder.unlockCanvasAndPost(canvas);
	}

	private void initBaseView(Canvas canvas){
		if (canvas==null){
			return;
		}
		//canvas.drawColor(Color.rgb(241, 241, 241));// 清除背景
		canvas.drawColor(getResources().getColor(R.color.waveformBacg));

		Paint borderLine =new Paint();
		borderLine.setColor(getResources().getColor(R.color.waveformBorderLine));
		//最上面的那根线
		canvas.drawLine(0, MARKER_LINE/2, getWidth(), MARKER_LINE/2, borderLine);
		//最下面的那根线
		canvas.drawLine(0, getHeight()-MARKER_LINE/2-1,getWidth(), getHeight()-MARKER_LINE/2-1, borderLine);
//		if (channelCount==2){
//			 canvas.drawLine(0, MARKER_LINE/2+MAX_HEIGHT/2, getWidth(),MARKER_LINE/2+MAX_HEIGHT/2, borderLine);//第二根线
//			 canvas.drawLine(0,  getHeight()/2+MAX_HEIGHT/2, getWidth(), getHeight()/2+MAX_HEIGHT/2, borderLine);//第3根线
//		}


		Paint centerLine =new Paint();
		centerLine.setColor(getResources().getColor(R.color.waveformCenterLine ));
		//中心线
		canvas.drawLine(0, getHeight()/2, getWidth() ,getHeight()/2, centerLine);

		canvas.drawCircle(0, RADIOUS, RADIOUS, circlePaint);// 上面小圆
		canvas.drawCircle(0, getHeight()-RADIOUS, RADIOUS, circlePaint);// 下面小圆
		canvas.drawLine(0, RADIOUS*2, 0, getHeight()-RADIOUS*2, circlePaint);//垂直的线

		float textWidth = textPaint.measureText(mShowText);
		Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();

		float progressHeight=getHeight()/2 + (fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
		canvas.drawText(mShowText, (int)((getWidth()-MARGIN_RIGHT+MARGIN_RIGHT/2)-textWidth/2), progressHeight , textPaint);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		LogUtils.v(TAG+"surfaceDestroyed");
	}

}
