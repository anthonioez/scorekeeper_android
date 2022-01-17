package com.miciniti.scorekeeper;

import org.json.JSONException;
import org.json.JSONObject;

public class Score
{
	private long	id;
	private String 	player;
	private int		score;
	private int		remaining;
	private double	mod;
	private int		state;
	
	public Score()
	{
		this.id 		= -1;
		this.player 	= "";
		this.score 		= 0;
		this.remaining	= 0;
		this.mod		= 0;
		this.state		= 0;
	}
	
	public long getId()
	{
		return id;
	}
	
	public String getPlayer()
	{
		return player;
	}
	
	public int getScore()
	{
		return score;
	}	
	
	public int getRemaining()
	{
		return remaining;
	}
	
	public double getMod()
	{
		return mod;
	}
	
	public int getState()
	{
		return state;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public void setPlayer(String player)
	{
		this.player = player;
	}
	
	public void setScore(int score)
	{
		this.score = score;
	}

	public void setRemaining(int rem)
	{
		this.remaining = rem;
	}

	public void setMod(double mod)
	{
		this.mod = mod;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public boolean copyJSON(JSONObject jo)
	{		
		try
		{
			setId		(jo.getLong		("id"));
			setPlayer	(jo.getString	("name"));
			setScore	(jo.getInt		("score"));
			setRemaining(jo.optInt		("rem", 0));
			setMod		(jo.optDouble	("mod", 0));
			setState	(jo.optInt		("state", 0));
//			setStamp	(jo.optLong		("stamp", 0));

			return true;
		}
		catch (JSONException e)
		{
		}

		return false;
	}
}
