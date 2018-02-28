package sk.fiit.dp.pathFinder.entities;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Location {
	private List<LocationPart> location;

	public Location(List<LocationPart> location) {
		super();
		this.location = location;
	}

	public List<LocationPart> getLocation() {
		return location;
	}

	public void setLocation(List<LocationPart> location) {
		this.location = location;
	}

	public String toString() {
		return this.location.toString();
	}

	public JSONArray toJSON() {
		JSONArray result = new JSONArray();
		for (LocationPart l : location) {
			JSONObject locpart = new JSONObject();
			locpart.append("type", l.getLocationPartType());
			locpart.append("name", l.getId());
			result.put(locpart);
		}

		return result;
	}
}
