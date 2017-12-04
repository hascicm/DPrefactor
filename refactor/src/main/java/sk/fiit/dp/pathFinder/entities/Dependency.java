package sk.fiit.dp.pathFinder.entities;

public class Dependency {
	private DependencyType type;
	private SmellType smell;
	private Double probability;
	private LocationPartType actionField; //the area, in which is dependency effective (for example in Class or Method)
	private DependencyPlaceType placeType; 

	public Dependency(DependencyType type, SmellType smell, Double probability, LocationPartType actionField,
						DependencyPlaceType placeType) {
		super();
		this.type = type;
		this.smell = smell;
		this.probability = probability;
		this.actionField = actionField;
		this.placeType = placeType;
	}

	public DependencyType getType() {
		return type;
	}

	public void setType(DependencyType type) {
		this.type = type;
	}

	public SmellType getSmell() {
		return smell;
	}

	public void setSmell(SmellType smell) {
		this.smell = smell;
	}

	public Double getPropability() {
		return probability;
	}

	public void setPropability(Double propability) {
		this.probability = propability;
	}

	public LocationPartType getActionField() {
		return actionField;
	}

	public void setActionField(LocationPartType actionField) {
		this.actionField = actionField;
	}

	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}

	public DependencyPlaceType getPlaceType() {
		return placeType;
	}

	public void setPlaceType(DependencyPlaceType placeType) {
		this.placeType = placeType;
	}
}
