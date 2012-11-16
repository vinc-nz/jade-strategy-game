package com.jrts.behaviours;

import jade.core.behaviours.CyclicBehaviour;

import com.jrts.agents.Worker;
import com.jrts.common.GameStatistics;
import com.jrts.environment.Direction;

public class SendAttack extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8832334971270701493L;
	private Worker unit;
	
	public SendAttack(Worker unit) {
		super();
		this.unit = unit;
	}

	@Override
	public void action() {
		GameStatistics.increaseCounter();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		unit.sendHit(Direction.random());
	}
}
