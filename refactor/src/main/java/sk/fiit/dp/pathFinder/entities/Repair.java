package sk.fiit.dp.pathFinder.entities;

import java.util.ArrayList;
import java.util.List;



public class Repair {
	private int id; 
	private String name;
	private List<RepairUse> repairUses; 
	
	public Repair(String name) {
		this.name = name;
		this.repairUses = new ArrayList<RepairUse>();
	}
	
	public Repair(String name,int id) {
		this.name = name;
		this.id = id;
		this.repairUses = new ArrayList<RepairUse>();
	}
	
	public Repair(int id, String name, List<RepairUse> repairUses) {
		this.id = id;
		this.name = name;
		this.repairUses = repairUses;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<SmellType> getSmells(){
		
		List<SmellType> result = new ArrayList<>();
		
		for(RepairUse ru : this.getRepairUses()){
			result.add(ru.smell);
		}
		
		return result;
	}
	
	public void addSmellCoverage(SmellType smell, int weight){
		this.repairUses.add(new RepairUse(smell, weight));
	}
	
	public int getWeight(SmellType smellType){
		
		int result = 0; 
		
		for(RepairUse ru : this.repairUses){
			if(ru.smell == smellType){
				result = ru.weight;
				break;
			}
		}
		
		return result; 
	}
	
	
	public double calculateProbability(){
		return 1.0; 
	}
	
	public List<RepairUse> getRepairUses() {
		return repairUses;
	}

	public void setRepairUses(List<RepairUse> repairUses) {
		this.repairUses = repairUses;
	}



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}



	public class RepairUse{
		private SmellType smell;
		private int weight;
		
		public RepairUse(SmellType smell, int weight) {
			super();
			this.smell = smell;
			this.weight = weight;
		}
		
		public SmellType getSmell(){
			return smell;
		}
			
	}
	
}
