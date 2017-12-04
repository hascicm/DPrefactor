package sk.fiit.dp.pathFinder.configuration;

import java.util.List;

import sk.fiit.dp.pathFinder.dataprovider.DataProvider;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.RefactorProcessOptimizer;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.refactor.model.JessInput;

public class PathFinderHandler {

	public static List<Relation> executePathFinder(List<JessInput> searchResults) {
		RefactorProcessOptimizer model = new RefactorProcessOptimizer();
		DataProvider dp = model.getDataProvider();

		// TODO add parameter containing smells
		dp.initializeRootState(searchResults);
		System.out.println("-----------initialization done ------------");
		for (SmellOccurance x : dp.getRootState().getSmells()) {
			System.out.println(x.getSmell().getName());
			for (Location l : x.getLocations()) {
				System.out.println("-------location------");
				for (LocationPart lo : l.getLocation()){
					System.out.println("-------part------");
					System.out.println(lo.getLocationPartType());
					System.out.println(lo.getId());
				}
			}
		}
		model.findRefactoringPath();

		return model.getOptimalPath();
	}
}
