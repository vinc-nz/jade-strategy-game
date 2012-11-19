package com.jrts.scorer;

import java.util.LinkedList;
import java.util.List;

import com.jrts.agents.MasterAI;
import com.jrts.common.GoalPriority;

public class GoalScorer {

	public interface Rule {
		double value();
	}

	public static final double MIN_SCORE = 0;
	public static final double MAX_SCORE = 100;

	List<Rule> rules = new LinkedList<GoalScorer.Rule>();

	MasterAI masterAI;

	

	public GoalScorer(MasterAI masterAI) {
		super();
		this.masterAI = masterAI;
	}

	public void addRule(Rule r) {
		rules.add(r);
	}

	public GoalPriority calculatePriority() {
		double score = 0;
		for (Rule r : rules) {
			double localScore = r.value();
			score += (localScore < MIN_SCORE ? 0 : localScore > MAX_SCORE
					|| Double.isNaN(localScore) ? MAX_SCORE : localScore);
		}
		score /= rules.size();
		if (score < MAX_SCORE / 3)
			return GoalPriority.LOW;
		else if (score < 2 * MAX_SCORE / 3)
			return GoalPriority.MEDIUM;
		return GoalPriority.HIGH;
	}

}
