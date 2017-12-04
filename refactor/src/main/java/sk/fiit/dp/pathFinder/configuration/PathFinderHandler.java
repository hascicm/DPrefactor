package sk.fiit.dp.pathFinder.configuration;

import sk.fiit.dp.pathFinder.entities.RefactorProcessOptimizer;

public class PathFinderHandler {

	public static void executePathFinder() {
		RefactorProcessOptimizer model = new RefactorProcessOptimizer();
		model.findRefactoringPath();
	}
}
