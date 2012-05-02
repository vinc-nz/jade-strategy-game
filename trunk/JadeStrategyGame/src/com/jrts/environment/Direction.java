package com.jrts.environment;

import java.util.Random;

public enum Direction{
	RIGHT(0,1),
	LEFT(0,-1),
	UP(-1,0),
	DOWN(1,0),
	RIGHT_UP(-1,1),
	RIGHT_DOWN(1,1),
	LEFT_UP(-1,-1),
	LEFT_DOWN(1,-1);
	
	
	public static Direction[] ALL = {DOWN, LEFT, RIGHT, UP,
				LEFT_DOWN, LEFT_UP, RIGHT_DOWN, RIGHT_UP, };
	
	public static Direction randomDirection(){
		Random r = new Random();
		return ALL[r.nextInt(ALL.length)];
	}
	
	private int rowVar;
	private int colVar;
	
	Direction(int rowVar, int colVar){
		this.rowVar = rowVar;
		this.colVar = colVar;
	}
	
	public int rowVar(){
		return rowVar;
	}
	
	public int colVar(){
		return colVar;
	}
}