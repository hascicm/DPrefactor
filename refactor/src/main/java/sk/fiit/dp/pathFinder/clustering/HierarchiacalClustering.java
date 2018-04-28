package sk.fiit.dp.pathFinder.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import sk.fiit.dp.pathFinder.clustering.model.Cluster;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;

public class HierarchiacalClustering implements CluseteringMethod {

	private final static int neighborPackage = 5;
	private final static int neighborClass = 5;
	private final static int neighborMethod = 5;
	private final static int neighborParameter = 5;
	private final static int neighborNode = 5;
	private final static int neighborAttribute = 5;
	private final static int neighborPosition = 5;

	private final static int packageToPackageDistance = 5;
	private final static int ClassToPackageDistance = 5;
	private final static int MethodToClassDistance = 5;
	private final static int ParameterToMethodDistance = 5;
	private final static int NodeToClassDistance = 5;
	private final static int NodeToMethodDistance = 5;
	private final static int AttributeToClassDistance = 5;
	private final static int AttributeToMethodDistance = 5;
	private final static int defaultPositionDistance = 5;
	private final static int defaultDistance = 5;

	private static final double penalisationConstant = 1;
	private static final long randomSeed = 7811019;

	private static Random generator = new Random(randomSeed);

	private List<Cluster> clusterList;
	private int numberOfIdentifiedSmells;

	@Override
	public void executeClustering(List<Cluster> clusters) {
		clusterList = clusters;
		int numberOfClusters = clusterList.size();
		numberOfIdentifiedSmells = 0;
		for (Cluster c : clusterList) {
			numberOfIdentifiedSmells += c.getSmellOccurrences().size();
		}
		while (numberOfClusters > 1) {
			NearestClusterPair ncp = findNearestClusters(clusterList);
			clusterList.remove(ncp.getClusterA());
			clusterList.remove(ncp.getClusterB());

			clusterList.add(ncp.mergePair());
			numberOfClusters--;
		}
	}

	@Override
	public List<Cluster> getResult(int desiredNumberOfClusters) {
		int resultSize = 1;
		List<Cluster> result = new ArrayList<Cluster>();
		Queue<Cluster> q = new PriorityQueue<Cluster>();
		q.add(clusterList.get(0));
		Cluster act;
		while (!q.isEmpty() && resultSize < desiredNumberOfClusters) {
			act = q.poll();
			if (act.getChildA() != null)
				q.add(act.getChildA());

			if (act.getChildB() != null)
				q.add(act.getChildB());

			if (act.getChildA() == null || act.getChildB() == null)
				result.add(act);
			resultSize++;
		}
		result.addAll(q);
		return result;
	}

	private NearestClusterPair findNearestClusters(List<Cluster> clusters) {
		int currentMin = Integer.MAX_VALUE;
		List<NearestClusterPair> candidates = new ArrayList<>();

		for (int i = 0; i < clusters.size(); i++) {
			for (int j = i; j < clusters.size(); j++) {
				if (clusters.get(i) == clusters.get(j)) {
					continue;
				}
				int distance = calculateDistance(clusters.get(i), clusters.get(j));
				if (currentMin >= distance) {
					currentMin = distance;
					candidates.add(new NearestClusterPair(clusters.get(i), clusters.get(j), distance));
				}
			}
		}
		Iterator<NearestClusterPair> iter = candidates.iterator();
		while (iter.hasNext()) {
			NearestClusterPair curr = iter.next();
			if (curr.getDistance() > currentMin) {
				iter.remove();
			}
		}

		return candidates.get(generator.nextInt(candidates.size()));
	}

	private int calculateDistance(Cluster cluster, Cluster cluster2) {

		int minDistance = Integer.MAX_VALUE;
		int distance = Integer.MAX_VALUE;
		for (SmellOccurance c1 : cluster.getSmellOccurrences()) {
			for (SmellOccurance c2 : cluster2.getSmellOccurrences()) {
				distance = calculateDistanceBetweenSmells(c1, c2);
				if (distance < minDistance)
					minDistance = distance;
			}
		}

		int clusterSize = cluster.getSmellOccurrences().size() + cluster2.getSmellOccurrences().size();
		int PenalizedDistance = (int) Math.round(minDistance * calculateSizePenalization(clusterSize));


		return PenalizedDistance;
	}

	private double calculateSizePenalization(int clusterSize) {
		return (((float) clusterSize / (float) numberOfIdentifiedSmells) * penalisationConstant);
	}

