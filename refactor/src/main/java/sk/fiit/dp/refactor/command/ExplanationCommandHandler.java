package sk.fiit.dp.refactor.command;

public class ExplanationCommandHandler {
	private static ExplanationCommandHandler INSTANCE;

	private ExplanationCommandHandler() {
	}

	public static ExplanationCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ExplanationCommandHandler();
		}
		return INSTANCE;
	}

	public void explain() {
	}

	public void processSearchExplanationFile() {
		
	}


}
