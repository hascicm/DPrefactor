package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.pathFinder.entities.stateSpace.State.MonteCarloState;
import sk.fiit.dp.pathFinder.usecases.AntColonyPathSearchMultithreded.Lock;

public class MonteCarloSearchStrategy extends PathSearchStrategy {

	private static final int numOfThreads = 50;
	private static final double constant = 50;
	private static final long MaxTimeInMilisec = 10000;

	private List<MonteCarloAgent> agents;
	private MonteCarloState bestState;
	private MonteCarloState rootState;
	private boolean end = false;
	private static int iteration = 0;
	private long startTime;

	private Lock dataProtectionlock = new Lock();

	public MonteCarloSearchStrategy(RelationCreator relationCreator) {
		super(relationCreator);
		agents = new ArrayList<MonteCarloAgent>();
		startTime = System.currentTimeMillis();
	}

	@Override
	public List<Relation> findPath(State rootState, int depth) {
		this.rootState = State.getMonteCarloStateInstance();
		this.rootState.setSmells(rootState.getSmells());
		this.rootState.setN(0);
		this.rootState.setT(0);
		StateProcessor.calculateFitnessForMonteCarlo(this.rootState, 0);
		bestState = this.rootState;

		MonteCarloAgent curent;

		for (int i = 0; i < numOfThreads; i++) {
			curent = new MonteCarloAgent(this.rootState);
			agents.add(curent);
			curent.start(i);
		}

		for (MonteCarloAgent agent : agents) {
			try {
				agent.t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		List<Relation> results = new ArrayList<Relation>();

		if (end) {
			while (bestState.getSourceRelation() != null) {
				results.add(bestState.getSourceRelation());
				bestState = (MonteCarloState) bestState.getSourceRelation().getFromState();
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

	@Override
	protected void expandCurrentState(State currentState) {

		createRelation(currentState);
		applyRepair(currentState.getRelations());
		calculateEndNodeFitness(currentState.getRelations());
		calculateProbabilityOfRelations(currentState.getRelations());
	}

	@Override
	protected void applyRepair(List<Relation> rels) {
		Relation rel = null;
		int length = rels.size();
		for (int i = 0; i < length; i++) {

			rel = rels.get(i);
			State s = StateProcessor.applyRepairMonteCarlo(rel);
			s.setSourceRelation(rel);
			s.setDepth(rel.getFromState().getDepth() + 1);
			rel.setToState(s);

			// sort smells in new state by ID
			s.getSmells().sort((o1, o2) -> {
				if (o1.getSmell().getId().compareTo(o2.getSmell().getId()) == 0) {
					return o1.getLocations().get(0).toString().compareTo(o2.getLocations().get(0).toString());
				} else {
					return o1.getSmell().getId().compareTo(o2.getSmell().getId());
				}
			});
		}
	}

	@Override
	protected void calculateEndNodeFitness(List<Relation> relations) {
		for (Relation rel : relations) {

			StateProcessor.calculateFitnessForMonteCarlo(rel.getToState(), rootStateSmellsWeight);

			if (rel.getToState().getFitness() < 0) {
				rel.getToState().setFitness(1);
			}
			// rel.getToState().setFitness(10);
		}
	}

	public MonteCarloState UCB1(MonteCarloState s) {
		MonteCarloState result = null;
		double maxUCB1 = Double.MIN_VALUE;

		for (Relation r : s.getRelations()) {

			double UCB1;
			MonteCarloState mcs = (MonteCarloState) r.getToState();
			double avgValue = 0;

			if (mcs.getN() == 0) {
				UCB1 = Double.MAX_VALUE;
			} else {
				avgValue = mcs.getT() / mcs.getN();
				UCB1 = avgValue + constant * Math.sqrt((Math.log(rootState.getN()) / mcs.getN()));
			}
			if (maxUCB1 < UCB1 && !isLowProbability(mcs)) {
				maxUCB1 = UCB1;
				result = mcs;
			}
		}

		return result;
	}

	private class MonteCarloAgent implements Runnable {
		MonteCarloState curentState;
		private Thread t;

		public MonteCarloAgent(MonteCarloState rootState) {
			curentState = rootState;
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
				iteration++;
				long currTime = System.currentTimeMillis();

				if ((currTime - startTime) > MaxTimeInMilisec) {
					end = true;
				}
				while (!isLeafNode(curentState)) {
					moveAgent();
					printCurentState();
				}

				if (curentState.getFitness() > bestState.getFitness()) {
					System.out.println("new best at " + curentState.getDepth() + " fit " + curentState.getFitness());
					bestState = curentState;
				}
				if (curentState.getN() == 0) {
					rollout();
				} else {
					dataProtectionlock.lock();
					expandCurrentState(curentState);
					dataProtectionlock.unlock();
					moveAgentToFirstChild();
					rollout();
				}
			}
		}

		private boolean isLeafNode(State state) {
			if (state.getRelations() == null || state.getRelations().isEmpty()) {
				return true;
			}
			return false;
		}

		private void moveAgent() {
			curentState = UCB1(curentState);
		}

		private void moveAgentToFirstChild() {
			if (!curentState.getRelations().isEmpty() && curentState.getRelations().get(0) != null) {
				curentState = (MonteCarloState) curentState.getRelations().get(0).getToState();
			}
		}

		private void rollout() {
			double terminalFitnes = simulate();
			backPropagate(terminalFitnes);
		}

		private void backPropagate(double simultatedFitness) {

			while (curentState != rootState) {
				((MonteCarloState) curentState).addT(simultatedFitness);
				((MonteCarloState) curentState).incremetN();
				curentState = (MonteCarloState) curentState.getSourceRelation().getFromState();
			}
			((MonteCarloState) curentState).addT(simultatedFitness);
			((MonteCarloState) curentState).incremetN();

		}

		private double simulate() {
			double fitnes = curentState.getFitness();
			return fitnes;
		}

		private void printCurentState() {
			System.out.println("agent moving, curent state: N:" + curentState.getN() + "  T: " + curentState.getT()
					+ " depth: " + curentState.getDepth() + " num of smells: " + curentState.getSmells().size()
					+ " fitness " + curentState.getFitness() + "\t in iteration " + iteration);
		}
	}

	public class Lock {

		private boolean isLocked = false;

		public synchronized void lock() {
			while (isLocked) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			isLocked = true;
		}

		public synchronized void unlock() {
			isLocked = false;
			notifyAll();
		}
	}
}
