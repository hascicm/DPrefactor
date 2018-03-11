package sk.fiit.dp.pathFinder.entities;

public class PatternRepair extends Repair {
	private int patternID;

	public PatternRepair(String name, int id, int patternID) {
		super(name, id);
		this.setPatternID(patternID);
	}

	@Override
	public int getWeight(SmellType smellType) {
		return 5;// RETURN MAX VALUE
	}

	public int getPatternID() {
		return patternID;
	}

	public void setPatternID(int patternID) {
		this.patternID = patternID;
	}

}
