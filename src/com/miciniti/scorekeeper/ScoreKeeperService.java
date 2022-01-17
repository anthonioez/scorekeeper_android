package com.miciniti.scorekeeper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.ConnectionClosedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.miciniti.scorekeeper.activities.Scores;
import com.miciniti.scorekeeper.utils.Utils;

public class ScoreKeeperService extends Service
{
	private static final String			TAG						= "ScoreKeeperService";

	public static final int 	TIMEOUT_CONNECTION 	= 5000;
	public static final int 	TIMEOUT_READ 		= 0;
	public static final int 	CONNECTION_RETRIES 	= 3;
	public static final int 	NOTIFICATION_ID 	= 32320;

	public static final String CMD_START 	= "init";
	public static final String CMD_STOP 	= "stop";	
	
	
	public static final String CMD_ERROR	= "erro";
	public static final String CMD_SCORE	= "play";
	public static final String CMD_HILITE	= "lite";
	public static final String CMD_REMOVE	= "wipe";
	public static final String CMD_LIST 	= "list";
	public static final String CMD_AUTH 	= "auth";
	public static final String CMD_PASS 	= "pass";
	public static final String CMD_OPTS 	= "opts";
	public static final String CMD_QUIT 	= "quit";

	public static final int OFFSET = 0;
		
	private static ScoreKeeperSocket	socketThread;
	private static boolean				connected		= false;
	private static boolean 				runningSocket 	= false;
	
	private byte[]						_buffer;
	private boolean						stopped = false;
	
	private NotificationManager 		notificationManager;
	
	private List<String>				commands;

	private long 						lastLead = -1;

	@Override
	public void onCreate() 
	{
		super.onCreate();
		
		lastLead = -1;
		Log.i(TAG, "Service created...");
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);		
		
		commands = new ArrayList<String>();
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	
		stop(true);
		
		clearNotification();
		
		Log.i(TAG, "Service destroyed");
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		String action = "";
		
		Log.i(TAG, "Service command...");
		
		if(intent == null)
		{
			
		}
		else
		{
			Bundle b = intent.getExtras();
			if (b != null)
			{				
				action = b.getString(ScoreKeeper.KEY_ACTION);
				Log.i(TAG, "action: " + action);
				
				if(action == null)
				{
				}
				else if(action.equals(CMD_START))
				{
					start();
				}
				else if(action.equals(CMD_STOP))
				{
					stop(true);
				}
				else if(action.equals(CMD_SCORE))
				{
					long 	id	 	= intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
					long 	delta 	= intent.getLongExtra(ScoreKeeper.KEY_DATA, 0);
					
					String cmd = createCommand(action, id, delta);
					execute(cmd);
				}
				else if(action.equals(CMD_HILITE))
				{
					long 	id	 	= intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
					long 	state 	= intent.getLongExtra(ScoreKeeper.KEY_DATA, 0);
					
					String cmd = createCommand(action, id, state);
					execute(cmd);
				}
				else if(action.equals(CMD_REMOVE))
				{
					long 	id	 	= intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
					long 	state 	= intent.getLongExtra(ScoreKeeper.KEY_DATA, 0);
					
					String cmd = createCommand(action, id, state);
					execute(cmd);
				}
				else if(action.equals(CMD_QUIT))
				{
					long 	id	 	= intent.getLongExtra(ScoreKeeper.KEY_ID, 0);

					String cmd = createCommand(action, id, 0);
					execute(cmd);
					
					Utils.sleeper(3000);
					
					stop(true);
				}
				else if(action.equals(CMD_AUTH))
				{
					long 	id		= intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
					String 	name	= intent.getStringExtra(ScoreKeeper.KEY_DATA);
					
					String cmd = createCommand(action, id, name);
					execute(cmd);
				}
				else if(action.equals(CMD_PASS))
				{
					long 	id		= intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
					String 	pass	= intent.getStringExtra(ScoreKeeper.KEY_DATA);
					
					String cmd = createCommand(action, id, pass);
					execute(cmd);
				}
				else if(action.equals(CMD_OPTS))
				{
					long 	turns	 	= intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
					long 	divisor 	= intent.getLongExtra(ScoreKeeper.KEY_DATA, 0);
					
					String cmd = createCommand(action, turns, divisor);
					execute(cmd);
				}
				else if(action.equals(CMD_LIST))
				{		
//					long 	id		= intent.getLongExtra(ScoreKeeper.KEY_ID, 0);
					
					String cmd = createCommand(action, 0, null);
					execute(cmd);
				}
				else
				{
				}
			}
		}
		
