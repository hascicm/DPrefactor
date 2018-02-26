package sk.fiit.dp.pathFinder.configuration;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.entities.OptimalPathResult;
import sk.fiit.dp.pathFinder.entities.RefactorProcessOptimizer;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class PathFinderHandler {

	public static List<OptimalPathResult> executePathFinder(List<State> rootStates, String method) {
		// int[] selectedSmells = { 15, 32, 1, 9, 31, 8, 3, 22, 30, 2, 10, 4,
		// 25, 21 };
		// int[] selectedRepairs = { 87, 92, 88, 93, 61, 94, 81, 74, 73, 50, 79,
		// 84, 80, 82, 15, 14, 12, 21, 65, 83, 85 };
		List<OptimalPathResult> result = null;
		if (rootStates.size() == 1) {
			result = executeSingleThead(rootStates.get(0), method);
		} else {
			result = executeMultiThdead(rootStates, method);
		}

		// RefactorProcessOptimizer model = new
		// RefactorProcessOptimizer(method);
		// DataProvider dp = model.getDataProvider();
		// ((DatabaseDataProvider) dp).reduceDBdata(selectedSmells,
		// selectedRepairs);
		// dp.initializeRootState(rootStates);
		// System.out.println("-----------initialization done ------------");
		// for (SmellOccurance x : dp.getRootState().getSmells()) {
		// System.out.println(x.getSmell().getName());
		// for (Location l : x.getLocations()) {
		// System.out.println("-------location------");
		// for (LocationPart lo : l.getLocation()) {
		// System.out.println("-------part------");
		// System.out.println(lo.getLocationPartType());
		// System.out.println(lo.getId());
		// }
		// }
		// }

		// model.findRefactoringPath();
		// ((DatabaseDataProvider)dp).printSmells();
		// ((DatabaseDataProvider) dp).printRepairs();

		return result;
	}

	private static List<OptimalPathResult> executeSingleThead(State state, String method) {
		RefactorProcessOptimizer model = new RefactorProcessOptimizer(method);
		model.findRefactoringPath(state);
		
		List<OptimalPathResult> resultList = new ArrayList<OptimalPathResult>();
		OptimalPathResult result = new OptimalPathResult();
		result.setRootState(state);
		result.setOptimalPath(model.getOptimalPath());
		
		resultList.add(result);
		
		return resultList;
	}

	private static List<OptimalPathResult> executeMultiThdead(List<State> states, String method) {
		List<OptimalPathResult> result = new ArrayList<OptimalPathResult>();
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
			OptimalPathResult actres = new OptimalPathResult();
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
