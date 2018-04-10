package sk.fiit.dp.pathFinder.clustering;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.clustering.model.Cluster;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class ClusteringHelperClass {

	/**
	 * method initiate cluster space, for each smelloccurance create separate
	 * cluster
	 * 
	 * @param state
	 *            initial state
	 * @return cluster space
	 */
	public static List<Cluster> initializeClusters(State state) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		for (SmellOccurance so : state.getSmells()) {
			Cluster c = new Cluster();
			c.addOccurrenceToCluster(so);
			clusters.add(c);
		}
		return clusters;
	}

	/**
	 * method merge cluster that are in common tree structure
	 * 
	 * @param clusters
	 * @return
	 */
	public static List<Cluster> mergeNestedSmells(List<Cluster> clusters) {
		boolean complete = false;
		while (complete == false) {
			complete = true;
			complete = FindNestedSmells(clusters, complete);
		}
		return clusters;
	}

	/**
	 * metóda prejde celý zoznam zhlukov a spojí prvé nájdené vnorené zhluky
	 * 
	 * @param clusters
	 * @param complete
	 * @return true v prídade že, sa počas celého priebehu nenašli žiadne dva
	 *         vnorené zhluky, inak false
	 */
	private static boolean FindNestedSmells(List<Cluster> clusters, boolean complete) {
		for (Cluster cx : clusters) {
			for (Cluster cy : clusters) {
				if (cx == cy) {
					continue;
				}
				if (nestedClusters(cx, cy)) {
					Cluster c = mergeClusters(cy, cx);
					clusters.add(c);
					clusters.remove(cx);
					clusters.remove(cy);
					complete = false;
					return complete;
				}
			}
		}
		return complete;
	}

	/**
	 * metóda prechádza inštancie pachov v dvoch zhlukoch a ich polohy a vóla
	 * metódu na zistenie zhniezdenia ich polôh
	 * 
	 * @param cx
	 * @param cy
	 * @return
	 */
	private static boolean nestedClusters(Cluster cx, Cluster cy) {
		boolean result = false;
		for (SmellOccurance x : cx.getSmellOccurrences()) {
			for (SmellOccurance y : cy.getSmellOccurrences()) {
				for (Location lx : x.getLocations()) {
					for (Location ly : y.getLocations()) {
						if (nestedLocation(lx, ly))
							return true;
					}
				}
			}
		}
		return result;
	}

	/**
	 * porovná dve inštancie objektu location a zistí či majú rovnakú cestu do
	 * hĺbky ,,trieda,,
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private static boolean nestedLocation(Location x, Location y) {
		for (LocationPart i : x.getLocation()) {
			for (LocationPart j : y.getLocation()) {
				if (i.getLocationPartType() == LocationPartType.CLASS
						&& j.getLocationPartType() == LocationPartType.CLASS) {
					if (i.getId().equals(j.getId()))
						return true;
				}
				if (i.getLocationPartType() == LocationPartType.METHOD
						&& j.getLocationPartType() == LocationPartType.METHOD) {
					if (i.getId().equals(j.getId()))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * metóda vráti objekt cluster vytvorený spojení dvoch objektov tohto typu
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Cluster mergeClusters(Cluster a, Cluster b) {
		Cluster c = new Cluster();
		c.addAllOccurrences(a.getSmellOccurrences());
		c.addAllOccurrences(b.getSmellOccurrences());
		return c;
	}

}