	private int calculateDistanceBetweenSmells(SmellOccurance c1, SmellOccurance c2) {
		int minDinstance = Integer.MAX_VALUE;
		int distance = Integer.MAX_VALUE;
		for (Location l1 : c1.getLocations()) {
			for (Location l2 : c2.getLocations()) {
				distance = calculateDistanceBetweenLocations(l1, l2);
				if (distance < minDinstance) {
					minDinstance = distance;
				}
			}
		}
		return distance;
	}

	private int calculateDistanceBetweenLocations(Location l1, Location l2) {
		int distance = 0;

		int shorter = l1.getLocation().size();
		if (l2.getLocation().size() < shorter)
			shorter = l2.getLocation().size();
		int i = 0;
		while (i < shorter && compareLocationParts(l1.getLocation().get(i), l2.getLocation().get(i))) {
			i++;
		}
		i--;
		if (i < 0)
			i = 0;
		distance += evaluateNaigborDistance(l1.getLocation().get(i));
		distance += calculateRemainingDistanceInLocation(i, l1.getLocation());
		distance += calculateRemainingDistanceInLocation(i, l2.getLocation());
		return distance;
	}

	private int evaluateNaigborDistance(LocationPart locationPart) {
		int distance = 0;
		if (LocationPartType.PACKAGE == locationPart.getLocationPartType()) {
			distance = neighborPackage;
		} else if (LocationPartType.CLASS == locationPart.getLocationPartType()) {
			distance = neighborClass;
		} else if (LocationPartType.METHOD == locationPart.getLocationPartType()) {
			distance = neighborMethod;
		} else if (LocationPartType.PARAMETER == locationPart.getLocationPartType()) {
			distance = neighborParameter;
		} else if (LocationPartType.NODE == locationPart.getLocationPartType()) {
			distance = neighborNode;
		} else if (LocationPartType.ATTRIBUTE == locationPart.getLocationPartType()) {
			distance = neighborAttribute;
		} else if (LocationPartType.POSITION == locationPart.getLocationPartType()) {
			distance = neighborPosition;
		}
		return distance;
	}

	private int calculateRemainingDistanceInLocation(int i, List<LocationPart> lp) {
		int distance = 0;
		for (int x = i; x < lp.size() - 1; x++) {
			LocationPart act = lp.get(x);
			LocationPart next = lp.get(x + 1);
			if (act.getLocationPartType() == LocationPartType.PACKAGE
					&& next.getLocationPartType() == LocationPartType.PACKAGE) {
				distance += packageToPackageDistance;
			} else if (act.getLocationPartType() == LocationPartType.PACKAGE
					&& next.getLocationPartType() == LocationPartType.CLASS) {
				distance += ClassToPackageDistance;
			} else if (act.getLocationPartType() == LocationPartType.CLASS
					&& next.getLocationPartType() == LocationPartType.METHOD) {
				distance += MethodToClassDistance;
			} else if (act.getLocationPartType() == LocationPartType.METHOD
					&& next.getLocationPartType() == LocationPartType.PARAMETER) {
				distance += ParameterToMethodDistance;
			} else if (act.getLocationPartType() == LocationPartType.CLASS
					&& next.getLocationPartType() == LocationPartType.NODE) {
				distance += NodeToClassDistance;
			} else if (act.getLocationPartType() == LocationPartType.METHOD
					&& next.getLocationPartType() == LocationPartType.NODE) {
				distance += NodeToMethodDistance;
			} else if (act.getLocationPartType() == LocationPartType.CLASS
					&& next.getLocationPartType() == LocationPartType.ATTRIBUTE) {
				distance += AttributeToClassDistance;
			} else if (act.getLocationPartType() == LocationPartType.METHOD
					&& next.getLocationPartType() == LocationPartType.ATTRIBUTE) {
				distance += AttributeToMethodDistance;
			} else if (next.getLocationPartType() == LocationPartType.POSITION) {
				distance += defaultPositionDistance;
			} else {
				distance += defaultDistance;
			}
		}
		return distance;
	}

	private boolean compareLocationParts(LocationPart lp1, LocationPart lp2) {
		if (lp1.getLocationPartType() == lp2.getLocationPartType() && lp1.getId().equals(lp2.getId()))
			return true;
		return false;
	}

	private class NearestClusterPair {
		private Cluster a;
		private Cluster b;
		private int distance;

		public NearestClusterPair(Cluster a, Cluster b, int distance) {
			this.a = a;
			this.b = b;
			this.distance = distance;
		}

		public int getDistance() {
			return distance;
		}

		public Cluster getClusterA() {
			return a;
		}

		public Cluster getClusterB() {
			return b;
		}

		public Cluster mergePair() {
			Cluster parent = ClusteringHelperClass.mergeClusters(a, b);
			parent.setchildA(a);
			parent.setchildB(b);
			parent.setClusterDepth(a.getClusterDepth() + 1);
			return parent;
		}
	}

}
