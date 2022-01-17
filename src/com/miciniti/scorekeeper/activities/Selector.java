package com.miciniti.scorekeeper.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.miciniti.scorekeeper.R;

public class Selector extends Activity implements OnClickListener
{
	public static final String TAG = "Selector";

	private Button			mPlayer;
	private Button 			mKeeper;
	private Button 			mSettings;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.selector);		

		mPlayer 	= (Button) findViewById(R.id.player);		
		mKeeper 	= (Button) findViewById(R.id.keeper);		
		mSettings 	= (Button) findViewById(R.id.settings);		
		
		mPlayer.setOnClickListener(this);
		mKeeper.setOnClickListener(this);
		mSettings.setOnClickListener(this);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void onClick(View v)
	{
		if(v == mPlayer)
		{
			Intent intent = new Intent(this, Login.class);
			intent.putExtra("mode", 0);
			startActivity(intent);
		}
		else if(v == mKeeper)
		{
			Intent intent = new Intent(this, Scores.class);
			startActivity(intent);
		}
		else if(v == mSettings)
		{
			Intent intent = new Intent(this, Login.class);
			intent.putExtra("mode", 1);
			startActivity(intent);
		}
		
	}
}