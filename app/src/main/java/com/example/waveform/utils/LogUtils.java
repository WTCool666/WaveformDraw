package com.example.waveform.utils;

import android.util.Log;

/**
 * 日志输出
 */
public class LogUtils {

	private LogUtils() {

	}
	private static final String TAG = "KW_LOG";
	private static boolean debug = false;

	public static void setDebugMode(boolean flag) 
	{
		debug = flag;
	}

	public static void i(String text) {
		if (debug) {
			Log.i(TAG, text);
		}
	}

	public static void i(String tag, String text)
	{
		if (debug) {
			Log.i(tag, text);
		}
	}

	public static void e(String text)
	{
		if (debug) {
			Log.e(TAG, text);
		}
	}
	public static void b(byte[] bytearray)
	{
		if(debug){
			StringBuffer buf = new StringBuffer(bytearray.length);
			for(Byte data:bytearray){
				buf.append(String.format(" %02X", data));
			}
			Log.i(TAG,"size("+bytearray.length+")"+buf.toString());
		}
	}
	
	public static void b(String tag, byte[] bytearray)
	{
		if(debug){
			StringBuffer buf = new StringBuffer(bytearray.length);
			for(Byte data:bytearray){
				buf.append(String.format(" %02X", data));
			}
			Log.i(tag,"size("+bytearray.length+")"+buf.toString());
		}
	}
	
	public static void it(int[] inbuf)
	{
		if(debug){
			StringBuffer buf = new StringBuffer(inbuf.length*4);
			for(Integer data:inbuf){
				buf.append(String.format(" %04X", data));
			}
			Log.i(TAG," "+buf.toString());
		}
	}
	public static void e(String tag, String text)
	{
		if (debug) {
			Log.e(tag, text);
		}
	}

	public static void d(String text)
	{
		if (debug) {
			Log.d(TAG, text);
		}
	}

	public static void d(String tag, String text)
	{
		if (debug) {
			Log.d(tag, text);
		}
	}

	public static void v(String text)
	{
		{
			Log.d(TAG, text);
		}
	}

	public static void v(String tag, String text)
	{
		Log.d(tag, text);

	}

	public static void w(String text)
	{
		if (debug) {
			Log.w(TAG, text);
		}
	}

	public static void w(String tag, String text)
	{
		if (debug) {
			Log.w(tag, text);
		}
	}

	public static void println(String text)
	{
		if (debug) {
			System.out.println(text);
		}
	}
}
