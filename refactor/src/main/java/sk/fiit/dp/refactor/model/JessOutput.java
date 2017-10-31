package sk.fiit.dp.refactor.model;

/**
 * Vystup expertneho systemu
 * @author Lukas
 *
 */
public class JessOutput {
	private String refactoringMethod;
	
	private String tag;
	
	public JessOutput(String tag, String refactoringMethod) {
		super();
		this.refactoringMethod = refactoringMethod;
		this.tag = tag;
	}
	
	public String getRefactoringMethod() {
		return refactoringMethod;
	}

	public void setRefactoringMethod(String refactoringMethod) {
		this.refactoringMethod = refactoringMethod;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
