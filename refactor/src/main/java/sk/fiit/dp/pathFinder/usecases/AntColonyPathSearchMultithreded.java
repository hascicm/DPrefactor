package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class AntColonyPathSearchMultithreded extends PathSearchStrategy {

	private static final int numOfThreads = 50;
	private static final int maxPheromone = 10000;
	private static final int minPheromone = 200;
	private static final int pheromoneCalculatioCoeficient = 1;
	private static final int pheromoneEvaporationPerCrossing = 500;
	private static final int maxNonupdatingIterations = 10000;

	private List<Ant> ants;
	private State bestState;
	private State rootState;
	private boolean end = false;

	private static int iterations = 0;

	private Lock dataProtectionlock = new Lock();

	public AntColonyPathSearchMultithreded(RelationCreator relationCreator) {
		super(relationCreator);
		this.ants = new ArrayList<Ant>();
		new ArrayList<Relation>();

	}

	@Override
	public List<Relation> findPath(State rootState, int depth) {
		this.rootState = rootState;
		Ant curent;
		for (int i = 0; i < numOfThreads; i++) {
			curent = new Ant(rootState);
			ants.add(curent);
			curent.start(i);

		}

		for (Ant ant : ants) {
			try {
				ant.t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		List<Relation> results = new ArrayList<Relation>();

		if (end) {
			while (bestState.getSourceRelation() != null) {
				results.add(bestState.getSourceRelation());
				bestState = bestState.getSourceRelation().getFromState();
			}
			Collections.reverse(results);
		}

		System.out.println("");
		System.out.println("RESULT");
		State currentState = null;
		for (Relation r : results) {
			System.out.println("-------------");
			currentState = r.getFromState();
			System.out.println("S_" + currentState.getId() + " [ Fitness: " + currentState.getFitness()
					+ ", NumOfSmells: " + currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] "
					+ currentState);
			System.out.println(r.getUsedRepair().getName() + " -> " + r.getFixedSmellOccurance().getSmell().getName()
					+ " P: " + r.getProbability());
			currentState = r.getToState();
			System.out.println("S_" + currentState.getId() + " [ Fitness: " + currentState.getFitness()
					+ ", NumOfSmells: " + currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] "
					+ currentState);
		}
		System.out.println(currentState);

		return results;
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

	private class Ant implements Runnable {
		private Thread t;
		private int pheromone;
		private State finalState;
		private State currentState;

		public Ant(State rootState) {
			currentState = rootState;
		}

		public void start(int i) {
			if (t == null) {
				t = new Thread(this, "thread " + i);
				t.start();
			}
		}

		@Override
		public void run() {
			while (!end) {
				if (iterations > maxNonupdatingIterations)
					end = true;
				if (finalState == null) {
					makeAntMove();
				} else {
					if (currentState == rootState) {
						iterations++;
						reinitializeAnt(rootState);
					} else {
						backtrackAnt(rootState);
					}
				}
			}
		}

		public void makeAntMove() {
			// TODO chceck if same state
			try {
				dataProtectionlock.lock();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			expandCurrentState(currentState);
			StateProcessor.initializeState(currentState,minPheromone);
			dataProtectionlock.unlock();

			if (!currentState.getRelations().isEmpty()) {
				Relation nextRelation = rouletteWheel(getCurrentState().getRelations());

				if (isLowProbability(nextRelation.getToState())) {
					finalState = currentState;
				}
				setCurrentState(nextRelation.getToState());


			} else {
				setFinalState(currentState);
				if (bestState == null || getFinalState().getFitness() > bestState.getFitness()) {
					bestState = getFinalState();
//					System.out.println("new best state in depth: " + currentState.getDepth() + " fitness:"
//							+ currentState.getFitness() + " newiteration: " + iterations + " "
//							+ currentState.toString());
//					iterations = 0;
				}
				calculatePheromoneForAnt();
			}
		}

		public void backtrackAnt(State rootState) {
			while (currentState != rootState) {
				State state = getCurrentState();
				Relation relation = state.getSourceRelation();
				relation.setPheromoneTrail(calculatePheromoneForRelation(relation));
				setCurrentState(relation.getFromState());
			}
		}

		private int calculatePheromoneForRelation(Relation relation) {
//			System.out.println(relation.getPheromoneTrail() +" " +  getPheromone() + " " +pheromoneEvaporationPerCrossing);

			int calculatedPheromone = relation.getPheromoneTrail() + getPheromone() - pheromoneEvaporationPerCrossing;
			if (calculatedPheromone < minPheromone) {
				calculatedPheromone = minPheromone;
			} else if (calculatedPheromone > maxPheromone) {
				calculatedPheromone = maxPheromone;
			}
			// System.out.println(calculatedPheromone);
			//System.out.println("feromone trail after evaporation" + calculatedPheromone);
			return calculatedPheromone;
		}

		private void calculatePheromoneForAnt() {
			int calculatedPheromone = (int) (getFinalState().getFitness() / pheromoneCalculatioCoeficient);
			// System.out.println(calculatedPheromone);
			if (calculatedPheromone < minPheromone) {
				setPheromone(minPheromone);
			} else if (calculatedPheromone > maxPheromone) {
				setPheromone(maxPheromone);
			} else {
				setPheromone(calculatedPheromone);
			}
		}

//		private void evaporatePheromoneFromTrails(HashSet<Relation> relations) {
//			for (Relation r : relations) {
//				int calculatedPheromone = r.getPheromoneTrail() - pheromoneEvaporationPerCrossing;
//				if (calculatedPheromone < minPheromone) {
//					r.setPheromoneTrail(minPheromone);
//				} else {
//					r.setPheromoneTrail(calculatedPheromone);
//				}
//			}
//		}

		private void reinitializeAnt(State rootState) {
			finalState = null;
			setCurrentState(rootState);
		}

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

	public class Lock {

		private boolean isLocked = false;

		public synchronized void lock() throws InterruptedException {
			while (isLocked) {
				wait();
			}
			isLocked = true;
		}

		public synchronized void unlock() {
			isLocked = false;
			notify();
		}
	}

}
