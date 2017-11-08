package sk.fiit.dp.refactor.model.explanation;

//TODO
public class RepairRecord {
	private String codeBeforeRepair;
	private String codeAfterRepair;
	private String usedJessRule;
	private String SmellDescription;

	public String getCodeBeforeRepair() {
		return codeBeforeRepair;
	}

	public void setCodeBeforeRepair(String codeBeforeRepair) {
		this.codeBeforeRepair = codeBeforeRepair;
	}

	public String getCodeAfterRepair() {
		return codeAfterRepair;
	}

	public void setCodeAfterRepair(String codeAfterRepair) {
		this.codeAfterRepair = codeAfterRepair;
	}

	public String getUsedJessRule() {
		return usedJessRule;
	}

	public void setUsedJessRule(String usedJessRule) {
		this.usedJessRule = usedJessRule;
	}

	public String getSmellDescription() {
		return SmellDescription;
	}

	public void setSmellDescription(String smellDescription) {
		SmellDescription = smellDescription;
	}

}
