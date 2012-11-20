package com.jrts.agents;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

import com.jrts.O2Ainterfaces.Team;
import com.jrts.common.GameConfig;
import com.jrts.common.GoalPriority;
import com.jrts.common.ResourcesContainer;
import com.jrts.common.UnitFactory;
import com.jrts.common.Utils;
import com.jrts.environment.Cell;
import com.jrts.environment.CellType;
import com.jrts.environment.MasterPerception;
import com.jrts.environment.Perception;
import com.jrts.environment.Position;
import com.jrts.environment.World;
import com.jrts.environment.WorldMap;
import com.jrts.messages.EnemySighting;
import com.jrts.messages.GoalLevels;
import com.jrts.messages.MessageSubject;
import com.jrts.messages.Notification;
import com.jrts.scorer.AttackScorer;
import com.jrts.scorer.DefenceScorer;
import com.jrts.scorer.ExplorationScorer;
import com.jrts.scorer.ResourceScorer;

@SuppressWarnings("serial")
public class MasterAI extends JrtsAgent implements Team {

	public enum Nature {
		AGGRESSIVE, AVERAGE, DEFENSIVE
	}

	AID resourceAID, militaryAID;

	final Nature nature;
	GoalLevels goalLevels;

	ResourceScorer resourceScorer;
	AttackScorer attackScorer;
	DefenceScorer defenceScorer;
	ExplorationScorer explorationScorer;

	UnitFactory unitFactory;
	
	MasterPerception masterPerception;

	public MasterAI() {
		registerO2AInterface(Team.class, this);
		int die = Utils.random.nextInt(3);
		nature = die == 0 ? Nature.AGGRESSIVE : die == 1 ? Nature.AVERAGE
				: Nature.DEFENSIVE;
	}

	protected void setup() {
		super.setup();
		World world = World.getInstance();
		masterPerception.setWorldMap(new WorldMap(world.getRows(), world.getCols()));
		setTeamName(getAID().getLocalName());

		masterPerception.setCityCenter((Position) getArguments()[0]);

		resourceAID = new AID(getTeamName() + "-resourceAI", AID.ISLOCALNAME);
		militaryAID = new AID(getTeamName() + "-militaryAI", AID.ISLOCALNAME);

		PlatformController container = getContainerController();
		AgentController militaryAI, resourceAI, df;
		try {
			df = container.createNewAgent(getTeamDF().getLocalName(),
					"jade.domain.df", null);
			df.start();
			
			masterPerception.setTeamDF(getTeamDF());

			unitFactory = new UnitFactory(getTeamName(),
					getContainerController(), masterPerception.getCityCenter());
			unitFactory.start();

			masterPerception.setResourcesContainer(new ResourcesContainer(
					GameConfig.STARTUP_WOOD, GameConfig.STARTUP_FOOD));

			Object[] arg = { getTeamName(), unitFactory, masterPerception.getResourcesContainer(),
					masterPerception.getCityCenter(), nature };

			resourceAI = container.createNewAgent(resourceAID.getLocalName(),
					ResourceAI.class.getName(), arg);
			resourceAI.start();
			militaryAI = container.createNewAgent(militaryAID.getLocalName(),
					MilitaryAI.class.getName(), arg);
			militaryAI.start();

		} catch (ControllerException e) {
			e.printStackTrace();
		}

		setDefaultGoals();

		this.attackScorer = new AttackScorer(masterPerception);
		this.defenceScorer = new DefenceScorer(masterPerception);
		this.explorationScorer = new ExplorationScorer(masterPerception);
		this.resourceScorer = new ResourceScorer(masterPerception);

		addBehaviour(new TickerBehaviour(this, GameConfig.GOALS_UPDATE) {

			@Override
			protected void onTick() {
				goalLevels.setAttack(attackScorer.calculatePriority());
				goalLevels.setDefence(defenceScorer.calculatePriority());
				goalLevels
						.setExploration(explorationScorer.calculatePriority());
				goalLevels.setResources(resourceScorer.calculatePriority());
				notifyGoalChanges();
			}
		});
	}

	public void setDefaultGoals() {
		//just for initialization
		//they will change soon
		goalLevels = new GoalLevels(GoalPriority.LOW, GoalPriority.LOW,
				GoalPriority.LOW, GoalPriority.LOW);
		notifyGoalChanges();
	}

	@Override
	protected void updatePerception() {
		World world = World.getInstance();
		// check if city center was destroyed
		Cell cityCenterCell = world.getCell(masterPerception.getCityCenter());
		logger.log(
				logLevel,
				getTeamName() + " energy: "
						+ cityCenterCell.getResourceEnergy());
		if (cityCenterCell.getResourceEnergy() <= 0) {
			logger.log(logLevel, "TEAM DELETED");
			decease();
		}
	}

	public synchronized void decease() {
		// TODO maybe send a notification to other teams (like age of empires)

		// send notification of decease to military an resource ai (they will
		// forward it to their units)
		sendNotification(Notification.TEAM_DECEASED, null, militaryAID);
		sendNotification(Notification.TEAM_DECEASED, null, resourceAID);

		// clean the cell of citycenter in the floor
		World.getInstance().removeTeam(getTeamName());

		// wait for resource,military and units death messages and then delete
		// the agent
		removeAllBehaviours();
		this.doDelete();
	}

	/** O2A methods (for use in non-agent java objects) */

	@Override
	public int getFood() {
		return masterPerception.getResourcesContainer().getFood();
	}

	@Override
	public int getWood() {
		return masterPerception.getResourcesContainer().getWood();
	}

	@Override
	public String getTeamName() {
		return getLocalName();
	}

	private void notifyGoalChanges() {
		sendNotification(Notification.GOAL_LEVELS, goalLevels, militaryAID);
		sendNotification(Notification.GOAL_LEVELS, goalLevels, resourceAID);
	}

	@Override
	public WorldMap getWorldMap() {
		return masterPerception.getWorldMap();
	}

	@Override
	protected void handleNotification(Notification n) {
		if (n.getSubject().equals(Notification.PERCEPTION)) {
			Perception info = (Perception) n.getContentObject();
			masterPerception.getWorldMap().update(info);

		} else if (n.getSubject().equals(Notification.ENEMY_SIGHTED)) {
			EnemySighting e = (EnemySighting) n.getContentObject();
			// TODO save it

		} else if (n.getSubject().equals(Notification.NO_MORE_RESOURCE)) {
			CellType resourceType = (CellType) n.getContentObject();
			// TODO save it
		}
	}

	@Override
	protected Object handleRequest(String requestSubject) {
		if (requestSubject.equals(MessageSubject.GET_CITY_CENTER_POSITION))
			return masterPerception.getCityCenter();
		else if (requestSubject.equals(MessageSubject.GET_WORLD_MAP))
			return masterPerception.getWorldMap();
		return null;
	}

	public ResourcesContainer getResourcesContainer() {
		return masterPerception.getResourcesContainer();
	}

	@Override
	public int getEnergy() {
		Position cityCenter = masterPerception.getCityCenter();
		return World.getInstance().getCell(cityCenter).getResourceEnergy();
	}
}