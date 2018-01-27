package sk.fiit.dp.pathFinder.entities;

import java.util.List;

public class Pattern {
	private int id; 
	private List<PatternSmellUse> fixedSmells;
	private List<SmellType> residualSmells;
	private Repair usedRepair;
	private LocationPartType actionField; //the area, in which is dependency effective (for example in Class or Method)
	
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
	public Repair getUsedRepair() {
		return usedRepair;
	}
	public void setUsedRepair(Repair usedRepair) {
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
}
