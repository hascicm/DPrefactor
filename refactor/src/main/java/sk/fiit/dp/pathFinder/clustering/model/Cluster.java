package sk.fiit.dp.pathFinder.clustering.model;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;

public class Cluster implements Comparable<Cluster> {

	private List<SmellOccurance> smellOccurrences;
	private int clusterDepth;
	private Cluster childA;
	private Cluster childB;

	public int getClusterDepth() {
		return clusterDepth;
	}

	public void setClusterDepth(int clusterDepth) {
		this.clusterDepth = clusterDepth;
	}

	public List<SmellOccurance> getSmellOccurrences() {
		return smellOccurrences;
	}

	public Cluster() {
		this.smellOccurrences = new ArrayList<SmellOccurance>();
	}

	public void addOccurrenceToCluster(SmellOccurance so) {
		this.smellOccurrences.add(so);
	}

	public void addAllOccurrences(List<SmellOccurance> occurrences) {
		this.smellOccurrences.addAll(occurrences);
	}

	public void setchildA(Cluster c) {
		this.childA = c;
	}

	public void setchildB(Cluster c) {
		this.childB = c;
	}

	public Cluster getChildA() {
		return childA;
	}

	public Cluster getChildB() {
		return childB;
	}

	public void print() {
		System.out.println("depth " + clusterDepth + "   \t size " + smellOccurrences.size());
		for (SmellOccurance so : this.getSmellOccurrences()) {
			System.out.println(so.getSmell().getName());
			for (Location l : so.getLocations()) {
				System.out.println(l.toString());
			}
		}
	}

	@Override
	public int compareTo(Cluster o) {
		return Integer.compare(o.getClusterDepth(), this.getClusterDepth());
	}
}
