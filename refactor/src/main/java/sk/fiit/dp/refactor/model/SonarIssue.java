package sk.fiit.dp.refactor.model;

public class SonarIssue {
	Integer startLine; 
	Integer endLine; 
	String message;
	String file;
	Integer id;
		
	public SonarIssue(){
		
	}
	
	public SonarIssue(Integer id, Integer startLine, Integer endLine, String message, String file) {
		this.id = id; 
		this.startLine = startLine;
		this.endLine = endLine;
		this.message = message;
		this.file = file;
	}
	
	public Integer getStartLine() {
		return startLine;
	}
	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}
	public Integer getEndLine() {
		return endLine;
	}
	public void setEndLine(Integer endLine) {
		this.endLine = endLine;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
}
