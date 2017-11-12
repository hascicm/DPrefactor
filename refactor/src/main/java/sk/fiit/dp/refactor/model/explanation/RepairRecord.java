package sk.fiit.dp.refactor.model.explanation;

import org.json.JSONObject;

//TODO
public class RepairRecord {
	private int id; 
	private String refactoringCode; // with number
	private String refcode; // without number
	private String path;
	private String gitRepository;
	private String codeBeforeRepair;
	private String codeAfterRepair;
	private JessListenerOutput usedJessRule;
	private String smellDescription;

	public JSONObject asJson() {
		JSONObject json = new JSONObject();
		json.put("refactoringCode", refactoringCode);
		json.put("path", path);
		json.put("gitRepository", gitRepository);
		json.put("codeBeforeRepair", codeBeforeRepair);
		json.put("codeAfterRepair", codeAfterRepair);
		json.put("smellDescription", smellDescription);
		json.put("id",id);
		return json;
	}

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
		return smellDescription;
	}

	public void setSmellDescription(String smellDescription) {
		this.smellDescription = smellDescription;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
