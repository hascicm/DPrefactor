package sk.fiit.dp.refactor.model.explanation;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

public class RepairRecord {
	private int id;
	private String refactoringCode; // with number
	private String refcode; // without number
	private String path;
	private String gitRepository;
	private String codeBeforeRepair;
	private String codeAfterRepair;
	private JessListenerOutput usedJessRule;
	private String smellName;
	private String smellDescription;
	private String possibleRepairs;
	private long timeStamp;
	
	private JSONArray LocationJSON;

	public JSONObject asJson() {
		JSONObject json = new JSONObject();
		json.put("refactoringCode", refactoringCode);
		json.put("path", LocationJSON);
		json.put("gitRepository", gitRepository);
		json.put("codeBeforeRepair", codeBeforeRepair);
		json.put("codeAfterRepair", codeAfterRepair);
		json.put("smellName", smellName);
		json.put("smellDescription", smellDescription);
		json.put("possibleRepairs", possibleRepairs);

		Date date = new Date(timeStamp);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		json.put("time", sdf.format(date));
		if (usedJessRule != null) {
			json.put("jessname", usedJessRule.getRuleName());
			json.put("jessdesc", usedJessRule.getDocString());
		} else {
			json.put("jessname", "nenašlo sa žiadne pravidlo");
			json.put("jessdesc", "nenašlo sa žiadne pravidlo");
		}
		json.put("id", id);
		return json;
	}

	public String getSmellName() {
		return smellName;
	}

	public void setSmellName(String smellName) {
		this.smellName = smellName;
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

	public String getPossibleRepairs() {
		return possibleRepairs;
	}

	public void setPossibleRepairs(String possibleRepairs) {
		this.possibleRepairs = possibleRepairs;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public JSONArray getLocationJSON() {
		return LocationJSON;
	}

	public void setLocationJSON(JSONArray locationJSON) {
		LocationJSON = locationJSON;
	}

}
