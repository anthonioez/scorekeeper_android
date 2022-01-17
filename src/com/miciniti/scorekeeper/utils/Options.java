package com.miciniti.scorekeeper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Options
{
	public static final String TAG = Options.class.getSimpleName();

	private static final String SHARPREFS 	= "prefs";
	private static final String KEY_TOKEN	= "key_token";

	public static void saveToken(Context mContext, long mValue)
	{
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(SHARPREFS, Context.MODE_PRIVATE);
		Editor editor = mSharedPreferences.edit();
		editor.putLong(KEY_TOKEN, mValue);
		editor.commit();
	}

	public static long getToken(Context mContext)
	{
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(SHARPREFS, Context.MODE_PRIVATE);
		return mSharedPreferences.getLong(KEY_TOKEN, 0);
	}

}
