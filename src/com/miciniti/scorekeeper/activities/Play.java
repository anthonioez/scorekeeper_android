package com.miciniti.scorekeeper.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.miciniti.scorekeeper.R;
import com.miciniti.scorekeeper.ScoreKeeper;
import com.miciniti.scorekeeper.ScoreKeeperService;

public class Play extends Activity implements OnClickListener
{
	public static final String TAG = "Play";

	private TextView 			mScore;
	private ImageButton 		mInc;
	private ImageButton 		mDec;

	private ProgressBar			mProgress;
	private TextView 			mStatus;
	
	private Button				mReset;
	private Button				mQuit;

	private int 				mCurrentScore;

	private long 				mId;	
	private String				mUsername;

	private BroadcastReceiver 	mBroadcast;

	private Handler				mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mId = getIntent().getLongExtra("id", 0);
		mCurrentScore = getIntent().getIntExtra("score", 0);
		mUsername = getIntent().getStringExtra("username");
		
		if(mId < 1)
		{
			Toast.makeText(this, "User Id not set!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if(mUsername == null || mUsername.length() == 0)
		{
			Toast.makeText(this, "Username not set!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		setContentView(R.layout.play);
		
		mScore	 	= (TextView) findViewById(R.id.score);		
		mProgress 	= (ProgressBar) findViewById(R.id.progress);
		mStatus 	= (TextView) findViewById(R.id.status);		

		mInc 		= (ImageButton) findViewById(R.id.inc);		
		mDec 		= (ImageButton) findViewById(R.id.dec);	
		
		mReset 		= (Button) findViewById(R.id.reset);	
		mQuit 		= (Button) findViewById(R.id.quit);			

		mInc.setOnClickListener(this);
		mDec.setOnClickListener(this);
		mQuit.setOnClickListener(this);
		mReset.setOnClickListener(this);
		
		mProgress.setVisibility(View.INVISIBLE);
		mStatus.setVisibility(View.INVISIBLE);		
		
		register();
		
		if(!ScoreKeeperService.isRunning())
		{
			Toast.makeText(this, "Service not running!", Toast.LENGTH_LONG).show();
			finish();
		}
		
		updateScore();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		unregister();	
		
		ScoreKeeper.action(Play.this, ScoreKeeperService.CMD_STOP, 0, null);
	}

	@Override
	public void onClick(View v)
	{
		if(v == mInc)
		{
			inc();
		}		
		if(v == mDec)
		{
			dec();
		}		
		else if(v == mReset)
		{
			reset();
		}		
		else if(v == mQuit)
		{
			quit();
		}		
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		ScoreKeeper.action(this, ScoreKeeperService.CMD_STOP, 0, null);			
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			inc();
			return true;
		}
		else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			dec();
			return true;
		}
		return super.onKeyDown(keyCode, event);
		
	}

	private void quit()
	{
		Log.i(TAG, "changeScore...");

		ScoreKeeper.action(this, ScoreKeeperService.CMD_QUIT, mId, null);		

		finish();		
	}

	private void reset()
	{
		changeScore(0);
	}

	private void inc()
	{
		changeScore(1);		
	}

	private void dec()
	{
		changeScore(-1);		
	}

	public void updateScore()
	{
		mHandler.post(new Runnable()
		{
			public void run()
			{
				mScore.setText(String.valueOf(mCurrentScore));
			}
		});
	}

	public void register()
	{
		mBroadcast = new PlayReceiver();
	
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ScoreKeeper.INTENT_RECONNECT);
		mIntentFilter.addAction(ScoreKeeper.INTENT_CONNECTED);
		mIntentFilter.addAction(ScoreKeeper.INTENT_DISCONNECTED);
		mIntentFilter.addAction(ScoreKeeper.INTENT_SCORE);
		mIntentFilter.addAction(ScoreKeeper.INTENT_MESSAGE);
		mIntentFilter.addAction(ScoreKeeper.INTENT_ERROR);
		registerReceiver(mBroadcast, mIntentFilter);
		
//		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

//		mAudioManager.registerMediaButtonEventReceiver(audioBroadcastReceiver);		
	}

	public void unregister()
	{
		if (mBroadcast != null)
		{
			unregisterReceiver(mBroadcast);
			mBroadcast = null;
		}		
	}

	public void changeScore(int delta)
	{
		Log.i(TAG, "changeScore...");

		ScoreKeeper.action(this, ScoreKeeperService.CMD_SCORE, mId, delta);		
	}
	
	
	public class PlayReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent != null)
			{
				String action = intent.getAction();
				if(action != null)
				{
					if (action.equals(ScoreKeeper.INTENT_RECONNECT))
					{
						finish();
					}
					else if (action.equals(ScoreKeeper.INTENT_DISCONNECTED))
					{
						finish();
					}
					else if (action.equals(ScoreKeeper.INTENT_MESSAGE))
					{
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
						ScoreKeeper.toast(Play.this, mHandler, msg);
					}
					else if (action.equals(ScoreKeeper.INTENT_ERROR))
					{
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
						ScoreKeeper.toast(Play.this, mHandler, msg);						
//						finish();
					}
					else if (action.equals(ScoreKeeper.INTENT_SCORE))
					{
						long score = intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
//						ScoreKeeper.toastShort(Play.this, mHandler, msg);
						
						mCurrentScore = (int)score;
						updateScore();
						
					}
				}
			}
		}
	}
}