package com.jrts.agents;

import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;

import java.util.ArrayList;

import com.jrts.behaviours.PatrolBehaviour;
import com.jrts.behaviours.UpdateUnitTable;
import com.jrts.common.AgentStatus;
import com.jrts.common.GameConfig;
import com.jrts.common.GoalPriority;
import com.jrts.common.Utils;
import com.jrts.environment.Direction;
import com.jrts.messages.EnemySighting;
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
				addExplorer();
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
				resourcesContainer.isThereEnoughWood(GameConfig.SOLDIER_WOOD_COST)
				&& getTeamDF().countUnits() < GameConfig.POPULATION_LIMIT) {
			
			resourcesContainer.removeFood(GameConfig.SOLDIER_FOOD_COST);
			resourcesContainer.removeWood(GameConfig.SOLDIER_WOOD_COST);
			unitFactory.trainUnit(Soldier.class);
			soldierCounter++;
		}
	}

	public void addExplorer()
	{
		AID soldier = this.getUnitTable().getAFreeUnit();
		if(soldier != null)
			giveOrder(soldier, new Order(AgentStatus.EXPLORING));
	}
	
	public void addPatroler()
	{
		AID soldier = this.getUnitTable().getAFreeUnit();
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
			
			giveOrder(soldier, order);
		}
	}
	
	@Override
	protected void updatePerception() {
	}

	@Override
	public void onGoalsChanged() {
		// TODO Auto-generated method stub
	}

	public void onEnemySighting(EnemySighting e) {
		int numSightedSoldiers = e.getSoldierNumber();
		int distance = (int)e.getSightingPosition().distance(cityCenter);
		ArrayList<AID> freeSoldiers = unitTable.getFreeUnits();
		int numMyFreeSoldier = freeSoldiers.size();
		
		System.out.println(
				"\n----------------- Start OnEnemySighting ----------------\n" +
				"EnemySighting received: \n" + 
					e +
				"numSightedSoldiers = "+numSightedSoldiers+"\n"+
				"distance = " + distance +"\n"+
				"numMyFreeSoldier = "+numMyFreeSoldier+"\n");
		
		int x=1, y=1, z=1;
		switch (nature) {
		case AGGRESSIVE:
			x = 3;
			y = 3;
			z = 3;
			break;
		case AVERAGE:
			x = 2;
			y = 2;
			z = 1;
			break;
		case DEFENSIVE:
			x = 1;
			y = 1;
			z = 1;
			break;
		default:
			break;
		}
		
		// numSightedSoldiers * x - distance * goalDifesa / y + numMyFreeSoldier * z
		//          2/3				  10/20		                      5/6
		
		//TODO: Un numero che che rappresneti il livelo dovrebbe essere messo altrove, visto che sarà utilizzato spesso
		int defence = 1;
		if(goalLevels.getDefence().equals(GoalPriority.LOW))
			defence = 1;
		else if(goalLevels.getDefence().equals(GoalPriority.MEDIUM))
			defence = 2;
		else if(goalLevels.getDefence().equals(GoalPriority.HIGH))
			defence = 3;
		
		int heuristic = numSightedSoldiers*x - distance*defence/y + numMyFreeSoldier*z;
		System.out.println("CALCOLO EURISTICA, VALORE: " + heuristic + "\n" +
				"----------------- End OnEnemySighting ----------------\n");
	}
	
	@Override
	protected void handleNotification(Notification n) {
		super.handleNotification(n);
		if (n.getSubject().equals(Notification.ENEMY_SIGHTED)) {
			EnemySighting e = (EnemySighting) n.getContentObject();
			// forward notification to masterAi
			sendNotification(n.getSubject(), n.getContentObject(), getMasterAID());
			// decide what to do
			onEnemySighting(e);
		}
	}

	@Override
	protected Object handleRequest(String requestSubject) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
