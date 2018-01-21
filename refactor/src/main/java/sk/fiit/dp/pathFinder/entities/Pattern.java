package sk.fiit.dp.pathFinder.entities;

import java.util.List;

public class Pattern {
	private int id; 
	private List<PatternSmellUse> fixedSmells;
	private List<SmellType> residualSmells;
	private Repair usedRepair;
	
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
}
