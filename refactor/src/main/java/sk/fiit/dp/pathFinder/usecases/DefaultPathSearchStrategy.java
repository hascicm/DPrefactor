package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;


public class DefaultPathSearchStrategy extends PathSearchStrategy{
		
	private final int MAX_DEPTH = 10;
	
	public DefaultPathSearchStrategy(RelationCreator relationCreator) {
		super(relationCreator);
	}
	
	
	@Override
	public List<Relation> findPath(State rootState, int depth) {
			
		init(rootState, depth);
		
		start(rootState);
		
		//RESULT
		List<Relation> results = new ArrayList<Relation>();
		
		State currentState = this.localMaximum;
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
			System.out.println(r.getUsedRepair().getName() + " -> " + r.getFixedSmellOccurance().getSmell().getName() + " P: " + r.getProbability());
			currentState = r.getToState();
			System.out.println("S_" + currentState.getId()+ " [ Fitness: " + currentState.getFitness() + ", NumOfSmells: " +currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] " + currentState);
		}
		System.out.println(currentState);
		//DEBUG		
		return results;
	}


	protected void start(State rootState) {
		
		this.lastStateId = 0;
		// add relations from rootState to queue
		this.addRelationsToQueue(rootState.getRelations());
		
		Relation currentRelation = null;
		State currentState = null;
		
		
		
		while(!this.queue.isEmpty()){
					
			//get next state for visiting
			currentRelation = this.queue.remove().getRelation();
			currentState = currentRelation.getToState();
						
			//Skip the state contains same smells as any of visited state (node)
			if(isVisited(currentState)){
				continue;
			}
			
			if(isLowProbability(currentState)){
				continue;
			}
			
			currentState.setId(lastStateId++); 
						
			//if currentState is better then local maximum
			if(this.localMaximum.getFitness() < currentState.getFitness()){	
				this.localMaximum = currentState;
			}
						
			if(currentState.getDepth() < MAX_DEPTH){
				expandCurrentState(currentState);			
			}
			
			//DEBUG
				if(this.localMaximum.getSmells().size() == 0){
					break;
				}
			//DEBUG
			
		}
		//System.out.println(count);
		//System.out.println(lastStateId);
	}	

	protected void init(State rootState, int depth) {
		super.init(rootState, depth);
		
		// init queue
		this.queue = new PriorityQueue<GraphRelation>();

		// init visited state
		this.visitedStates = new HashSet<String>();
		this.visitedStates.add(StateProcessor.createHash(rootState));
		this.localMaximum = rootState;
		
		//DEBUG
			//printAllRelation(rootState);
		//DEBUG
		
	}
		
	protected void expandCurrentState(State currentState){
		
		super.expandCurrentState(currentState);
		
		this.visitedStates.add(StateProcessor.createHash(currentState));
		
		//add just created relations to queue
		this.addRelationsToQueue(currentState.getRelations());
		
		//DEBUG
		//printAllRelation(currentState);
		//DEBUG
		
	}
	
	protected static void printAllRelation(State s){
		System.out.println();
		System.out.println("NEXT EXPAND:");
		System.out.println();
		
		for(Relation r : s.getRelations()){
			System.out.println(r.getFixedSmellOccurance().getSmell().getName() + " ["+printSmellLocations(r.getFixedSmellOccurance()) +"] : " + r.getUsedRepair().getName() + "[" + r.getProbability()+ "]" +" > Fitness: " + r.getToState().getFitness());
			System.out.println("SMELLS:");
			printAllSmells(r.getToState().getSmells());
			
			System.out.println();
			System.out.println("----------------------------------");
			System.out.println();
		}
	}
	
	private static void printAllSmells(List<SmellOccurance> smells){
		for(SmellOccurance s : smells){
			System.out.println(s.getSmell().getName() + " ["+printSmellLocations(s) + "]");
		}
	}
	
	private static String printSmellLocations(SmellOccurance so){
		
		StringBuilder sb = new StringBuilder();
		
		for(LocationPart lp : so.getLocations().get(0).getLocation()){
			sb.append(lp.getId() + " <" + lp.getLocationPartType()+ "> /");
		}
		
		return sb.toString();
	}
}
