package sk.fiit.dp.pathFinder.entities.stateSpace;

import sk.fiit.dp.pathFinder.entities.Repair;
import sk.fiit.dp.pathFinder.usecases.ProbabilityCalculationStrategy;

public class Relation {
	
	private State fromState;
	private State toState;
	private SmellOccurance fixedSmellOccurance;
	private Repair usedRepair;

	private int pheromoneTrail;
	private double probability;
	
	public int getPheromoneTrail() {
		return pheromoneTrail;
	}
	public void setPheromoneTrail(int pheromoneTrail) {
		this.pheromoneTrail = pheromoneTrail;
	}
	public State getFromState() {
		return fromState;
	}
	public void setFromState(State fromState) {
		this.fromState = fromState;
	}
	public State getToState() {
		return toState;
	}
	public void setToState(State toState) {
		this.toState = toState;
	}
	public SmellOccurance getFixedSmellOccurance() {
		return fixedSmellOccurance;
	}
	public void setFixedSmellOccurance(SmellOccurance fixedSmellOccurance) {
		this.fixedSmellOccurance = fixedSmellOccurance;
	}
	public Repair getUsedRepair() {
		return usedRepair;
	}
	public void setUsedRepair(Repair usedRepair) {
		this.usedRepair = usedRepair;
	}
	
	public void calculateProbability(ProbabilityCalculationStrategy calculationStrategy){
		
		/*double probability = this.usedRepair.calculateProbability();
		
		if(this.getFromState().getSourceRelation() == null){
			this.probability = probability;
		}else{
			this.probability = probability * this.getFromState().getSourceRelation().probability;
		}*/
		
		this.probability = calculationStrategy.calculateProbability(this);
	}
	
	public double getProbability() {
		return probability;
	}
	
	public String toString(){
		return this.getUsedRepair().getName() + " -> " + this.getFixedSmellOccurance().getSmell().getName() + " P: " + this.getProbability();
	}
}
