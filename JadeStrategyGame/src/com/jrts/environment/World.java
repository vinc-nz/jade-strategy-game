package com.jrts.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jrts.O2Ainterfaces.IUnit;
import com.jrts.common.GameConfig;
import com.jrts.common.Utils;

public class World {

	private static World instance = null;
	
	public static Level logLevel = Level.FINE;

	final Floor floor;

	private final Map<String, Position> teams;

	private final int rows;
	private final int cols;

	/**
	 * I 4 booleani rappresentano i 4 angoli della mappa, ogni qual volta la base di una squadra 
	 * occupa uno di essi, allora la relativa cella dell'array diventa true.
	 */
	private boolean[] occupiedAngles = {false, false, false, false};
	private Logger logger = Logger.getLogger(World.class.getName());
	
	public static void create(int rows, int cols, float woodPercentage) {
		instance = new World(rows, cols, woodPercentage);
	}

	public static World getInstance() {
		return instance;
	}

	private World(int rows, int cols, float woodPercentage) {
		this.rows = rows;
		this.cols = cols;
		floor = new Floor(rows, cols);

		int numWood = (int) (((float) (rows * cols)) * woodPercentage);
		Cell wood = new Cell(CellType.WOOD, GameConfig.TREE_ENERGY);
		floor.generateObject(numWood, wood);

		teams = new HashMap<String, Position>();
	}

	
	boolean doMovement(Position source, Direction d) {
		Position destination = source.step(d);
		Cell srcCell = floor.get(source);
		if (isAvailable(destination)) {
			clear(source);
			floor.set(destination, srcCell);
			return true;
		} 
		return false;
	}
	
	/**
	 * changes the position of an object using the specified direction
	 * 
	 * @param source
	 *            the position of the object
	 * @param d
	 *            the direction
	 * @return true if the movement has been performed
	 */
	public synchronized boolean move(Position source, Direction d) {
		return doMovement(source, d);
	}

	boolean addObject(Cell objectType, Position p) {
		if (isAvailable(p)) {
			floor.set(p, objectType);
			return true;
		}
		return false;
	}


	public synchronized Cell getCell(Position p) {
		return floor.getCopy(p);
	}


	/**
	 * returns a random free position near a center between the specified
	 * distance range
	 * 
	 * @param center
	 * @param minDistance
	 * @param maxDistance
	 * @return
	 */
	Position near(Position center, int minDistance, int maxDistance) {
		int maxIterations = 10;
		int len = maxDistance - minDistance + 1;
		Position candidate = null;
		do {
			int rowOffset = Utils.random.nextInt(len) + minDistance;
			int colOffset = Utils.random.nextInt(len) + minDistance;
			int rowMultiplier = (Utils.random.nextInt(2) == 0 ? -1 : 1);
			int colMultiplier = (Utils.random.nextInt(2) == 0 ? -1 : 1);
			int row = center.row + rowOffset * rowMultiplier;
			int col = center.col + colOffset * colMultiplier;
			candidate = new Position(row, col);
		} while (!isAvailable(candidate) && --maxIterations > 0);
		return candidate;
	}

	boolean isAvailable(Position p) {
		return floor.isValid(p) && floor.get(p).type == CellType.FREE;
	}
	
	public  void clear(Position p) {
		floor.set(p, new Cell(CellType.FREE));
	}

