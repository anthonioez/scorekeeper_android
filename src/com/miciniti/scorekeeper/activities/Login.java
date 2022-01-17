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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.miciniti.scorekeeper.R;
import com.miciniti.scorekeeper.ScoreKeeper;
import com.miciniti.scorekeeper.ScoreKeeperService;
import com.miciniti.scorekeeper.utils.Options;
import com.miciniti.scorekeeper.utils.Utils;

public class Login extends Activity implements OnClickListener
{
	public static final String TAG = "Login";
   
	private EditText			mEtUsername;
	private EditText			mEtPassword;
	private ProgressBar			mPbProgress;
	private TextView 			mTvStatus;
	private Button				mBtLogin;
	private TextView 			mTvTitle;
	private LinearLayout 		mLyUser;
	private LinearLayout 		mLyPass;

	private String 				mPlayerName;
	private String 				mPlayerPass;

	private LoginReceiver 		mBroadcast;

	private Handler				mHandler = new Handler();

	private int 				mMode = 0;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mMode = getIntent().getIntExtra("mode", 0);
		
		setContentView(R.layout.login);
		mTvTitle	 	= (TextView) findViewById(R.id.title);
		mEtUsername 	= (EditText) findViewById(R.id.username);		
		mEtPassword	= (EditText) findViewById(R.id.password);		
		mPbProgress 	= (ProgressBar) findViewById(R.id.progress);
		mTvStatus 	= (TextView) findViewById(R.id.status);		

		mLyUser		= (LinearLayout) findViewById(R.id.user);
		mLyPass		= (LinearLayout) findViewById(R.id.pass);
		
		mBtLogin 	= (Button) findViewById(R.id.login);		
		
		mBtLogin.setOnClickListener(this);
		
		mPbProgress.setVisibility(View.INVISIBLE);
		mTvStatus.setVisibility(View.INVISIBLE);
		
		if(mMode == 0)
		{
			mTvTitle.setText("Login");
			mLyPass.setVisibility(View.GONE);
		}
		else
		{
			mTvTitle.setText("Auth");
			mLyUser.setVisibility(View.GONE);			
		}
		
//		mUsername.setText("sample user");
//		mEtPassword.setText("password");
	
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
	public void onBackPressed()
	{
		super.onBackPressed();

		ScoreKeeper.action(this, ScoreKeeperService.CMD_STOP, 0, null);			
	}

	@Override
	public void onPause()
	{
		super.onPause();	
		
		unregister();
	}

	@Override
	public void onClick(View v)
	{
		if(v == mBtLogin)
		{
			if(mMode == 0)
			{
				mPlayerName = mEtUsername.getText().toString().trim();
				if(mPlayerName.length() == 0)
				{
					Toast.makeText(this, "Enter your username!", Toast.LENGTH_LONG).show();
					return;
				}
			}
			else
			{
				mPlayerPass = mEtPassword.getText().toString().trim();
				if(mPlayerPass.length() == 0)
				{
					Toast.makeText(this, "Enter your password!", Toast.LENGTH_LONG).show();
					return;
				}
			}

			if(!ScoreKeeperService.isRunning())
			{
				start();
			}
			else
			{
				connect();
			}
		}		
	}

	public void connect()
	{
		if(mMode == 0)
		{
			login();
		}
		else 
		{
			pass();							
		}		
	}
	
	public void play(long id, int score)
	{
		Log.i(TAG, "play " + id + " : " + score);

		Options.saveToken(Login.this, id);

		Intent intent = new Intent(this, Play.class);
		intent.putExtra("id", id);
		intent.putExtra("score", score);
		intent.putExtra("username", mPlayerName);
		startActivity(intent);
	}	
	
	public void settings(int turns, int divisor)
	{
		Log.i(TAG, "settings: " + turns + " divisor: " + divisor);

		Intent intent = new Intent(this, Settings.class);
		intent.putExtra("turns", turns);
		intent.putExtra("divisor", divisor);
		startActivity(intent);
	}	
	
	public void start()
	{
		Log.i(TAG, "start...");

		ScoreKeeper.action(this, ScoreKeeperService.CMD_START, 0, null);		
	}
	
	public void login()
	{
		Log.i(TAG, "login...");
		
		long id = Options.getToken(Login.this);

		ScoreKeeper.action(this, ScoreKeeperService.CMD_AUTH, id, mPlayerName);
				
		mBtLogin.setEnabled(false);
	}
	
	public void pass()
	{
		Log.i(TAG, "pass...");
		
		long id = Options.getToken(Login.this);

		ScoreKeeper.action(this, ScoreKeeperService.CMD_PASS, id, mPlayerPass);
				
		mBtLogin.setEnabled(false);
	}
	
	public void register()
	{
		mBroadcast = new LoginReceiver();
	
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ScoreKeeper.INTENT_RECONNECT);
		mIntentFilter.addAction(ScoreKeeper.INTENT_CONNECTING);
		mIntentFilter.addAction(ScoreKeeper.INTENT_CONNECTED);
		mIntentFilter.addAction(ScoreKeeper.INTENT_DISCONNECTED);
		mIntentFilter.addAction(ScoreKeeper.INTENT_LOGIN);
		mIntentFilter.addAction(ScoreKeeper.INTENT_PASS);
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

	public class LoginReceiver extends BroadcastReceiver
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
						mTvStatus.setText("Logging in...");						
						connect();
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
						ScoreKeeper.toast(Login.this, mHandler, msg);
					}
					else if (action.equals(ScoreKeeper.INTENT_ERROR))
					{
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
//						ScoreKeeper.toast(Login.this, mHandler, msg);
						
						mPbProgress.setVisibility(View.INVISIBLE);
						mTvStatus.setVisibility(View.VISIBLE);
						mTvStatus.setText(msg);
						mBtLogin.setEnabled(true);

						ScoreKeeper.action(Login.this, ScoreKeeperService.CMD_STOP, 0, null);
					}
					else if (action.equals(ScoreKeeper.INTENT_LOGIN))
					{
						mPbProgress.setVisibility(View.INVISIBLE);
						mTvStatus.setVisibility(View.INVISIBLE);
						mBtLogin.setEnabled(true);


						long id = intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);						
						int score = Utils.getStringInt(msg);

						play(id, score);
					}
					else if (action.equals(ScoreKeeper.INTENT_PASS))
					{
						mPbProgress.setVisibility(View.INVISIBLE);
						mTvStatus.setVisibility(View.INVISIBLE);
						mBtLogin.setEnabled(true);

						long id = intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
						
						int divisor = Utils.getStringInt(msg);
						
						settings((int)id, divisor);							
					}
				}
			}
		}
	}

}