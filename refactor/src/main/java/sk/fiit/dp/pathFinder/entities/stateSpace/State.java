package sk.fiit.dp.pathFinder.entities.stateSpace;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import sk.fiit.dp.pathFinder.entities.Location;

public class State {
	private long id;
	private List<SmellOccurance> smells;
	private double fitness;
	private List<Relation> relations;
	private Relation sourceRelation = null;
	private int depth;

	public State() {

	}

	public State(State s) {

		this.smells = new ArrayList<SmellOccurance>(s.getSmells());
		this.fitness = s.fitness;
		this.depth = s.depth;
		this.sourceRelation = s.getSourceRelation();

	}

	public List<Relation> getRelations() {
		return relations;
	}

	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public List<SmellOccurance> getSmells() {
		return smells;
	}

	public void setSmells(List<SmellOccurance> smells) {
		this.smells = smells;
	}

	public Relation getSourceRelation() {
		return sourceRelation;
	}

	public void setSourceRelation(Relation sourceRelation) {
		this.sourceRelation = sourceRelation;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Smells: ");
		for (SmellOccurance so : this.getSmells()) {
			sb.append(so.getSmell().getName());
			sb.append(so.getLocations());
			sb.append(", ");
		}

		return sb.toString();
	}

	public JSONArray asJSONArray() {
		JSONArray smellsArray = new JSONArray();
		for (SmellOccurance current : smells) {
			smellsArray.put(current.toJSON());
		}
		return smellsArray;
	}

	public static MonteCarloState getMonteCarloStateInstance() {
		State s = new State();
		MonteCarloState mcs = s.new MonteCarloState();
		mcs.N = 0;
		mcs.T = 0;

		return mcs;
	}

	public class MonteCarloState extends State {
		private int N = 0;
		private double T = 0;

		public int getN() {
			return N;
		}

		public void setN(int n) {
			this.N = n;
		}

		public void incremetN() {
			this.N++;
		}

		public double getT() {
			return T;
		}

		public void setT(double t) {
			this.T = t;
		}

		public void addT(double simultatedFitness) {
			this.T += simultatedFitness;
		}
	}
}
