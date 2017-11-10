package sk.fiit.dp.refactor.model.explanation;

//TODO
public class RepairRecord {
	private String refactoringCode; // with number
	private String refcode; // without number
	private String path;
	private String gitRepository;
	private String codeBeforeRepair;
	private String codeAfterRepair;
	private JessListenerOutput usedJessRule;
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

	public JessListenerOutput getUsedJessRule() {
		return usedJessRule;
	}

	public void setUsedJessRule(JessListenerOutput usedJessRule) {
		this.usedJessRule = usedJessRule;
	}

	public String getSmellDescription() {
		return SmellDescription;
	}

	public void setSmellDescription(String smellDescription) {
		SmellDescription = smellDescription;
	}

	public String getGitRepository() {
		return gitRepository;
	}

	public void setGitRepository(String gitRepository) {
		this.gitRepository = gitRepository;
	}

	public String getRefactoringCode() {
		return refactoringCode;
	}

	public void setRefactoringCode(String refactoringCode) {
		this.refactoringCode = refactoringCode;
	}

	public String getRefcode() {
		return refcode;
	}

	public void setRefcode(String refcode) {
		this.refcode = refcode;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
