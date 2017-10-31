package sk.fiit.dp.refactor.helper;

public enum Str {
	RESOURCES("src/main/resources"),
	SCRIPTS("src/main/scripts"),
	CONFIG("src/main/config"),
	SRCML("srcML/srcml"),
	
	RULES("JESS_RULES.clp");
	
	private final String value; 
	
	private Str(final String value) {
		this.value = value;
	}
	
	public String val() {
		return value;
	}
}