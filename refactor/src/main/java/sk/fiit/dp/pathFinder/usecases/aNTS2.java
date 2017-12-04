package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class aNTS2 extends PathSearchStrategy {

	private static final int numOfThreads = 1;
	private static final int maxPheromone = 100000;
	private static final int minPheromone = 0;
	private static final int pheromoneCalculatioCoeficient = 1;
	private static final int pheromoneEvaporationPerCrossing = 10000;
	private static final int maxNonupdatingIterations = 100;

	private List<Ant> ants;
	private State bestState;
	private State rootState;
	private boolean end = false;

	private static int iterations = 0;

	private static HashSet<Relation> exploredrelations;
	private Lock dataProtectionlock = new Lock();

	public aNTS2(RelationCreator relationCreator) {
		super(relationCreator);
		this.ants = new ArrayList<Ant>();
		new ArrayList<Relation>();
		exploredrelations = new HashSet<Relation>();
	}

	@Override
	public List<Relation> findPath(State rootState, int depth) {
		this.rootState = rootState;
		// System.out.println(rootState);
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
		/*
		 * System.out.println(""); System.out.println("RESULT"); State
		 * currentState = null; for (Relation r : results) {
		 * System.out.println("-------------"); currentState = r.getFromState();
		 * System.out.println("S_" + currentState.getId() + " [ Fitness: " +
		 * currentState.getFitness() + ", NumOfSmells: " +
		 * currentState.getSmells().size() + ", Depth: " +
		 * currentState.getDepth() + "] " + currentState);
		 * System.out.println(r.getUsedRepair().getName() + " -> " +
		 * r.getFixedSmellOccurance().getSmell().getName() + " P: " +
		 * r.getProbability()); currentState = r.getToState();
		 * System.out.println("S_" + currentState.getId() + " [ Fitness: " +
		 * currentState.getFitness() + ", NumOfSmells: " +
		 * currentState.getSmells().size() + ", Depth: " +
		 * currentState.getDepth() + "] " + currentState); }
		 * System.out.println(currentState);//
		 */
		return results;
	}

	private Relation rouletteWheel(List<Relation> posibleMoves) {
		int sum = 0;
		int partialsum = 0;
		Relation output = null;
//		System.out.println("------------------");
		for (Relation r : posibleMoves) {
			sum += r.getPheromoneTrail();
	//		System.out.println(r.getPheromoneTrail());
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
	}// */

	
	
	
	
	
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
				if (isInLeafNode()) {
					expandNode();
					if (currentState.getRelations().size() != 0) {
	//					System.out.println(currentState.getRelations().size());
						moveUsingRoultte();
					}
					if (bestState== null || bestState.getFitness() < currentState.getFitness()){
						bestState = currentState;
						System.out.println("new best state in depth: " + bestState.getDepth() + " fitness:"
								+ bestState.getFitness() + " newiteration: " + iterations + " "
								+ bestState.toString());
						}
					backTrackToRoot();
					reinitialize();
				} else {
					if (currentState.getRelations().size() != 0) {
						moveUsingRoultte();
					} else {
						backTrackToRoot();
						reinitialize();
					}
				}
			}
		}

		private void moveUsingRoultte() {
			Relation nextRelation = rouletteWheel(currentState.getRelations());
			// if (isLowProbability(nextRelation.getToState())) {
			// return;
			// }
			setCurrentState(nextRelation.getToState());
		}

		private void backTrackToRoot() {
			while (currentState != rootState) {
				Relation r = currentState.getSourceRelation();
				// TODO
				r.setPheromoneTrail(50);
				currentState = r.getFromState();
			}
		}

		private void expandNode() {
			try {
				dataProtectionlock.lock();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			expandCurrentState(currentState);
			StateProcessor.initializeState(currentState);
			dataProtectionlock.unlock();
		}

		private boolean isInLeafNode() {
			if (currentState.getRelations() == null)
				return true;
			return false;
		}

		private void reinitialize() {
			currentState = rootState;
			finalState = null;
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
			this.pheromone += pheromone;
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
