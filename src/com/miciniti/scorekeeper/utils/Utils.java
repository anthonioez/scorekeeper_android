package com.miciniti.scorekeeper.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

public class Utils
{
	private static final String TAG = "Utils";
	
	private final static boolean logOn = true;
	
	public static void log(String tag, String msg)
	{	
		if (logOn)
		{
			Log.d(tag, "" + msg);
		}
	
	}

	public static void sleeper(int i)
	{
		try
		{
			Thread.sleep(i);
		}
		catch (InterruptedException e)
		{
		}
	}

	public static String streamString(InputStream is) throws Exception
	{
		String s = "";
		String line = "";
	
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	
		while ((line = rd.readLine()) != null)
		{
			s += line;
		}
	
		return s;
	}

	public static int getStringInt(String msg)
	{
		int score = 0;
		try
		{
			score = Integer.parseInt(msg);
		}
		catch(Exception e)
		{
			
		}
		return score;
	}

	public static void vibrate(final Context context, final int i)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				doVibrate(context, i);
			}
		}).start();
	}

	public static void doVibrate(Context context, int duration) 
	{
		try
		{			
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(duration);			
		}
		catch(Exception e)
		{
			Log.i(TAG, "vibrator: " + e.toString());
		}
	}				
}
