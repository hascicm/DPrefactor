package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class BeePathSearchStrategy extends PathSearchStrategy {
	
	private static int NUM_ITER = 200;
	private static int NUM_BEES = 80; 
	private static int NUM_EMPLOYED_BEES = 40;
	private static int NUM_ONLOOKER_BEES = 40;
	private static int SCOUT_MAX_DEPTH = 45;
	private static int PATCH_SIZE = 3;
	private List<Bee> bees;
	
	public BeePathSearchStrategy(RelationCreator relationCreator) {
		super(relationCreator);
		this.bees = new ArrayList<Bee>();
	}

	@Override
	public List<Relation> findPath(State rootState, int depth) {
		
		List<Bee> employeeBees = new ArrayList<Bee>();
		List<Bee> onlookerBees = new ArrayList<Bee>();
		List<Bee> fittestBees = new ArrayList<Bee>();
		List<Bee> remainingBees = new ArrayList<Bee>();
		
		this.init(rootState, depth);
		
		this.initPopulation(rootState);
		
		for(int i = 0; i < NUM_ITER; i++){
			
			this.evaluatePopulation(this.bees);
			
			Collections.sort(this.bees);
			//DEBUG
			//printBees(bees);
			//printBestBee(bees.get(0));
			/*if(bees.get(0).getHeuristic() >= 564.0){
				break;
			}*/
			//DEBUG
			
			//best <num> of states (employed bees)
			employeeBees.clear();
			for(int j = 0; j < NUM_EMPLOYED_BEES; j++){
				employeeBees.add(this.bees.get(j));
			}
			
			//others - onlookers
			onlookerBees.clear();
			for(int j = NUM_EMPLOYED_BEES; j < NUM_BEES; j++){
				onlookerBees.add(bees.get(j));
			}
			
			//calculate the probability for each employed bee - je to nevynutne? 
			this.calculateProbabilityForRecruit(employeeBees);
			
			//Recruit Bees for selected space exploit the employed bees (with the onlookers)
			int numOfRecruitBees = 0;
			for(Bee b : employeeBees){
				b.getRecruitedBees().clear();
								
				Bee tempBee = null;
				for(int j = 0; j < b.getNumForRecruit(); j++){
					tempBee = onlookerBees.get(numOfRecruitBees++);
					exploreSpace(tempBee, b.visitedState, PATCH_SIZE);
					b.getRecruitedBees().add(tempBee);
				}
			}
			
			//select the fittes Bee form Each State
			fittestBees.clear();
			remainingBees.clear();
			for(Bee b : employeeBees){
				//for comparison add self
				b.getRecruitedBees().add(b);
				
				Collections.sort(b.getRecruitedBees());
				//fittest bee on index O
				fittestBees.add(b.getRecruitedBees().get(0));
				
				//others go to remaining bees
				for(int j = 1; j < b.getRecruitedBees().size(); j++){
					remainingBees.add(b.getRecruitedBees().get(j));
				}
			}
			
			//Remaining bees are scouts and create random explore
			for(Bee b : remainingBees){
				exploreSpace(b, rootState, SCOUT_MAX_DEPTH);
			}
			
			//assign new population
			this.bees.clear();
			
			for(Bee b : fittestBees){
				this.bees.add(b);
			}
			
			for(Bee b : remainingBees){
				this.bees.add(b);
			}
		}
		
		Collections.sort(this.bees);
		
		List<Relation> results = new ArrayList<Relation>();
		State currentState = this.bees.get(0).getVisitedState();
		while(currentState.getSourceRelation() != null){
			results.add(currentState.getSourceRelation());
			currentState = currentState.getSourceRelation().getFromState();
		}
		
		Collections.reverse(results);
		
		//DEBUG
		System.out.println("");
		System.out.println("RESULT");
		for(Relation r : results){
			System.out.println("-------------");
			currentState = r.getFromState();
			System.out.println("S_" + currentState.getId()+ " [ Fitness: " + currentState.getFitness() + ", NumOfSmells: " +currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] " + currentState);
			System.out.println(r.getUsedRepair().getName() + " P: " + r.getProbability());
			currentState = r.getToState();
			System.out.println("S_" + currentState.getId()+ " [ Fitness: " + currentState.getFitness() + ", NumOfSmells: " +currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] " + currentState);
		}
		System.out.println(currentState);
		//DEBUG
		
		return results;
	}

	private void calculateProbabilityForRecruit(List<Bee> employeeBees) {
		double sum = 0.0;
		for(Bee b : employeeBees){
			sum += b.getHeuristic();
		}
		
		for(Bee b : employeeBees){
			b.setProbabilityToRecruit(b.getHeuristic()/sum);
			
			//25% for 2 recruited bee
			if(employeeBees.indexOf(b) < employeeBees.size()/4){
				b.setNumForRecruit(2);
			}else{
				
				if(employeeBees.indexOf(b) < ((employeeBees.size()/4) * 3)){
					b.setNumForRecruit(1);
				}else{
					b.setNumForRecruit(0);
				}
				
			}
			
		}		
	}
	
	private void checkLowProbability(Bee bee){
		if(isLowProbability(bee.getVisitedState())){
			bee.heuristic *= (-1.0);
		}
	}

	private void evaluatePopulation(List<Bee> bees) {
		for(Bee b: bees){
			b.setHeuristic(this.calculateHeuristic(b.getVisitedState().getSourceRelation()));
			checkLowProbability(b);
		}	
	}
	
	private void initPopulation(State rootState) {	
		
		//init of one bee which find the local maximum
		Bee bee = new Bee();
		/*this.initSuperBee(bee, rootState);
		this.bees.add(bee);*/ 
				
		for(int i = 0; i < NUM_BEES; i++){	
			bee = new Bee();
			exploreSpace(bee, rootState, SCOUT_MAX_DEPTH);
			this.bees.add(bee);
		}
	}
	
	private void initSuperBee(Bee b, State s){
		
		init(s, 0);
		
		this.queue = new PriorityQueue<GraphRelation>();
		expandCurrentState(s);
		addRelationsToQueue(s.getRelations());
		
		this.localMaximum = s;
		
		Relation currentRelation;
		State currentState;
		while(!this.queue.isEmpty()){
			
			//get next state for visiting
			currentRelation = this.queue.remove().getRelation();
			currentState = currentRelation.getToState();
		
			if(isLowProbability(currentState)){
				continue;
			}
			
			if(this.localMaximum.getFitness() < currentState.getFitness()){
				this.localMaximum = currentState;
			}
			
			//remove last relations
			this.queue.clear();
			if(currentState.getDepth() < this.SCOUT_MAX_DEPTH){
				expandCurrentState(currentState);
				addRelationsToQueue(currentState.getRelations());
			}
		}
		
		b.setVisitedState(localMaximum);
	}
	
	private void exploreSpace(Bee bee, State state, int depth){
		
		Random random = new Random();
		State bestFoundState = state;
		
		//random number between 1 - maxDepth
		int numOfHops = random.nextInt(depth) + 1;
				
		State currentState = state;
		int indexOfSelectedRelation;
		for(int i = 0; i < numOfHops; i++){
			
			expandCurrentState(currentState);
			
			List<Relation> relations = currentState.getRelations();
			
			relations = filterLowProbabilityRelations(relations);
			
			if(relations.size() == 0){
				break;
			}
						
			indexOfSelectedRelation = random.nextInt(relations.size()); 
			
			currentState = relations.get(indexOfSelectedRelation).getToState();
			
			if(bestFoundState.getFitness() < currentState.getFitness()){
				bestFoundState = currentState;
			}
		}
		
		bee.setVisitedState(bestFoundState);
	}
	
	
	private List<Relation> filterLowProbabilityRelations(List<Relation> relations) {
		
		List<Relation> result = new ArrayList<Relation>();
		
		for(Relation rel : relations){
			if(!isLowProbability(rel.getToState())){
				result.add(rel);
			}
		}
		
		return result;
	}

	private void exploreSpaceOnLooker(Bee b, State s){
		/*
		Random random = new Random();
		
		expandCurrentState(s);
		
		List<Relation> relations = filterLowProbabilityRelations(s.getRelations());
		
		
		
		
		
		Relation currentRelation;
		State currentState;
		int actualDepth = s.getDepth();
		
		if(this.queue == null){
			this.queue = new PriorityQueue<GraphRelation>();
		}
			
			
		this.queue.clear();
		
		expandCurrentState(s);
		addRelationsToQueue(s.getRelations());
		
		this.localMaximum = s;
		
		while(!this.queue.isEmpty()){
			
			//get next state for visiting
			currentRelation = this.queue.remove().getRelation();
			currentState = currentRelation.getToState();
		
			if(this.localMaximum.getFitness() < currentState.getFitness()){
				this.localMaximum = currentState;
			}
			
			if(currentState.getDepth() < (actualDepth + this.PATCH_SIZE)){
				expandCurrentState(currentState);
				addRelationsToQueue(currentState.getRelations());
			}
			
		}
		
		b.setVisitedState(this.localMaximum);
		*/
	}
		
	private class Bee implements Comparable<Bee>{
		
		State visitedState;
		double heuristic;
		double probabilityToRecruit;
		int numForRecruit; 
		List<Bee> recruitedBees = new ArrayList<Bee>();

		public State getVisitedState() {
			return visitedState;
		}

		public void setVisitedState(State visitedState) {
			this.visitedState = visitedState;
		}

		public double getHeuristic() {
			return heuristic;
		}

		public void setHeuristic(double heuristic) {
			this.heuristic = heuristic;
		}

		@Override
		public int compareTo(Bee o) {
			return Double.compare(o.getHeuristic(), this.getHeuristic());
		}

		public double getProbabilityToRecruit() {
			return probabilityToRecruit;
		}

		public void setProbabilityToRecruit(double probabilityToRecruit) {
			this.probabilityToRecruit = probabilityToRecruit;
		}

		public List<Bee> getRecruitedBees() {
			return recruitedBees;
		}

		public void setRecruitedBees(List<Bee> recruitedBees) {
			this.recruitedBees = recruitedBees;
		}

		public int getNumForRecruit() {
			return numForRecruit;
		}

		public void setNumForRecruit(int numForRecruit) {
			this.numForRecruit = numForRecruit;
		}
				
	}

	private void printBees(List<Bee> bees){
		for(Bee b : bees){
				if(b.getVisitedState().getSourceRelation() != null)
					System.out.println((b.getHeuristic()) + "[P:"+b.getVisitedState().getSourceRelation().getProbability()+"], ");
		}
	}
	
	private void printBestBee(Bee b){
		System.out.println(b.getHeuristic());
	}
}
