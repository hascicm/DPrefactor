package sk.fiit.dp.pathFinder.entities;

import java.util.List;

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
	
	public String toString(){
		return this.location.toString();
	}
}
