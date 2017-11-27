package sk.fiit.dp.refactor.model;

import org.json.JSONObject;

/**
 * Vseobecna trieda pre opravovacie a refaktorovacie objekty
 * 
 * @author Lukas
 *
 */
public abstract class RefactorObject {

	private String code;

	private String name;

	private String script;

	private String explanation;

	private String position;
	
	//for search scripts
	public RefactorObject(String code, String name, String script, String explanation, String position) {
		super();
		this.code = code;
		this.name = name;
		this.script = script;
		this.explanation = explanation;
		this.setPosition(position);
	}
	//for repair scripts
	public RefactorObject(String code2, String name2, String script2, String explanation2) {
		super();
		this.code = code2;
		this.name = name2;
		this.script = script2;
		this.explanation = explanation2;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScript() {
		if (script == null) {
			script = "";
		}

		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public JSONObject asJson() {
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("name", name);
		json.put("script", script);

		return json;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
}