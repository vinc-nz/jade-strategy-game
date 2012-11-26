package com.jrts.common;

/**
 * An anget status identifies what a unit is currently doing.
 * It is also used by high-level AI to give unit orders
 * @author spax
 *
 */
public class AgentStatus {
	
	public static final String FREE = "free";

	public static final String WOOD_CUTTING = "wood-cutting";
	public static final String FOOD_COLLECTING = "food-collecting";

	public static final String PATROLING = "patroling";
	public static final String EXPLORING = "exploring";

	public static final String GO_UPGRADING = "go_upgrading";
	public static final String GO_FIGHTING = "go_fighting";
	public static final String FIGHTING = "fighting";

}
