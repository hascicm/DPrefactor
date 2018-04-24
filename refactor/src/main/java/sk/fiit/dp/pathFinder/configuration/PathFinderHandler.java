package sk.fiit.dp.pathFinder.configuration;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.RefactorProcessOptimizer;
import sk.fiit.dp.pathFinder.entities.OptimalPathForCluster;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class PathFinderHandler {

	public static List<OptimalPathForCluster> executePathFinder(List<State> rootStates, String method) {

		List<OptimalPathForCluster> result = null;
		if (rootStates.size() == 1) {
			result = executeSingleThead(rootStates.get(0), method);
		} else {
			result = executeMultiThdead(rootStates, method);
		}

		return result;
	}

	private static List<OptimalPathForCluster> executeSingleThead(State state, String method) {
		RefactorProcessOptimizer model = new RefactorProcessOptimizer(method);
		model.findRefactoringPath(state);
		
		List<OptimalPathForCluster> resultList = new ArrayList<OptimalPathForCluster>();
		OptimalPathForCluster result = new OptimalPathForCluster();
		result.setRootState(state);
		result.setOptimalPath(model.getOptimalPath());
		
		resultList.add(result);
		
		return resultList;
	}

	private static List<OptimalPathForCluster> executeMultiThdead(List<State> states, String method) {
		List<OptimalPathForCluster> result = new ArrayList<OptimalPathForCluster>();
		List<MultithreadedPathFinder> computers = new ArrayList<MultithreadedPathFinder>();
		for (int i = 0; i < states.size(); i++) {
			MultithreadedPathFinder runner = new PathFinderHandler().new MultithreadedPathFinder(states.get(i), method);
			computers.add(runner);
			runner.start(i);
		}
		for (MultithreadedPathFinder c : computers) {
			try {
				c.t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (MultithreadedPathFinder act : computers) {
			OptimalPathForCluster actres = new OptimalPathForCluster();
			actres.setRootState(act.root);
			actres.setOptimalPath(act.result);
			result.add(actres);
		}
		return result;
	}

	private class MultithreadedPathFinder implements Runnable {
		private Thread t;
		private List<Relation> result;
		private State root;
		private String method;

		public void start(int i) {
			if (t == null) {
				t = new Thread(this, "thread " + i);
				t.start();
			}
		}

		public MultithreadedPathFinder(State root, String method) {
			this.root = root;
			this.method = method;
		}

		@Override
		public void run() {
			RefactorProcessOptimizer model = new RefactorProcessOptimizer(method);
			model.findRefactoringPath(root);
			result = model.getOptimalPath();
		}
	}
}