	    return START_STICKY;
	}

	public void broadcast(String intentAction, long id, String msg)
	{
		Log.i(TAG, "Broadcasting intent: " + intentAction + " id: " + id + " msg: " + msg);
		
		Intent mIntent = new Intent(intentAction);
		mIntent.setAction(intentAction);
		mIntent.putExtra(ScoreKeeper.KEY_ID, id);
		mIntent.putExtra(ScoreKeeper.KEY_DATA, msg);
		sendBroadcast(mIntent);
	}
    
	public void setNotification(String message) 
	{
		Context context = this.getApplicationContext();

		Intent notificationIntent = new Intent(this, Scores.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);		

		Notification notification = new Notification(android.R.drawable.ic_popup_sync, message, System.currentTimeMillis());
//		notification.defaults |= Notification.DEFAULT_SOUND;		
		notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;	//Notification.FLAG_AUTO_CANCEL;
//		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
		notification.setLatestEventInfo(context, ScoreKeeper.getAppName(), message, contentIntent);
		
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	public void clearNotification()
	{
//		stopForeground(true);
		
		notificationManager.cancel(NOTIFICATION_ID);		
	}

	public void start() 
	{	
		Log.i(TAG, "Service start");

//		setNotification("Started...");
				
		stopped = false;

		if(socketThread == null)
		{
			socketThread = new ScoreKeeperSocket();
			socketThread.start();		
		}
	}
	
	public void stop(boolean why) 
	{
		stopped = why;

		Log.i(TAG, "Service stop");
		
//		setNotification("Stopped...");
		clearNotification();
		
		runningSocket = false;
		
		if(socketThread != null)
		{
			socketThread.stopWriter();
			
			socketThread.interrupt();
			socketThread.quit();
			socketThread = null;
		}
	}

	public void execute(String cmd)
	{
		Log.i(TAG, "CMD: " + cmd);

		enqueueCommand(cmd);
		if(!isRunning())
		{
			broadcast(ScoreKeeper.INTENT_RECONNECT, 0, null);
		}
	}

	public static boolean isRunning()
	{
		return runningSocket;
	}

	public static boolean isConnected()
	{
		return connected;
	}


	public void readList(String msg, Object data)
	{
		Log.i(TAG, "readList");

		if(data == null) return;
		
		if(!(data instanceof JSONArray)) return;

		JSONArray ja = (JSONArray) data;
		
		ScoreKeeper.scores = new ArrayList<Score>();
		for(int i = 0; i < ja.length(); i++)
		{
			try
			{
				JSONObject jo = ja.getJSONObject(i);
				
				Score score = new Score();
				if(score.copyJSON(jo))
				{
					ScoreKeeper.scores.add(score);
				}
			}
			catch(Exception e)
			{
				
			}
		}
		
		Collections.sort(ScoreKeeper.scores, Collections.reverseOrder(new ScoreComparator()));			

		broadcast(ScoreKeeper.INTENT_UPDATE, 0, msg);
		
		if(ScoreKeeper.scores.size() > 1)
		{
			Score score1 = ScoreKeeper.scores.get(0);
			Score score2 = ScoreKeeper.scores.get(1);
			if(score1 != null && score2 != null)
			{
				long id = score1.getId();
				/*lastLead  != -1 && lastLead !=  && */
				if(lastLead != id && score1.getScore() != score2.getScore())
				{
					setNotification("Player [" + score1.getPlayer() + "] is leading!");					
					
					Utils.vibrate(this, 2000);

					lastLead = id;
				}
			}			
		}		
	}

	public void vibrate(final int i)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				doVibrate(i);
			}
		}).start();
	}

	public void doVibrate(int duration) 
	{
		try
		{			
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(duration);			
		}
		catch(Exception e)
		{
			
		}
	}	
	
	public void readLogin(String msg, Object data)
	{
		Log.i(TAG, "readLogin");
		
		if(data == null) return;
		
		long id = 0;
		if(data instanceof Integer)
			id = (Integer)data;
		else if(data instanceof Long)
			id = (Long)data;
		else
			return;

		if(id > 0)
		{
			broadcast(ScoreKeeper.INTENT_LOGIN, id, msg);
		}
		else
		{
			broadcast(ScoreKeeper.INTENT_RECONNECT, 0, null);			
		}
	}

	public void readPass(String msg, Object data)
	{
		Log.i(TAG, "readPass");
		
		if(data == null) return;
		
		long id = 0;
		if(data instanceof Integer)
			id = (Integer)data;
		else if(data instanceof Long)
			id = (Long)data;
		else
			return;

		if(id > 0)
		{
			broadcast(ScoreKeeper.INTENT_PASS, id, msg);
		}
		else
		{
			broadcast(ScoreKeeper.INTENT_RECONNECT, 0, null);			
		}
	}

	public void readScore(String msg, Object data)
	{
		Log.i(TAG, "readScore");
		
		if(data == null) return;
		
		long score = 0;
		if(data instanceof Integer)
			score = (Integer)data;
		else if(data instanceof Long)
			score = (Long)data;
		else
			return;

		broadcast(ScoreKeeper.INTENT_SCORE, score, msg);
	}

	public void readOpts(String msg, Object data)
	{
		Log.i(TAG, "readOpts");
		
		broadcast(ScoreKeeper.INTENT_OPTS, 0, msg);
	}

	public void readError(String msg, Object data)
	{
		Log.i(TAG, "readError");
		
		broadcast(ScoreKeeper.INTENT_ERROR, 0, msg);			
	}

	public void send(String string)
	{
		if(socketThread == null) return;
		
		// TODO check is socket is running
		
		socketThread.write(string);				
	}

	private String createCommand(String action, long id, String name)
	{
		String cmd = null;
		
		JSONObject json = new JSONObject();
		try
		{
			json.put("cmd", action);
			json.put("id", id);
			json.put("data", name);
	
			cmd = json.toString();
		}
		catch (JSONException e)
		{
		}					
		return cmd;
	}

	private String createCommand(String action, long id, long data)
	{
		String cmd = null;
		
		JSONObject json = new JSONObject();
		try
		{
			json.put("cmd", action);
			json.put("id", id);
			json.put("data", data);
	
			cmd = json.toString();
		}
		catch (JSONException e)
		{
		}					
		return cmd;
	}

	private void enqueueCommand(String cmd)
	{
		if(commands == null) commands = new ArrayList<String>();
		
		synchronized(commands)
		{
			commands.add(cmd);
		}
	}
	
	private String getCommand()
	{
		if(commands == null || commands.size() == 0) return null;
		
		String cmd = null;
		
		synchronized(commands)
		{
			cmd = commands.get(0);
			commands.remove(0);
		}
		return cmd;
	}
	
	public class ScoreKeeperSocket extends Thread
	{
		final String TAG = "ScoreKeeperReaderSocket";
		
		boolean 					ready 	= false;
		int 						retry 	= 0;
		Socket 						_socket = null;
		InputStream 				_in;
	    OutputStream 				_out;        	    
		ScoreKeeperSocketWriter 	socketWriter;

		public void run()
		{	
			Log.i(TAG,	"run()");
			
			runningSocket = true;
			
			while(runningSocket)
			{
				init();
						
				loop();
				
				quit();
				
				retry();				
			}
		
			runningSocket = false;
			
//			ScoreKeeperService.this.stop(true);
		}

		public boolean init()
		{
			Log.i(TAG,	"init()");
			
			ready = false;
			try
			{
				String ip 	= ScoreKeeper.SERVER_ADDRESS;
				String port	= ScoreKeeper.SERVER_PORT;
				
		        broadcast(ScoreKeeper.INTENT_CONNECTING, 0, "");	

		        Log.i(TAG, "Opening socket connection " + ip + ":" + port + "...");		        
		        
				InetSocketAddress remoteAddr = new InetSocketAddress(ip, Integer.parseInt(port));
				
				_socket = new Socket();
				
				_socket.connect(remoteAddr, TIMEOUT_CONNECTION);
				
		        Log.i(TAG, "Socket " + _socket.getLocalPort() + " connection open...");
		
		        _in = _socket.getInputStream();
		        _out = _socket.getOutputStream();

//		        broadcast(ScoreKeeper.INTENT_MESSAGE, 0, "Connection successful");	

//		        Utils.sleeper(2000);
		        
				_socket.setSoTimeout(TIMEOUT_READ);
		        
		        broadcast(ScoreKeeper.INTENT_CONNECTED, 0, "");	

		        ready = true;
			}
			catch(Exception e)
			{
		        Log.i(TAG, "init Exception: " + e);
		        
		        String msg = e.getMessage();		        
		        if(!stopped)
		        	broadcast(ScoreKeeper.INTENT_MESSAGE, 0, msg);
		        
		        ready = false;
			}	
			
			return ready;
		}

		public void quit()
		{
			Log.i(TAG,	"quit()");

			try{	if(_in != null) _in.close();   }
		    catch(IOException ioe){}                
		
		    try{	if(_out != null) _out.close();   }
		    catch(IOException ioe){}                
		
		    try{	if(_socket != null) _socket.close(); }
		    catch(IOException ioe){}
		}

		public void loop()
		{
			Log.i(TAG,	"loop()");

			if(!ready) return;
			
//			setNotification("Running...");

			stopWriter();
			
			socketWriter = new ScoreKeeperSocketWriter();
	        socketWriter.start();
	        
	        while(runningSocket)
			{				
		        Log.i(TAG, "Waiting to read from socket...");
		        
		        //scorekeeper mode
		        if(readRequest())
		        {		    
			        if(!processResponse(_buffer)) 
			        {
			        	ready = false;
			        	break;
			        }
		        }
		        else
		        {
//					setNotification("Stopped...");
		        	break;
		        }
			}
	        
	        stopWriter();
		}
		
		public void retry()
		{
			if(ready) return;
			
			if(stopped) return;
			
			Thread.yield();
			Utils.sleeper(2000);
	        			
			if(++retry > CONNECTION_RETRIES) 
			{
				runningSocket = false;
				broadcast(ScoreKeeper.INTENT_ERROR, 0, "Unable to connect to server!");
				
				return;
			}

			if(!stopped)
				broadcast(ScoreKeeper.INTENT_MESSAGE, retry, "Retrying (" + retry + " of " + CONNECTION_RETRIES + ")");
			
			Thread.yield();
			Utils.sleeper(2000);
		}
		
		private boolean readRequest()
		{
			Log.i(TAG,	"readRequest()");

			int read = read();
		    if(read == 0 || read == -1)		//timeout(0), closed(-1)
		    {
		        Log.i(TAG,	"read error");
		         
				connected = false;
				
		        ready = false;
				
		        if(stopped) return false;

		        String status;
		        if(read == -1)
		        	status = "Server closed connection!";
		        else
		        	status = "Server not reachable!";
		        
		        broadcast(ScoreKeeper.INTENT_DISCONNECTED, 0, status);	
		        
				return false;
		    }	        
		    
		    String str = new String(_buffer);
		    Log.i(TAG, "Read from socket: [" + str + "]");	        			        
		
			if(stopped) return false;
			
			connected = true;
			
        	ready = true;
        	retry = 0;

		    return true;			
		}

		private boolean processResponse(byte[] buffer)
		{
			Log.i(TAG, "processResponse()");
			
	        String response = new String(buffer);

//TODO			if(!Utilities.isValidJSON(response)) return;
			
	        JSONObject jo = null;
			try
			{
				jo = new JSONObject(new JSONTokener(response));
				if ((jo != null) && jo.has("res")) 
				{
					String res 	= jo.optString("res");
					String msg 	= jo.optString("msg");					
					Object data = jo.get("data");
					
					Log.i(TAG, "Response: " + res);
					Log.i(TAG, "Message: " + msg);
					
					if(res.equalsIgnoreCase(CMD_LIST))
					{
						readList(msg, data);
					}
					else if(res.equalsIgnoreCase(CMD_AUTH))
					{
						readLogin(msg, data);
					}
					else if(res.equalsIgnoreCase(CMD_PASS))
					{
						readPass(msg, data);
					}
					else if(res.equalsIgnoreCase(CMD_OPTS))
					{
						readOpts(msg, data);
					}
					else if(res.equalsIgnoreCase(CMD_SCORE))
					{
						readScore(msg, data);
					}
					else if(res.equalsIgnoreCase(CMD_ERROR))
					{
						readError(msg, data);
					}
			        return true; 
				}
			}
			catch (JSONException e)
			{
				Log.i(TAG, "processResponse: " + e);	

				return false;
			}
			return false;		
		}

		public boolean write(String data)
		{
			if(data == null) return false;
	     
			data += "\n";
			Log.i(TAG, "write Writing to socket " + _socket.getLocalPort() + " : [" + data + "]");
			try
			{
		        _out.write(data.getBytes(), 0, data.length());

		        _out.write(0);
		        
		        _out.flush();
		        
		        Log.i(TAG, "write ok");				
		        return true;
			}
			catch(Exception e)
			{
		        Log.i(TAG, "write Exception: " + e);				
			}
			return false;
		}

		private int read() 
		{
		    ByteArrayOutputStream bStrm = new ByteArrayOutputStream();
			
			Log.i(TAG, "read: Reading from socket " + _socket.getLocalPort() + " ...");
		
			int bytes = 2048;
			int ch = 0;
			int read = 0;
			int count = 0;
			while (count < bytes) 
			{
				try 
				{
					ch = _in.read();
					if(ch == -1)
					{
						read = -1;
						break;
					}
					else if(ch == 0)
					{
						break;
					}
		
				} 
				catch (ConnectionClosedException e)
				{
					Log.i(TAG, "read ConnectionClosedException on socket: " + e);
					read = -1;
					break;					
				}
				catch (IOException e) 
				{
					Log.i(TAG, "read IOException on socket: " + e);
					read = 0;
					break;
				}
				bStrm.write(ch);
				count++;
				read++;
			}
			_buffer = bStrm.toByteArray();
			return read;
		}
		
		public void stopWriter()
		{
		    if(socketWriter != null)
		    {
		    	socketWriter.interrupt();
		        socketWriter = null;
		    }
		}

		public class ScoreKeeperSocketWriter extends Thread
		{
			final String TAG = "ScoreKeeperSocketWriter";

			public void run()
			{	
				Log.i(TAG,	"run()");

				while(runningSocket)
				{
			        String cmd = getCommand();
			        if(cmd == null) continue;
	
			        write(cmd);
			        if(cmd.contains("\"quit\""))
			        {			        	
			        	Utils.sleeper(2000);
			        	runningSocket = false;
			        	stopped = true;
			        	
			        	ScoreKeeperService.this.stop(true);
			        	break;
			        }
				}
			}
		}
				
	}

	public class ScoreComparator implements Comparator<Score>
	{
		@Override
		public int compare(Score lhs, Score rhs)
		{
			return ((int) (lhs.getScore() - rhs.getScore()));
		}
	}}

