package sk.fiit.dp.pathFinder.usecases.stateProcessing;

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
import sk.fiit.dp.pathFinder.entities.PatternSmellUse;
import sk.fiit.dp.pathFinder.entities.Repair;
import sk.fiit.dp.pathFinder.entities.SmellType;
import sk.fiit.dp.pathFinder.entities.stateSpace.PatternRelation;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.pathFinder.usecases.helpers.PlaceComparator;

public class StateProcessor {

	public static State applyRepair(Relation rel) {

		State resultState = null;

		if (!(rel instanceof PatternRelation)) {

			Repair repair = rel.getUsedRepair();
			SmellOccurance smellOccurance = rel.getFixedSmellOccurance();
			State baseState = rel.getFromState();

			if (rel instanceof Relation) {
				resultState = applyBasicRepair(baseState, repair, smellOccurance);

				if (repair instanceof DependencyRepair) {
					applyDependencies(resultState, (DependencyRepair) repair, smellOccurance);
				}

			}
		} else {
			resultState = applyPatternRelation((PatternRelation) rel);
		}

		return resultState;
	}

	public static State applyRepairMonteCarlo(Relation rel) {

		State resultState = null;

		if (!(rel instanceof PatternRelation)) {

			Repair repair = rel.getUsedRepair();
			SmellOccurance smellOccurance = rel.getFixedSmellOccurance();
			State baseState = rel.getFromState();

			if (rel instanceof Relation) {
				resultState = applyBasicRepairMonteCarlo(baseState, repair, smellOccurance);

				if (repair instanceof DependencyRepair) {
					applyDependencies(resultState, (DependencyRepair) repair, smellOccurance);
				}
			}
		} else {
			resultState = applyPatternRelationMonteCarlo((PatternRelation) rel);

		}
		return resultState;

	}

	// for the simply repair without dependency just remove smell occurance
	private static State applyBasicRepair(State baseState, Repair repair, SmellOccurance smellOccurance) {

		State resultState = new State();

		List<SmellOccurance> smellOccuranceList = new ArrayList<SmellOccurance>(baseState.getSmells());

		smellOccuranceList.remove(smellOccurance);

		resultState.setSmells(smellOccuranceList);

		return resultState;
	}

	// for the simply repair without dependency just remove smell occurrence
	private static State applyBasicRepairMonteCarlo(State baseState, Repair repair, SmellOccurance smellOccurance) {

		State resultState = State.getMonteCarloStateInstance();

		List<SmellOccurance> smellOccuranceList = new ArrayList<SmellOccurance>(baseState.getSmells());

		smellOccuranceList.remove(smellOccurance);

		resultState.setSmells(smellOccuranceList);
		return resultState;
	}

