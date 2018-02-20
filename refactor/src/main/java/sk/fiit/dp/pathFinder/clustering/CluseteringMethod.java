package sk.fiit.dp.pathFinder.clustering;

import java.util.List;

import sk.fiit.dp.pathFinder.clustering.model.Cluster;

public interface CluseteringMethod {
	public void executeClustering(List<Cluster> clusters);

	public List<Cluster> getResult(int desiredNumberOfClusters);
}
