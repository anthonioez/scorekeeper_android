package com.miciniti.scorekeeper.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.miciniti.scorekeeper.R;
import com.miciniti.scorekeeper.ScoreKeeper;
import com.miciniti.scorekeeper.ScoreKeeperService;
import com.miciniti.scorekeeper.utils.Utils;

public class Settings extends Activity implements OnClickListener
{
	public static final String TAG = "Settings";
   
	private EditText			mEtTurns;
	private EditText			mEtDivisor;
	private Button				mBtSave;

	private ProgressBar			mPbProgress;
	private TextView 			mTvStatus;

	private SettingsReceiver	mBroadcast;

	private Handler				mHandler = new Handler();

	private int mTurns;
	private int mDivisor;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mTurns 		= getIntent().getIntExtra("turns", 20);
		mDivisor 	= getIntent().getIntExtra("divisor", 2);
		
		setContentView(R.layout.settings);
		mEtTurns 	= (EditText) findViewById(R.id.turns);		
		mEtDivisor	= (EditText) findViewById(R.id.divisor);		
		
		mPbProgress = (ProgressBar) findViewById(R.id.progress);
		mTvStatus 	= (TextView) findViewById(R.id.status);		

		mBtSave 	= (Button) findViewById(R.id.save);		
		
		mBtSave.setOnClickListener(this);
		
		mPbProgress.setVisibility(View.INVISIBLE);
		mTvStatus.setVisibility(View.INVISIBLE);

		mEtTurns.setText(String.valueOf(mTurns));
		mEtDivisor.setText(String.valueOf(mDivisor));	
	}

	@Override
	public void onResume()
	{
		super.onResume();		
		
		register();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();		
		unregister();
	}

	@Override
	public void onPause()
	{
		super.onPause();	
		
		unregister();
	}


	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		ScoreKeeper.action(this, ScoreKeeperService.CMD_STOP, 0, null);			
	}

	@Override
	public void onClick(View v)
	{
		if(v == mBtSave)
		{
			String turns = mEtTurns.getText().toString().trim();
			if(turns.length() == 0)
			{
				Toast.makeText(this, "Enter the number of turns!", Toast.LENGTH_LONG).show();
				return;
			}

			String divisor = mEtDivisor.getText().toString().trim();
			if(divisor.length() == 0)
			{
				Toast.makeText(this, "Enter the divisor!", Toast.LENGTH_LONG).show();
				return;
			}

			mTurns = Utils.getStringInt(turns);
			mDivisor = Utils.getStringInt(divisor);
			
			if(mTurns == 0 || mDivisor == 0)
			{
				Toast.makeText(this, "Invalid parameters!", Toast.LENGTH_LONG).show();
				return;
			}
			
			if(!ScoreKeeperService.isRunning())
			{
				start();
			}
			else
			{
				save();
			}
		}		
	}
	
	public void start()
	{
		Log.i(TAG, "start...");

		ScoreKeeper.action(this, ScoreKeeperService.CMD_START, 0, null);		
	}
	
	public void save()
	{
		Log.i(TAG, "save...");

		ScoreKeeper.action(this, ScoreKeeperService.CMD_OPTS, mTurns, mDivisor);
				
		mBtSave.setEnabled(false);
	}

	public void register()
	{
		mBroadcast = new SettingsReceiver();
	
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ScoreKeeper.INTENT_RECONNECT);
		mIntentFilter.addAction(ScoreKeeper.INTENT_CONNECTING);
		mIntentFilter.addAction(ScoreKeeper.INTENT_CONNECTED);
		mIntentFilter.addAction(ScoreKeeper.INTENT_DISCONNECTED);
		mIntentFilter.addAction(ScoreKeeper.INTENT_OPTS);
		mIntentFilter.addAction(ScoreKeeper.INTENT_MESSAGE);
		mIntentFilter.addAction(ScoreKeeper.INTENT_ERROR);
		registerReceiver(mBroadcast, mIntentFilter);
	}

	public void unregister()
	{
		if (mBroadcast != null)
		{
			unregisterReceiver(mBroadcast);
			mBroadcast = null;
		}		
	}

	public class SettingsReceiver extends BroadcastReceiver
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
						start();
					}
					else if (action.equals(ScoreKeeper.INTENT_CONNECTING))
					{
						mPbProgress.setVisibility(View.VISIBLE);
						mTvStatus.setVisibility(View.VISIBLE);
						mTvStatus.setText("Connecting...");
					}
					else if (action.equals(ScoreKeeper.INTENT_CONNECTED))
					{
						mTvStatus.setText("Saving...");						
						save();
					}
					else if (action.equals(ScoreKeeper.INTENT_DISCONNECTED))
					{
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
						mPbProgress.setVisibility(View.INVISIBLE);
						mTvStatus.setVisibility(View.VISIBLE);
						mTvStatus.setText(msg);
					}
					else if (action.equals(ScoreKeeper.INTENT_MESSAGE))
					{
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
						ScoreKeeper.toast(Settings.this, mHandler, msg);
					}
					else if (action.equals(ScoreKeeper.INTENT_ERROR))
					{
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
//						ScoreKeeper.toast(Login.this, mHandler, msg);
						
						mPbProgress.setVisibility(View.INVISIBLE);
						mTvStatus.setVisibility(View.VISIBLE);
						mTvStatus.setText(msg);
						mBtSave.setEnabled(true);

						ScoreKeeper.action(Settings.this, ScoreKeeperService.CMD_STOP, 0, null);
					}
					else if (action.equals(ScoreKeeper.INTENT_OPTS))
					{
//						long id = intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);

						ScoreKeeper.toast(Settings.this, mHandler, msg);
						
						mPbProgress.setVisibility(View.INVISIBLE);
						mTvStatus.setVisibility(View.INVISIBLE);

						mBtSave.setEnabled(true);
					}
				}
			}
		}
	}

}