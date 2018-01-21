package sk.fiit.dp.pathFinder.entities;

public class PatternSmellUse {
	private boolean isMain;
	private SmellType smellType;
	public boolean isMain() {
		return isMain;
	}
	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}
	public SmellType getSmellType() {
		return smellType;
	}
	public void setSmellType(SmellType smellType) {
		this.smellType = smellType;
	} 
}
