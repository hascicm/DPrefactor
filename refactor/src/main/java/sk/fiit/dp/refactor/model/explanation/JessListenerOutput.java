package sk.fiit.dp.refactor.model.explanation;

public class JessListenerOutput {
	private String code;
	private String refCode;
	private String ruleName;
	private String docString;

	public JessListenerOutput(String code, String refCode, String ruleName, String docString) {
		this.code = code;
		this.refCode = refCode;
		this.ruleName = ruleName;
		this.docString = docString;
	}

	public JessListenerOutput(String ruleName, String docString) {
		this.ruleName = ruleName;
		this.docString = docString;
	}

	public String getCode() {
		return code;
	}

	public String getRefCode() {
		return refCode;
	}

	public String getRuleName() {
		return ruleName;
	}

	public String getDocString() {
		return docString;
	}

}
