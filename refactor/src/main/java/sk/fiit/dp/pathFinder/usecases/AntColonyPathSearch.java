package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

@Deprecated
public class AntColonyPathSearch extends PathSearchStrategy {

	private static final int maxPheromone = 100;
	private static final int minPheromone = 20;
	private static final int pheromoneCalculatioCoeficient = 50;
	private static final int pheromoneEvaporationPerCycle = 30;
	private List<Ant> ants;
	private static int x = 0;
	private State bestState;
	private boolean end = false;
	List<Relation> relations;
	HashSet<Relation> exploredrelations;

	public AntColonyPathSearch(RelationCreator relationCreator) {
		super(relationCreator);
		this.ants = new ArrayList<Ant>();
		this.relations = new ArrayList<Relation>();
		this.exploredrelations = new HashSet<Relation>();
		for (int i = 0; i < 2; i++)
			ants.add(new Ant());

	}

	@Override
	public List<Relation> findPath(State rootState, int depth) {

		for (Ant a : ants)
			reinitializeAnt(a, rootState);

		while (!end) {
			x++;
			evaporatePheromoneFromTrails(exploredrelations);
			for (Ant a : ants) {
				if (a.finalState == null) {
					makeAntMove(a);
				} else {
					if (a.currentState == rootState) {
						reinitializeAnt(a, rootState);
					} else {
						backtrackAnt(a, rootState);
					}
				}
			}
		}

		return null;

	}

	private void reinitializeAnt(Ant a, State rootState) {
		// System.out.println("ant going again");

		a.finalState = null;
		a.setCurrentState(rootState);
	}

	public void makeAntMove(Ant ant) {

		State currentState = ant.getCurrentState();
		expandCurrentState(currentState);
		StateProcessor.initializeState(currentState, minPheromone);

		if (!currentState.getRelations().isEmpty()) {
			Relation nextRelation = rouletteWheel(ant.getCurrentState().getRelations());
			ant.setCurrentState(nextRelation.getToState());
			if (!exploredrelations.contains(nextRelation)) {
				exploredrelations.add(nextRelation);
			}
		} else {
			ant.setFinalState(currentState);
			if (bestState == null || ant.getFinalState().getFitness() > bestState.getFitness()) {
				bestState = ant.getFinalState();
				System.out.println("new best state in depth: " + currentState.getDepth() + " fitness:"
						+ currentState.getFitness() + " x: " + x + " " + currentState.toString());
			}
			calculateAntPheromoneForAnt(ant);
		}
	}

	public void backtrackAnt(Ant ant, State rootState) {
		State state = ant.getCurrentState();
		Relation relation = state.getSourceRelation();
		relation.setPheromoneTrail(calculatePheromoneForRelation(ant, relation));
		ant.setCurrentState(relation.getFromState());
	}

	private void evaporatePheromoneFromTrails(HashSet<Relation> relations) {
		for (Relation r : relations) {
			int calculatedPheromone = r.getPheromoneTrail() - pheromoneEvaporationPerCycle;
			if (calculatedPheromone < minPheromone) {
				r.setPheromoneTrail(minPheromone);
			} else {
				r.setPheromoneTrail(calculatedPheromone);
			}
		}
	}

	private void calculateAntPheromoneForAnt(Ant ant) {
		int calculatedPheromone = (int) (ant.getFinalState().getFitness() / pheromoneCalculatioCoeficient);
		// System.out.println(root.getFitness() /
		// pheromoneCalculatioCoeficient);
		// System.out.println("pheromone: " + calculatedPheromone);
		if (calculatedPheromone < minPheromone) {
			ant.setPheromone(minPheromone);
		} else if (calculatedPheromone > maxPheromone) {
			ant.setPheromone(maxPheromone);
		} else {
			ant.setPheromone(calculatedPheromone);
		}
	}

	private int calculatePheromoneForRelation(Ant ant, Relation relation) {
		int calculatedPheromone = relation.getPheromoneTrail() + ant.getPheromone();
		if (calculatedPheromone < minPheromone) {
			calculatedPheromone = minPheromone;
		} else if (calculatedPheromone > maxPheromone) {
			calculatedPheromone = maxPheromone;
		}
		// System.out.println(calculatedPheromone);
		return calculatedPheromone;
	}

	private Relation rouletteWheel(List<Relation> posibleMoves) {
		int sum = 0;
		int partialsum = 0;
		Relation output = null;

		for (Relation r : posibleMoves) {
			sum += r.getPheromoneTrail();
		}
		int x = new Random().nextInt(sum);

		for (Relation r : posibleMoves) {
			partialsum += r.getPheromoneTrail();
			output = r;
			if (partialsum >= x)
				break;
		}
		return output;
	}

	@Override
	protected void calculateEndNodeFitness(List<Relation> relations) {

		for (Relation rel : relations) {
			StateProcessor.calculateFitnessForAnts(rel.getToState());
		}
	}

	private class Ant {
		private int pheromone;
		private State finalState;
		private State currentState;

		public State getCurrentState() {
			return currentState;
		}

		public void setCurrentState(State currentState) {
			this.currentState = currentState;
		}

		public State getFinalState() {
			return finalState;
		}

		public void setFinalState(State finalState) {
			this.finalState = finalState;
		}

		public int getPheromone() {
			return pheromone;
		}

		public void setPheromone(int pheromone) {
			this.pheromone = pheromone;
		}

	}

}
