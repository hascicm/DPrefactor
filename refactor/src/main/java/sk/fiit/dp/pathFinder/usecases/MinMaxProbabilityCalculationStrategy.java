package sk.fiit.dp.pathFinder.usecases;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;

public class MinMaxProbabilityCalculationStrategy extends ProbabilityCalculationStrategy {

	@Override
	public double calculateProbability(Relation rel) {
		
		double probability = rel.getUsedRepair().calculateProbability();
		
		if(!(rel.getFromState().getSourceRelation() == null)){
			if(rel.getFromState().getSourceRelation().getProbability() < probability){
				probability = rel.getFromState().getSourceRelation().getProbability();			
			}
		}
		
		return probability;
	}

}
