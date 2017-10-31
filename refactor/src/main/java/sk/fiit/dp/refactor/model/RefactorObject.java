package sk.fiit.dp.refactor.model;

import org.json.JSONObject;

/**
 * Vseobecna trieda pre opravovacie a refaktorovacie objekty
 * @author Lukas
 *
 */
public abstract class RefactorObject {
	
	private String code;
	
	private String name;
	
	private String script;
	
	private String explanation; 
	
	public RefactorObject(String code, String name, String script, String explanation) {
		super();
		this.code = code;
		this.name = name;
		this.script = script;
		this.explanation = explanation;
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
		if(script == null) {
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
}