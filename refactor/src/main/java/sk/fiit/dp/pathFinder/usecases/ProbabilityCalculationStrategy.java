package sk.fiit.dp.pathFinder.usecases;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;

public abstract class ProbabilityCalculationStrategy {
	abstract public double calculateProbability(Relation rel); 
}