	private static void applyDependencies(State state, DependencyRepair repair, SmellOccurance smellOccurance) {

		for (Dependency dep : repair.getDependencies()) {

			if (dep.getType() == DependencyType.CAUSE) {

				if (dep.getPlaceType() == DependencyPlaceType.INTERNAL) {
					SmellOccurance newSmellOccurance = new SmellOccurance(dep.getSmell());

					List<LocationPart> newLocationPartList = new ArrayList<LocationPart>();

					List<LocationPart> tempLocationPartList = smellOccurance.getLocations().get(0).getLocation();
					boolean isFound = false;
					LocationPart currentPart = null;

					for (int i = tempLocationPartList.size() - 1; i >= 0; i--) {

						currentPart = tempLocationPartList.get(i);

						if (isFound) {

							newLocationPartList.add(currentPart);

						} else {

							if (currentPart.getLocationPartType() == dep.getActionField()) {
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

				if (dep.getPlaceType() == DependencyPlaceType.INTERNAL) {
					SmellOccurance tempSmellOccurance = isOnSameLocation(state, smellOccurance, dep.getSmell(),
							dep.getActionField());

					if (tempSmellOccurance != null) {
						state.getSmells().remove(tempSmellOccurance);
					}

				}

				if (dep.getPlaceType() == DependencyPlaceType.EXTERNAL) {
					// Nothing to do... :(
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

		// fitness = (state.getDepth());
		state.setFitness(fitness);
	}

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
		fitness = ((int) (fit * 1000));

		State currentState = state;
		while (currentState.getSourceRelation() != null) {
			fitness -= (currentState.getSourceRelation().getUsedRepair()
					.getWeight(currentState.getSourceRelation().getFixedSmellOccurance().getSmell()) * 5);
			currentState = currentState.getSourceRelation().getFromState();
		}
		state.setFitness(fitness);
	}

	public static void calculateFitnessForMonteCarlo(State state, long rootSmellsWeight) {
		double fitness = 0;
		float fit = 0;
		for (SmellOccurance smellOccurance : state.getSmells()) {
			fitness += smellOccurance.getSmell().getWeight() * 2;
		}
		if (fitness == 0) {
			fitness = 1;
		}
		fit = 1 / (float) fitness;
		fitness = (fit * 10000);

		State currentState = state;
		while (currentState.getSourceRelation() != null) {
			fitness -= (currentState.getSourceRelation().getUsedRepair()
					.getWeight(currentState.getSourceRelation().getFixedSmellOccurance().getSmell()) * 1);
			currentState = currentState.getSourceRelation().getFromState();
		}
		state.setFitness(fitness);
	}

	public static void initializeState(State state, int minPheromone) {
		for (Relation r : state.getRelations()) {
			if (r.getPheromoneTrail() < 1) {
				r.setPheromoneTrail(minPheromone);
			}
		}
	}

	public static String createHash(State s) {

		StringBuilder sb = new StringBuilder();

		for (SmellOccurance so : s.getSmells()) {
			sb.append(so.getSmell().getId() + "_");
			for (LocationPart loc : so.getLocations().get(0).getLocation()) {
				sb.append(loc.getId() + "_" + loc.getLocationPartType() + "_");
			}
		}

		return sb.toString();
	}

	public static SmellOccurance isOnSameLocation(State state, SmellOccurance baseSmellOccurance, SmellType smellType,
			LocationPartType placeType) {

		SmellOccurance result = null;
		List<LocationPart> tempPath;

		for (SmellOccurance smellOccurance : state.getSmells()) {

			if (smellOccurance != baseSmellOccurance) {

				if (smellOccurance.getSmell() == smellType) {

					tempPath = PlaceComparator.findCommonDestinationPath(
							baseSmellOccurance.getLocations().get(0).getLocation(),
							smellOccurance.getLocations().get(0).getLocation());

					for (int i = tempPath.size() - 1; i >= 0; i--) {

						if (tempPath.get(i).getLocationPartType() == placeType) {
							result = smellOccurance;
							break;
						}

					}
				}

			}
		}

		return result;
	}

	private static State applyPatternRelation(PatternRelation rel) {

		State baseState = rel.getFromState();
		State resultState = new State();

		List<SmellOccurance> newSmellsSet = new ArrayList<SmellOccurance>(baseState.getSmells());

		List<LocationPart> tempLocation = null;
		boolean isAssign = false;
		// REMOVE SMELLS
		for (PatternSmellUse psu : rel.getPatternSmellUses().keySet()) {
			newSmellsSet.remove(rel.getPatternSmellUses().get(psu));

			if (!isAssign) {
				tempLocation = rel.getPatternSmellUses().get(psu).getLocations().get(0).getLocation();
				isAssign = true;
			}
		}

		// ADD SMELLS
		List<LocationPart> location = createLocation(tempLocation, rel.getUsedPattern().getActionField());

		for (SmellType st : rel.getUsedPattern().getResidualSmells()) {
			SmellOccurance newSmellOccurance = new SmellOccurance(st);

			List<Location> locs = new ArrayList<Location>();
			locs.add(new Location(location));

			newSmellOccurance.setLocations(locs);

			newSmellsSet.add(newSmellOccurance);
		}

		resultState.setSmells(newSmellsSet);

		return resultState;
	}

	private static State applyPatternRelationMonteCarlo(PatternRelation rel) {

		State baseState = rel.getFromState();
		State resultState = State.getMonteCarloStateInstance();

		List<SmellOccurance> newSmellsSet = new ArrayList<SmellOccurance>(baseState.getSmells());

		List<LocationPart> tempLocation = null;
		boolean isAssign = false;
		// REMOVE SMELLS
		for (PatternSmellUse psu : rel.getPatternSmellUses().keySet()) {
			newSmellsSet.remove(rel.getPatternSmellUses().get(psu));

			if (!isAssign) {
				tempLocation = rel.getPatternSmellUses().get(psu).getLocations().get(0).getLocation();
				isAssign = true;
			}
		}

		// ADD SMELLS
		List<LocationPart> location = createLocation(tempLocation, rel.getUsedPattern().getActionField());

		for (SmellType st : rel.getUsedPattern().getResidualSmells()) {
			SmellOccurance newSmellOccurance = new SmellOccurance(st);

			List<Location> locs = new ArrayList<Location>();
			locs.add(new Location(location));

			newSmellOccurance.setLocations(locs);

			newSmellsSet.add(newSmellOccurance);
		}

		resultState.setSmells(newSmellsSet);

		return resultState;
	}

	private static List<LocationPart> createLocation(List<LocationPart> location, LocationPartType actionField) {

		List<LocationPart> newLocation = new ArrayList<LocationPart>();

		int index = location.size() - 1;
		for (; index >= 0; index--) {

			if (location.get(index).getLocationPartType() == actionField) {
				break;
			}

		}

		for (int i = 0; i < index + 1; i++) {
			newLocation.add(location.get(i));
		}

		return newLocation;
	}

}
