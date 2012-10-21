package com.jrts.logic;

import java.util.ArrayList;
import java.util.HashMap;
import com.jrts.common.GameConfig;
import com.jrts.environment.CellType;
import com.jrts.environment.Direction;
import com.jrts.environment.Floor;
import com.jrts.environment.Hit;
import com.jrts.environment.Position;
import com.jrts.environment.World;

public class AttacksManager {

	private static ArrayList<Hit> hits;
	private static int counter;
	
	static {
		hits = new ArrayList<Hit>();
	}

	public synchronized static void addHit(Position pos, Direction dir, int damage){
		Hit hit = new Hit(pos, dir, damage);
		//Eseguo uno spostamento per evitare che il colpo danneggi l'unita'� sorgente stessa
		hit.step();
		hits.add(hit);
//		System.out.println("Added new hit" + hits.size());
	}
	
	public synchronized static void update() {
		if(counter++ < GameConfig.ATTACKS_REFRESH)
			return;
		counter = 0;
		
		//refresh hit's positions
		for (int i = 0; i < hits.size(); i++){
			Hit hit = (Hit) hits.get(i);
			hit.step();
			//disable hit which exceed floor's bound
			if(!hit.respectLimits(World.getInstance().getRows(), World.getInstance().getCols()))
				hit.setEnabled(false);
		}
		
		//remove disabled hits
		for (int i = 0; i < hits.size(); i++)
			if(!hits.get(i).isEnabled()){
				hits.remove(i);
//				System.out.println("Removed hit");
			}
		
		//check if there is some collision
		for (int i = 0; i < hits.size(); i++){
			Position hp = hits.get(i).getPos();
			Floor floor = World.getInstance().getFloor();
			if(floor.get(hp).getType() != CellType.FREE){
				System.out.println("Detected collision");
				Hit hit = hits.remove(i);
				Position pos = hit.getPos();
				int damage = hit.getDamage();
				if(floor.get(hp).getType() == CellType.UNIT)
					World.getInstance().getCell(pos).getUnit().decreaseLife(damage);
			}
		}
	}

	public synchronized static boolean isThereAnHit(int row, int col) {
		for (int i = 0; i < hits.size(); i++)
			if(hits.get(i).getPos().equals(new Position(row, col)))
				return true;
		return false;
	}
}