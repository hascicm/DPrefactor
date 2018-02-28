package sk.fiit.dp.pathFinder.entities;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class OptimalPathForCluster {

	private State rootState;
	private List<Relation> optimalPath;

	public OptimalPathForCluster() {
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

	public JSONObject toJSON() {
		JSONArray repairs = new JSONArray();
		int i = 0;

		for (Relation x : optimalPath) {
			JSONObject curr = new JSONObject();
			curr.put("repair", x.getUsedRepair().getName());
			curr.put("repaired", x.getFixedSmellOccurance().toJSON());
			curr.put("order ", i);
			i++;
			repairs.put(curr);
		}
		JSONObject result = new JSONObject();
		result.append("rootsmells", rootState.asJSONArray());
		result.append("repairs", repairs);

		return result;
	}

}
