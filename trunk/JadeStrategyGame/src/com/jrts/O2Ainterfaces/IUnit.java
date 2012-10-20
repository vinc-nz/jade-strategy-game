package com.jrts.O2Ainterfaces;

import com.jrts.environment.Position;

public interface IUnit {
	
	public int getLife();
	public String getStatus();
	public String getId();
	public Position getPosition();
	public String getTeamName();
}
