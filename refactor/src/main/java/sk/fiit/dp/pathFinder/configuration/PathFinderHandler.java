package sk.fiit.dp.pathFinder.configuration;

import java.util.List;

import sk.fiit.dp.pathFinder.dataprovider.DataProvider;
import sk.fiit.dp.pathFinder.dataprovider.DatabaseDataProvider;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.RefactorProcessOptimizer;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.refactor.model.JessInput;

public class PathFinderHandler {

	public static List<Relation> executePathFinder(List<JessInput> searchResults, String method) {
		int[] selectedSmells = { 15, 32, 1, 9, 31, 8, 3, 22, 30, 2, 10, 4, 25, 21 };
		int[] selectedRepairs = { 87, 92, 88, 93, 61, 94, 81, 74, 73, 50, 79, 84, 80, 82, 15, 14, 12, 21, 65, 83, 85 };

		RefactorProcessOptimizer model = new RefactorProcessOptimizer(method);
		DataProvider dp = model.getDataProvider();
		((DatabaseDataProvider) dp).reduceDBdata(selectedSmells, selectedRepairs);
		// TODO add parameter containing smells
		dp.initializeRootState(searchResults);
//		System.out.println("-----------initialization done ------------");
//		for (SmellOccurance x : dp.getRootState().getSmells()) {
//			System.out.println(x.getSmell().getName());
//			for (Location l : x.getLocations()) {
//				System.out.println("-------location------");
//				for (LocationPart lo : l.getLocation()) {
//					System.out.println("-------part------");
//					System.out.println(lo.getLocationPartType());
//					System.out.println(lo.getId());
//				}
//			}
//		}
		 model.findRefactoringPath();
//		((DatabaseDataProvider)dp).printSmells();
//		((DatabaseDataProvider) dp).printRepairs();

		return model.getOptimalPath();
	}
}
