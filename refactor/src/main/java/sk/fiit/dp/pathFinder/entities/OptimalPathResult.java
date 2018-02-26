package sk.fiit.dp.pathFinder.entities;

import java.util.List;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class OptimalPathResult {

	private State rootState;
	private List<Relation> optimalPath;

	public OptimalPathResult() {
	}

	public State getRootState() {
		return rootState;
	}

	public void setRootState(State rootState) {
		this.rootState = rootState;
	}

	public List<Relation> getOptimalPath() {
		return optimalPath;
	}

	public void setOptimalPath(List<Relation> optimalPath) {
		this.optimalPath = optimalPath;
	}

}
