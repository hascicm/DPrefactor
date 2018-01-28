package sk.fiit.dp.pathFinder.entities;

import java.util.List;

public class Pattern {
	private int id; 
	private List<PatternSmellUse> fixedSmells;
	private List<SmellType> residualSmells;
	private PatternRepair usedRepair;
	private LocationPartType actionField; //the area, in which is dependency effective (for example in Class or Method)
	private String description; 
	
	public Pattern(){
		
	}
	
	public Pattern(String desc){
		this.description = desc;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<PatternSmellUse> getFixedSmells() {
		return fixedSmells;
	}
	public void setFixedSmells(List<PatternSmellUse> fixedSmells) {
		this.fixedSmells = fixedSmells;
	}
	public List<SmellType> getResidualSmells() {
		return residualSmells;
	}
	public void setResidualSmells(List<SmellType> residualSmells) {
		this.residualSmells = residualSmells;
	}
	public PatternRepair getUsedRepair() {
		return usedRepair;
	}
	public void setUsedRepair(PatternRepair usedRepair) {
		this.usedRepair = usedRepair;
	}
	
	public SmellType getMainSmell(){
		return null;
	}
	public LocationPartType getActionField() {
		return actionField;
	}
	public void setActionField(LocationPartType actionField) {
		this.actionField = actionField;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
