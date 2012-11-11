package com.jrts.agents;

import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import com.jrts.behaviours.PatrolBehaviour;
import com.jrts.behaviours.UpdateUnitTable;
import com.jrts.common.AgentStatus;
import com.jrts.common.GameConfig;
import com.jrts.common.Utils;
import com.jrts.environment.Direction;
import com.jrts.messages.Notification;
import com.jrts.messages.Order;


public class MilitaryAI extends GoalBasedAI {
	private static final long serialVersionUID = 9114684864072759345L;

	int soldierCounter = 0;
	
	@Override
	protected void setup() {
		super.setup();
		
		addBehaviour(new UpdateUnitTable(this, Soldier.class));

		addBehaviour(new WakerBehaviour(this, 5000) {
			private static final long serialVersionUID = 1746608629262055814L;
			@Override
			protected void handleElapsedTimeout() {
				createSoldier();
			}
		});
		
		addBehaviour(new WakerBehaviour(this, 10000) {
			private static final long serialVersionUID = 1746608629262055814L;
			@Override
			protected void handleElapsedTimeout() {
				createSoldier();
			}
		});
		
		addBehaviour(new WakerBehaviour(this, 10000) {
			private static final long serialVersionUID = 1746608629262055814L;

			@Override
			protected void handleElapsedTimeout() {
				addPatroler();
			}
		});
		
		addBehaviour(new WakerBehaviour(this, 15000) {
			private static final long serialVersionUID = 1746608629262055814L;

			@Override
			protected void handleElapsedTimeout() {
				addPatroler();
			}
		});
		
	}

	public void createSoldier()
	{
		if (resourcesContainer.isThereEnoughFood(GameConfig.SOLDIER_FOOD_COST) && 
				resourcesContainer.isThereEnoughWood(GameConfig.SOLDIER_WOOD_COST) ) {
			
			resourcesContainer.removeFood(GameConfig.SOLDIER_FOOD_COST);
			resourcesContainer.removeWood(GameConfig.SOLDIER_WOOD_COST);
			unitFactory.trainUnit(Soldier.class);
			soldierCounter++;
		}
	}

	public void addPatroler()
	{
		AID soldier = this.getUnitTable().getFreeUnits();
		if(soldier != null){
			
			Direction angle = Utils.getMapAnglePosition(cityCenter);
			
			Order order = new Order(AgentStatus.PATROLING);
			if(angle.equals(Direction.LEFT_UP))
			{
				if(Utils.random.nextBoolean())
					order.setPatrolDirection(Direction.RIGHT);
				else
					order.setPatrolDirection(Direction.DOWN);
			} else if(angle.equals(Direction.LEFT_DOWN))
			{
				if(Utils.random.nextBoolean())
					order.setPatrolDirection(Direction.RIGHT);
				else
					order.setPatrolDirection(Direction.UP);
			} else if(angle.equals(Direction.RIGHT_UP)){
				if(Utils.random.nextBoolean())
					order.setPatrolDirection(Direction.LEFT);
				else
					order.setPatrolDirection(Direction.DOWN);
			} else if(angle.equals(Direction.RIGHT_DOWN)){
				if(Utils.random.nextBoolean())
					order.setPatrolDirection(Direction.LEFT);
				else
					order.setPatrolDirection(Direction.UP);
			}
			
			switch (Utils.random.nextInt(3)) {
			case 0:
				order.setPatrolDistance(PatrolBehaviour.DISTANCE_LITTLE);
				break;
			case 1:
				order.setPatrolDistance(PatrolBehaviour.DISTANCE_MEDIUM);
				break;
			case 2:
				order.setPatrolDistance(PatrolBehaviour.DISTANCE_BIG);
				break;
			}
			
			changeAgentStatus(soldier, order);
		}
	}
	
	@Override
	protected void updatePerception() {
	}

	@Override
	public void onGoalsChanged() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void handleNotification(Notification notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleRequest(ACLMessage msg) {
		// TODO Auto-generated method stub
		
	}

}