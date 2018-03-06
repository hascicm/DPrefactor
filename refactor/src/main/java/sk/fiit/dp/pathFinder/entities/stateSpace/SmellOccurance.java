package sk.fiit.dp.pathFinder.entities.stateSpace;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.SmellType;

public class SmellOccurance {
	private SmellType smell;
	private List<Location> locations; // na prvom mieste sa nachadza klucova trieda?
	private String code;
										

	public SmellOccurance(SmellType smell) {
		super();
		this.smell = smell;
		this.locations = new ArrayList<Location>();
	}

	public SmellOccurance(SmellType smell, List<Location> locations) {
		super();
		this.smell = smell;
		this.locations = locations;
	}

	public SmellOccurance(SmellType smell, List<Location> locations, String code) {
		super();
		this.smell = smell;
		this.locations = locations;
		this.setCode(code);
	}

	public SmellType getSmell() {
		return smell;
	}

	public void setSmell(SmellType smell) {
		this.smell = smell;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.append("name", smell.getName());
		result.append("description", smell.getDescription());
		result.append("position", locationsToJSON());
		return result;
	}

	public JSONArray locationsToJSON() {
		JSONArray result = new JSONArray();
		for (Location l : locations) {
			JSONArray current = new JSONArray();

			for (LocationPart j : l.getLocation()) {
				JSONObject part = new JSONObject();
				part.put("type", j.getLocationPartType().toString());
				part.put("name", j.getId());
				current.put(part);
			}
			result.put(current);
		}
		return result;
	}


}
