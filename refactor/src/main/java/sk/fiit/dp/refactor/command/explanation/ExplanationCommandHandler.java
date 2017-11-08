package sk.fiit.dp.refactor.command.explanation;

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

	// TODO
	public void createRepairRecord() {
	}
	
	// TODO
	private void processSearchExplanationFile() {
	}

	// TODO
	private void processRepairExplanationFile() {
	}

	// TODO
	private void processJessListenerOutput() {
	}

}
