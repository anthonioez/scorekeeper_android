package com.miciniti.scorekeeper.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.miciniti.scorekeeper.R;
import com.miciniti.scorekeeper.Score;
import com.miciniti.scorekeeper.ScoreKeeper;
import com.miciniti.scorekeeper.ScoreKeeperService;
import com.miciniti.scorekeeper.utils.Utils;

public class Scores extends Activity implements OnItemClickListener
{
	public static final String TAG = "Scores";

	private static final int MENU_ITEM_HILITE = 1;
	private static final int MENU_ITEM_REMOVE = 2;

	private ProgressBar		mPbProgress;
	private TextView 		mTvStatus;
	private ListView 		mList;

	private ScoresListAdapter 	mAdapter;
	private ScoresReceiver 		mBroadcast;
	private Handler				mHandler = new Handler();

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.scores);
		
		mPbProgress 	= (ProgressBar) findViewById(R.id.progress);
		mTvStatus		= (TextView) findViewById(R.id.status);
		
		mList = (ListView) findViewById(R.id.list);
	    mList.setCacheColorHint(0xFF000000);
	    mList.setSelector(R.drawable.selector);
	    mList.setDividerHeight(0);
	    mList.setOnItemClickListener(this);

	    registerForContextMenu(mList);
	    
		LayoutInflater inflater = Scores.this.getLayoutInflater();
		View row = inflater.inflate(R.layout.scores_row, null);					
//	    mList.addHeaderView(row);
	    mList.addHeaderView(row, null, false);

		mPbProgress.setVisibility(View.INVISIBLE);
		mTvStatus.setVisibility(View.INVISIBLE);
		
	    mAdapter = new ScoresListAdapter(this);
		mList.setAdapter(mAdapter);			
		
		ScoreKeeper.scores = new ArrayList<Score>();
		mAdapter.notifyDataSetChanged();
	
		
		register();

		if(!ScoreKeeperService.isRunning())
		{
			start();
		}
		else
		{
			loadList();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();	
		
		unregister();
		
		ScoreKeeper.action(Scores.this, ScoreKeeperService.CMD_STOP, 0, null);
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
	    super.onCreateContextMenu(menu, v, menuInfo);
	    
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
	
		menu.add(0, MENU_ITEM_HILITE, 0, "Hilite");

		menu.add(0, MENU_ITEM_REMOVE, 0, "Remove");
		
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch(item.getItemId())
	    {
	    	case MENU_ITEM_HILITE:
	    		hilite(info.position);
	    		break;
	    	case MENU_ITEM_REMOVE:
	    		remove(info.position);
	    		break;
	    }
    	return super.onContextItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> adp, View v, int position, long id)
	{
		hilite(position);
	}
	
	public void hilite(int position)
	{
		if(position == 0) return;
		Score score = (Score) mAdapter.getItem(position - 1);
		if(score != null)
		{
			ScoreKeeper.action(this, ScoreKeeperService.CMD_HILITE, score.getId(), score.getState() == 1 ? 0 : 1);						
		}
	}

	public void remove(int position)
	{
		if(position == 0) return;
		Score score = (Score) mAdapter.getItem(position - 1);
		if(score != null)
		{
			ScoreKeeper.action(this, ScoreKeeperService.CMD_REMOVE, score.getId(), 0);						
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		ScoreKeeper.action(this, ScoreKeeperService.CMD_STOP, 0, null);			
	}

	public void register()
	{
		mBroadcast = new ScoresReceiver();

		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ScoreKeeper.INTENT_RECONNECT);
		mIntentFilter.addAction(ScoreKeeper.INTENT_CONNECTING);
		mIntentFilter.addAction(ScoreKeeper.INTENT_CONNECTED);
		mIntentFilter.addAction(ScoreKeeper.INTENT_DISCONNECTED);
		mIntentFilter.addAction(ScoreKeeper.INTENT_UPDATE);
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
	
	public void start()
	{
		Log.i(TAG, "start...");

		ScoreKeeper.action(this, ScoreKeeperService.CMD_START, 0, null);
	}
	
	public void loadList()
	{
		Log.i(TAG, "loadList...");
		
		ScoreKeeper.action(this, ScoreKeeperService.CMD_LIST, 0, null);
	}
	
	public class ScoresListAdapter extends BaseAdapter 
	{
	    public ScoresListAdapter(Context c) 
	    {
	    }
	
		public int getCount()
		{
			if (ScoreKeeper.scores != null)
				return ScoreKeeper.scores.size();
			else
				return 0;
		}

		public Object getItem(int position)
		{
			if (getCount() == 0)
				return null;

			if (position >= 0 && position < getCount())
			{
				return ScoreKeeper.scores.get(position);
			}

			return null;
		}

		public long getItemId(int position)
		{
			Score tweet = (Score) getItem(position);
			if(tweet != null)
				return tweet.getId();
			else
				return -1;
		}

		public View getView(final int position, View convertView, ViewGroup parent)
		{
			View row = convertView;

			final Score item = (Score) getItem(position);

			LayoutInflater inflater = Scores.this.getLayoutInflater();
			row = inflater.inflate(R.layout.scores_row, null);					

			if(item != null)
			{
				TextView 	player	= (TextView) row.findViewById(R.id.player);
				TextView 	score	= (TextView) row.findViewById(R.id.score);
				TextView 	rem		= (TextView) row.findViewById(R.id.rem);
				TextView 	mod		= (TextView) row.findViewById(R.id.mod);
					
				player.setText(item.getPlayer()); 
				score.setText(String.valueOf(item.getScore()));
				rem.setText(String.valueOf(item.getRemaining()));
				mod.setText(String.format("%.2f", item.getMod()));
				
				if(item.getState() == 1)
				{
					row.setBackgroundColor(0x88008000);					
				}
				else
				{
					row.setBackgroundColor(0x00000000);					
				}
			}
			return (row);
		}	
	}

	public class ScoresReceiver extends BroadcastReceiver
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
						mPbProgress.setVisibility(View.VISIBLE);
						mTvStatus.setVisibility(View.VISIBLE);
						mTvStatus.setText("Loading...");

						loadList();
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
						ScoreKeeper.toast(Scores.this, mHandler, msg);
					}
					else if (action.equals(ScoreKeeper.INTENT_ERROR))
					{
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
						mPbProgress.setVisibility(View.INVISIBLE);
						mTvStatus.setVisibility(View.VISIBLE);
						mTvStatus.setText(msg);
					}
					else if (action.equals(ScoreKeeper.INTENT_UPDATE))
					{
						String msg = intent.getStringExtra(ScoreKeeper.KEY_DATA);
//						ScoreKeeper.toast(Scores.this, mHandler, msg);

						mPbProgress.setVisibility(View.INVISIBLE);
						mTvStatus.setVisibility(View.VISIBLE);
						mTvStatus.setText(msg);

						mAdapter.notifyDataSetChanged();
					}
				}
			}
		}
	}
}