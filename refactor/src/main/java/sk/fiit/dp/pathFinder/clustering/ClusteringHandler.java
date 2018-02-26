package sk.fiit.dp.pathFinder.clustering;

import java.util.ArrayList;
import java.util.List;
import sk.fiit.dp.pathFinder.clustering.model.Cluster;
import sk.fiit.dp.pathFinder.dataprovider.DataProvider;
import sk.fiit.dp.pathFinder.dataprovider.DatabaseDataProvider;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.refactor.model.JessInput;

public class ClusteringHandler {
	public static List<State> executeClustering(List<JessInput> searchResults) {
		System.out.println("clustering is starting");

		DataProvider dataProvider = DatabaseDataProvider.getInstance();

		System.out.println("creating inital state");
		dataProvider.initializeRootState(searchResults);

		System.out.println("initializing clusters");
		List<Cluster> clusters = ClusteringHelperClass.initializeClusters(dataProvider.getRootState());
		int i = 1;
		for (Cluster c : clusters) {
			System.out.println("----- cluster " + i + " ------");
			c.print();
			i++;
		}
		
		System.out.println("merging nested smells");
		clusters = ClusteringHelperClass.mergeNestedSmells(clusters);

		for (Cluster c : clusters) {
			c.setClusterDepth(0);
		}

		CluseteringMethod cm = new HierarchiacalClustering();
		System.out.println("execution started");
		cm.executeClustering(clusters);
		System.out.println("clustering finished");
		List<Cluster> x = cm.getResult(3);

		List<State> result = new ArrayList<State>();
		
		System.out.println("--------------- clustering result " + x.size() + " ---------------");
		for (Cluster c : x) {
			c.print();
			State act = new State();
			act.setSmells(c.getSmellOccurrences());
			result.add(act);
		}
		

		return result;
	}
}
