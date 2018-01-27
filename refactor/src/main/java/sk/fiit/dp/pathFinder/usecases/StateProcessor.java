package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sk.fiit.dp.pathFinder.entities.Dependency;
import sk.fiit.dp.pathFinder.entities.DependencyPlaceType;
import sk.fiit.dp.pathFinder.entities.DependencyRepair;
import sk.fiit.dp.pathFinder.entities.DependencyType;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.pathFinder.entities.Repair;
import sk.fiit.dp.pathFinder.entities.SmellType;
import sk.fiit.dp.pathFinder.entities.stateSpace.PatternRelation;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class StateProcessor {

	public static State applyRepair(Relation rel) {
		
		State baseState = rel.getFromState();
		Repair repair = rel.getUsedRepair();
		SmellOccurance smellOccurance = rel.getFixedSmellOccurance();
		State resultState = null;
		
		if(rel instanceof Relation ){
			resultState = applyBasicRepair(baseState, repair, smellOccurance);
	
			// TODO preprobit na repair.applyOnState() s vyuzitim override
			if (repair instanceof DependencyRepair) {
				applyDependencies(resultState, (DependencyRepair) repair, smellOccurance);
			}	
		}else if(rel instanceof PatternRelation){
			resultState = applyPatternRelation((PatternRelation)rel);
		}
			
		return resultState;
	}

	public static State applyRepairMonteCarlo(State baseState, Repair repair, SmellOccurance smellOccurance) {

		State resultState = applyBasicRepairMonteCarlo(baseState, repair, smellOccurance);

		// TODO preprobit na repair.applyOnState() s vyuzitim override
		if (repair instanceof DependencyRepair) {
			applyDependencies(resultState, (DependencyRepair) repair, smellOccurance);
		}

		return resultState;
	}

	// for the simply repair without dependency just remove smell occurance
	private static State applyBasicRepair(State baseState, Repair repair, SmellOccurance smellOccurance) {

		State resultState = new State();
		
		List<SmellOccurance> smellOccuranceList = new ArrayList<SmellOccurance>(baseState.getSmells());
		
		smellOccuranceList.remove(smellOccurance);

		/*for (SmellOccurance so : baseState.getSmells()) {
			if (so != smellOccurance) {
				smellOccuranceList.add(so);
			}
		}*/
		
		resultState.setSmells(smellOccuranceList);
		
		return resultState;
	}

	// for the simply repair without dependency just remove smell occurance
	private static State applyBasicRepairMonteCarlo(State baseState, Repair repair, SmellOccurance smellOccurance) {

		State resultState = State.getMonteCarloStateInstance();

		List<SmellOccurance> smellOccuranceList = new ArrayList<SmellOccurance>();

		for (SmellOccurance so : baseState.getSmells()) {
			if (so != smellOccurance) {
				smellOccuranceList.add(so);
			}
		}

		resultState.setSmells(smellOccuranceList);
		return resultState;
	}

	private static void applyDependencies(State state, DependencyRepair repair, SmellOccurance smellOccurance) {

		for (Dependency dep : repair.getDependencies()) {

			if (dep.getType() == DependencyType.CAUSE) {
				
				if(dep.getPlaceType() == DependencyPlaceType.INTERNAL){
					SmellOccurance newSmellOccurance = new SmellOccurance(dep.getSmell());
					
					List<LocationPart> newLocationPartList = new ArrayList<LocationPart>();
					
					List<LocationPart> tempLocationPartList = smellOccurance.getLocations().get(0).getLocation(); 
					boolean isFound = false;
					LocationPart currentPart = null;
					
					for(int i = tempLocationPartList.size()-1; i >= 0; i--){
					
						currentPart = tempLocationPartList.get(i);
						
						if(isFound){
						
							newLocationPartList.add(currentPart);
						
						}else{
							
							if(currentPart.getLocationPartType() == dep.getActionField()){
								isFound = true;
								newLocationPartList.add(currentPart);
							}
						}
					}
					
					List<Location> newLocations = new ArrayList<Location>();
					Collections.reverse(newLocationPartList);
					Location loc = new Location(newLocationPartList);
					newLocations.add(loc);
					
					newSmellOccurance.setLocations(newLocations);
				
					state.getSmells().add(newSmellOccurance);
				}
			}

			if (dep.getType() == DependencyType.SOLVE) {
				
				if(dep.getPlaceType() == DependencyPlaceType.INTERNAL){
					SmellOccurance tempSmellOccurance = isOnSameLocation(state, smellOccurance, dep.getSmell(), dep.getActionField());
					/*boolean isSolved = false;
	
					for (SmellOccurance smellOccurance : state.getSmells()) {
						if (smellOccurance.getSmell() == dep.getSmell()) {
	
							tempSmellOccurance = smellOccurance;
							isSolved = true;
							break;
						}
					}
	
					if (isSolved) {
						state.getSmells().remove(tempSmellOccurance);
					}*/
					if(tempSmellOccurance != null){
						state.getSmells().remove(tempSmellOccurance);
					}
				
				}
				
				if(dep.getPlaceType() == DependencyPlaceType.EXTERNAL){
					//TODO!!!!
				}

			}
		}
	}

	public static long calculateSmellsWeight(State state) {
		long result = 0;

		for (SmellOccurance so : state.getSmells()) {
			result += so.getSmell().getWeight();
		}

		return result;
	}

	public static void calculateFitness(State state, long initSmellsWeight) {

		long fitness = 0;

		fitness += (initSmellsWeight - calculateSmellsWeight(state));

		fitness = fitness << 2;

		Relation currentRel = state.getSourceRelation();
		int count = 0;
		int weightSum = 0;
		while (currentRel != null) {
			weightSum += currentRel.getUsedRepair().getWeight(currentRel.getFixedSmellOccurance().getSmell());
			count++;
			currentRel = currentRel.getFromState().getSourceRelation();
		}

		if (count != 0)
			fitness += (weightSum / count);

		fitness -= state.getDepth();

		/*
		 * if(state.getSourceRelation() != null){ fitness *=
		 * state.getSourceRelation().getProbability(); }
		 */

		// fitness = (state.getDepth());
		state.setFitness(fitness);
	}

	/*
	 * public static void calculateFitness(State state){
	 * 
	 * int fitness = 0;
	 * 
	 * for(SmellOccurance smellOccurance : state.getSmells()){ fitness +=
	 * smellOccurance.getSmell().getWeight(); }
	 * 
	 * fitness *= 10; fitness = (int) Math.pow(fitness, 3.0);
	 * 
	 * fitness += state.getDepth();
	 * 
	 * State currentState = state; while(currentState.getSourceRelation() !=
	 * null){ fitness +=
	 * currentState.getSourceRelation().getUsedRepair().getWeight(currentState.
	 * getSourceRelation().getFixedSmellOccurance().getSmell()); currentState =
	 * currentState.getSourceRelation().getFromState(); }
	 * 
	 * state.setFitness(fitness); }
	 */

	public static void calculateFitnessForAnts(State state) {
		int fitness = 0;
		float fit = 0;
		for (SmellOccurance smellOccurance : state.getSmells()) {
			fitness += smellOccurance.getSmell().getWeight() * 2;
		}
		if (fitness == 0) {
			fitness = 1;
		}
		fit = 1 / (float) fitness;
		fitness = ((int) (fit * 10000));

		State currentState = state;
		while (currentState.getSourceRelation() != null) {
			fitness -= (currentState.getSourceRelation().getUsedRepair()
					.getWeight(currentState.getSourceRelation().getFixedSmellOccurance().getSmell()) * 5);
			currentState = currentState.getSourceRelation().getFromState();
		}
		state.setFitness(fitness);
	}

	public static void initializeState(State state) {
		for (Relation r : state.getRelations()) {
			r.setPheromoneTrail(2000);
		}
	}

	public static String createHash(State s) {

		StringBuilder sb = new StringBuilder();

		for (SmellOccurance so : s.getSmells()) {
			sb.append(so.getSmell().getId() + "_");
			for(LocationPart loc : so.getLocations().get(0).getLocation()){
				sb.append(loc.getId()+ "_" + loc.getLocationPartType() + "_" );
			}
		}

		return sb.toString();
	}
	
	public static SmellOccurance isOnSameLocation(State state, SmellOccurance baseSmellOccurance, SmellType smellType, LocationPartType placeType){
		
		SmellOccurance result = null;
		List<LocationPart> tempPath;
		
		for(SmellOccurance smellOccurance : state.getSmells()){
			
			if(smellOccurance != baseSmellOccurance){
				
				if(smellOccurance.getSmell() == smellType){
					
					tempPath = PlaceComparator.findCommonDestinationPath(baseSmellOccurance.getLocations().get(0).getLocation(), 
																			smellOccurance.getLocations().get(0).getLocation());
					
					for(int i = tempPath.size()-1; i >=0; i-- ){
						
						if(tempPath.get(i).getLocationPartType() == placeType){
							result = smellOccurance;
							break;
						}

					}
				}
				
			}
		}
		
		
		return result; 
	}
	
	private static State applyPatternRelation(PatternRelation rel){
		
		State resultState = null;
		
		return resultState; 
	}
	
}
