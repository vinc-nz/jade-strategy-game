package com.jrts.agents;

import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

import com.jrts.O2Ainterfaces.IUnit;
import com.jrts.behaviours.CheckGoals;
import com.jrts.common.UnitFactory;
import com.jrts.environment.Position;
import com.jrts.environment.World;
import com.jrts.messages.GoalLevels;

public abstract class GoalBasedAI extends JrtsAgent {

	private static final long serialVersionUID = 8548442850146189200L;

	long unitCounter = 0;
	
	GoalLevels goalLevels;
	UnitFactory unitFactory;
	
	public GoalBasedAI() {
		
	}
	
	protected void setup(){
		super.setup();
		
		Object[] args = getArguments();
		if (args != null) {
			setTeamName((String) args[0]);
			setUnitFactory((UnitFactory) args[1]);
		} else {
			System.out.println("Needs team's name");
			System.exit(1);
		}
		
		addBehaviour(new CheckGoals());
	}
	
	public AID trainUnit(Class<? extends Unit> claz)  {
		World world = World.getInstance();
		String unitName = getTeamName() + "-" + claz.getSimpleName() + unitCounter++;
		Position unitPosition = world.neighPosition(world.getCityCenter(getTeamName()));
		if(unitPosition != null){
			//Instantiate the unit
			PlatformController container = getContainerController();
			AgentController agentController;
			try {
				Object[] args = {unitPosition, getTeamName()};
				agentController = container.createNewAgent(unitName, claz.getName(), args);
				agentController.start();
				IUnit o2a = agentController.getO2AInterface(IUnit.class);
				World.getInstance().addUnit(unitPosition, unitName, o2a);
				
				AID unitAID = new AID(unitName, AID.ISLOCALNAME);
				return unitAID;
			} catch (ControllerException e) {
				e.printStackTrace();
			}
		}
		else{
			System.out.println(getTeamName() + ":Cannot instantiate the unit");
		}
		return null;
	}
	
	public void updateGoalLevels(GoalLevels goals) {
		this.goalLevels = goals;
	}

	public GoalLevels getGoalLevels() {
		return goalLevels;
	}

	public void setGoalLevels(GoalLevels goalLevels) {
		this.goalLevels = goalLevels;
	}

	public long getUnitCounter() {
		return unitCounter;
	}

	public void setUnitCounter(long unitCounter) {
		this.unitCounter = unitCounter;
	}

	public UnitFactory getUnitFactory() {
		return unitFactory;
	}

	public void setUnitFactory(UnitFactory unitFactory) {
		this.unitFactory = unitFactory;
	}
}
