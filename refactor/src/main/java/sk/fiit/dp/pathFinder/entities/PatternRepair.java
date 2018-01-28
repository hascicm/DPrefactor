package sk.fiit.dp.pathFinder.entities;

public class PatternRepair extends Repair {

	public PatternRepair(String name) {
		super(name);
	}
	
	
	@Override
	public int getWeight(SmellType smellType) {
		return 5;//RETURN MAX VALUE
	}

}
