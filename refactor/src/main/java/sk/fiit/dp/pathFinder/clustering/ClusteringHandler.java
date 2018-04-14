package sk.fiit.dp.pathFinder.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sk.fiit.dp.pathFinder.clustering.model.Cluster;
import sk.fiit.dp.pathFinder.dataprovider.DataProvider;
import sk.fiit.dp.pathFinder.dataprovider.DatabaseDataProvider;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.refactor.model.JessInput;

public class ClusteringHandler {
	public static List<State> executeClustering(List<JessInput> searchResults) {

		Logger.getLogger("clutering logger").log(Level.INFO, "clustering is starting");
		DataProvider dataProvider = DatabaseDataProvider.getInstance();

		Logger.getLogger("clutering logger").log(Level.INFO, "creating inital state");
		dataProvider.initializeRootState(searchResults);

		Logger.getLogger("clutering logger").log(Level.INFO, "initializing clusters");
		List<Cluster> clusters = ClusteringHelperClass.initializeClusters(dataProvider.getRootState());

		Logger.getLogger("clutering logger").log(Level.INFO, "merging nested smells");
		clusters = ClusteringHelperClass.mergeNestedSmells(clusters);
		Logger.getLogger("clutering logger").log(Level.INFO, "merged cluster count: " + clusters.size());

		int numberOfSmells = 0;
		for (Cluster c : clusters) {
			c.setClusterDepth(0);
			numberOfSmells += c.getSmellOccurrences().size();
		}
		System.out.println(numberOfSmells);
		CluseteringMethod cm = new HierarchiacalClustering();
		Logger.getLogger("clutering logger").log(Level.INFO, "execution started");
		cm.executeClustering(clusters);

		int desiredClusterCount = (int) Math.round(Math.pow(numberOfSmells, 1.0 / 3));
		if (desiredClusterCount < 1) {
			desiredClusterCount = 1;
		} else if (desiredClusterCount > numberOfSmells) {
			desiredClusterCount = numberOfSmells;
		}
		System.out.println(desiredClusterCount);
		List<Cluster> x = cm.getResult(desiredClusterCount);

		List<State> result = new ArrayList<State>();
		
		for (Cluster c : x) {
			State act = new State();
			act.setSmells(c.getSmellOccurrences());
			result.add(act);
		}

		Logger.getLogger("clutering logger").log(Level.INFO, "clustering finished");
		return result;
	}
}
