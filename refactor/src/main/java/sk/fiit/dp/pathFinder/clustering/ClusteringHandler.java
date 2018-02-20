package sk.fiit.dp.pathFinder.clustering;

import java.util.List;
import sk.fiit.dp.pathFinder.clustering.model.Cluster;
import sk.fiit.dp.pathFinder.dataprovider.DataProvider;
import sk.fiit.dp.pathFinder.dataprovider.DatabaseDataProvider;
import sk.fiit.dp.refactor.model.JessInput;

public class ClusteringHandler {
	public static List<Cluster> executeClustering(List<JessInput> searchResults) {
		System.out.println("clustering is starting");
		int[] selectedSmells = { 15, 32, 1, 9, 31, 8, 3, 22, 30, 2, 10, 4, 25, 21 };
		int[] selectedRepairs = { 87, 92, 88, 93, 61, 94, 81, 74, 73, 50, 79, 84, 80, 82, 15, 14, 12, 21, 65, 83, 85 };

		DataProvider dataProvider = new DatabaseDataProvider();
		((DatabaseDataProvider) dataProvider).reduceDBdata(selectedSmells, selectedRepairs);
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
//		System.out.println("merging nested smells");
//		clusters = ClusteringHelperClass.mergeNestedSmells(clusters);

		for (Cluster c : clusters) {
			c.setClusterDepth(0);
		}

		CluseteringMethod cm = new HierarchiacalClustering();
		System.out.println("execution started");
		cm.executeClustering(clusters);
		System.out.println("clustering finished");
		List<Cluster> x = cm.getResult(3);
		System.out.println("--------------- clustering result " + x.size() + " ---------------");
		for (Cluster c : x) {
			c.print();
		}

		return x;
	}
}
