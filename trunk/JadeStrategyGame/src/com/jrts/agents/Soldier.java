package com.jrts.agents;

import com.jrts.common.AgentStatus;
import com.jrts.common.GameConfig;
import com.jrts.environment.CellType;
import com.jrts.environment.Direction;
import com.jrts.environment.Position;
import com.jrts.logic.AttacksManager;

@SuppressWarnings("serial")
public class Soldier extends Unit {
	
	int knapsack = 0;
	CellType resourceCarried;

	public Soldier() {
		super();
	}

	public Soldier(Position position) {
		super(position);
	}

	@Override
	protected void setup(){
		super.setup();
		setLife(GameConfig.SOLDIER_LIFE);
		setSpeed(GameConfig.SOLDIER_SPEED);
		setForceOfAttack(GameConfig.SOLDIER_DAMAGES);
		setSight(GameConfig.SOLDIER_SIGHT);
		
		switchStatus(AgentStatus.FREE);
		
//		addBehaviour(new SendAttack(this));
	}
	
	public void sendHit(Direction direction) {
		AttacksManager.addHit(getPosition().clone(), direction, GameConfig.SOLDIER_DAMAGES);
	}
}
