package com.miciniti.scorekeeper;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class ScoreKeeper extends Application
{
	private static final String TAG = "ScoreKeeper";

	public static String appName = "ScoreKeeper";

//	public static String 	SERVER_ADDRESS 	= "192.168.43.34";
	public static String 	SERVER_ADDRESS 	= "184.73.76.174";
	public static String 	SERVER_PORT		= "8124";


	public static final String INTENT_CONNECTING 	= "com.miciniti.scorekeeper.connecting";
	public static final String INTENT_CONNECTED 	= "com.miciniti.scorekeeper.connected";
	public static final String INTENT_DISCONNECTED 	= "com.miciniti.scorekeeper.disconnected";
	public static final String INTENT_RECONNECT 	= "com.miciniti.scorekeeper.reconnect";
	public static final String INTENT_UPDATE 		= "com.miciniti.scorekeeper.update";
	public static final String INTENT_SCORE 		= "com.miciniti.scorekeeper.score";
	public static final String INTENT_LOGIN 		= "com.miciniti.scorekeeper.login";
	public static final String INTENT_PASS	 		= "com.miciniti.scorekeeper.pass";
	public static final String INTENT_OPTS	 		= "com.miciniti.scorekeeper.opts";
	public static final String INTENT_MESSAGE 		= "com.miciniti.scorekeeper.message";	
	public static final String INTENT_ERROR 		= "com.miciniti.scorekeeper.error";
	
//	public static final String INTENT_CANCEL 		= "com.miciniti.scorekeeper.cancel";
//	public static final String INTENT_START 		= "com.miciniti.scorekeeper.start";
//	public static final String INTENT_STOP 			= "com.miciniti.scorekeeper.stop";

	public static final String KEY_ACTION 	= "action";
	public static final String KEY_DATA 	= "data";
	public static final String KEY_ID	 	= "id";

	public static List<Score>	scores;
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "Starting...");

		appName = this.getResources().getString(R.string.app_name);
		
		scores = new ArrayList<Score>();	
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
		Log.d(TAG, "Terminating...");
	}

	public static String getAppName()
	{
		return appName;
	}

	public static void action(Context context, String action, long id, long data)
	{
		Intent intent = new Intent(context, ScoreKeeperService.class);
		intent.putExtra(ScoreKeeper.KEY_ACTION, action);
		intent.putExtra(ScoreKeeper.KEY_DATA, data);
		intent.putExtra(ScoreKeeper.KEY_ID, id);
		context.startService(intent);
	}

	public static void action(Context context, String action, long id, String data)
	{
		Intent intent = new Intent(context, ScoreKeeperService.class);
		intent.putExtra(ScoreKeeper.KEY_ACTION, action);
		intent.putExtra(ScoreKeeper.KEY_DATA, data);
		intent.putExtra(ScoreKeeper.KEY_ID, id);
		context.startService(intent);
	}


	public static void toast(final Context context, Handler handler, final String msg)
	{
		if(msg == null || msg.length() == 0) return;

		handler.post(new Runnable()
		{
			public void run()
			{
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		});
	}

	public static void toastShort(final Context context, Handler handler, final String msg)
	{
		if(msg == null || msg.length() == 0) return;

		handler.post(new Runnable()
		{
			public void run()
			{
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
}