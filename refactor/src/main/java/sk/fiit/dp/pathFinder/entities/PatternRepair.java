package sk.fiit.dp.pathFinder.entities;

public class PatternRepair extends Repair {

	public PatternRepair(String name, int id) {
		super(name,id);
	}
	
	
	@Override
	public int getWeight(SmellType smellType) {
		return 5;//RETURN MAX VALUE
	}

}
