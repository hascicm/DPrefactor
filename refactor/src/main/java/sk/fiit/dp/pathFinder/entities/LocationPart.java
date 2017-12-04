package sk.fiit.dp.pathFinder.entities;

public class LocationPart {
	private LocationPartType locationPartType;
	private String id;
	
	public LocationPart(LocationPartType locationPartType, String id) {
		super();
		this.locationPartType = locationPartType;
		this.id = id;
	}
	
	public LocationPartType getLocationPartType() {
		return locationPartType;
	}
	
	public void setLocationPartType(LocationPartType locationPartType) {
		this.locationPartType = locationPartType;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String toString(){
		return "[" + this.getId() + " <" +  this.getLocationPartType() +">]";
	}
}