	/**
	 * adds the city center in a random position for a new team with the
	 * specified name
	 * 
	 * @param name
	 *            the name of the team, has to be unique
	 * @return 
	 */
	public Position addTeam(String name) {
		// Prendo un angolo a caso tra 0 e 3
		int angle = Utils.random.nextInt(4);

		// se l'angolo non e' occupato da un'altra squadra 
		// lo utilizzo per mettere la base della squadra corrente,
		// altrimenti ne scelgo un altro

		while(this.occupiedAngles[angle])
			angle = (angle + 1) % 4;

		this.occupiedAngles[angle] = true;
		
		Cell base = new Cell(CellType.CITY_CENTER, name);
		base.energy = GameConfig.BUILDING_ENERGY;

		//Inizializzo la var con una posizione inesistente
		Position cityCenter = new Position(-1, -1);
		
		// A seconda dell'angolo della mappa scelto e del valore della var n
		// scelgo la posizione della mappa ove posizionare la base
		int n = Utils.random.nextInt(10) + 1;
		do {
			switch (angle) {
			case 0:
				cityCenter = new Position(n, n);
				break;
			case 1:
				cityCenter = new Position((this.rows - n), n);
				break;
			case 2:
				cityCenter = new Position((this.rows - n), (this.cols - n));
				break;
			case 3:
				cityCenter = new Position(n, (this.cols - n));
				break;
			}
			
			// Genero un numero casuale che sarà utilizzato per distanziare la base
			// dalla cella di riferimento dellangolo della mappa scelto
			//startP.col += (r.nextInt(2) == 0)? r.nextInt(10) : - r.nextInt(10);
		
		} while (!addObject(base, cityCenter));
		
		teams.put(name, cityCenter);
		
		// put a food and a wood resource near the city center
		Position foodPosition = near(cityCenter, GameConfig.FOOD_MIN_DISTANCE, GameConfig.FOOD_MAX_DISTANCE);
		Cell food = new Cell(CellType.FOOD, GameConfig.FARM_ENERGY);
		addObject(food, foodPosition);
		
		Position woodPosition = near(cityCenter, GameConfig.WOOD_MIN_DISTANCE, GameConfig.WOOD_MAX_DISTANCE);
		Cell wood = new Cell(CellType.WOOD, GameConfig.TREE_ENERGY);
		addObject(wood, woodPosition);

		logger.log(logLevel, "TEAM " + name + " added in " + cityCenter.toString());
		
		return cityCenter;
	}
	
	public synchronized void removeTeam(String teamName) {
		clear(teams.get(teamName));
		teams.remove(teamName);
	}

	/**
	 * gets a free position in the neighborhood of the parameter
	 * 
	 * @param p
	 *            the position to use as center of the neighborhood
	 * @return the position available
	 */
	public synchronized Position neighPosition(Position p) {
		if (!isAvailable(p)) {
			p = floor.nextTo(p, CellType.FREE, 2);
			if (p == null)
				return null;
		}
		return p;
	}

	public synchronized void addUnit(Position p, String unitId, IUnit unit) {
		Cell unitCell = new Cell(unitId, unit, unit.getType());
		floor.set(p, unitCell);
	}

	public synchronized Floor getSnapshot() {
		return new Floor(floor);
	}
	
	/**
	 * Returns the perceived floor in a certain position with the specified
	 * range of sight
	 * 
	 * @param center
	 *            where the observer is located
	 * @param sight
	 *            the range of sight of the observer
	 * @return a floor object where the perceived cells are the same of the
	 *         world's floor and the others are set to UNKNOWN
	 */

	public synchronized Perception getPerception(Position center, int sight) {
		return new Perception(floor, center, sight);
	}

	public synchronized int takeEnergy(Position target, int amount) {
		Cell targetCell = floor.get(target);
		if (targetCell.energy >= amount) {
			targetCell.energy -= amount;
			return amount;
		} else {
			int taken = targetCell.energy;
			clear(target);
			if (targetCell.type == CellType.CITY_CENTER)
				teams.remove(targetCell.id);
			return taken;
		}
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public synchronized void killUnit(IUnit u) {
		Cell target = floor.get(u.getPosition());
		if (u.getId().equals(target.id)) {
			clear(u.getPosition());
		}
	}

	public synchronized boolean isGameFinished() {
		return teams.size() <= 1;
	}

	public synchronized void changeCell(int i, int j, Cell cell) {
		floor.set(i,j,cell);
	}

	
}