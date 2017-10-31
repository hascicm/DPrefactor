package sk.fiit.dp.refactor.command;

import java.util.List;
import java.util.Scanner;

import sk.fiit.dp.refactor.model.SearchObject;

public class ExplanationHandler {
	private static ExplanationHandler INSTANCE;

	private ExplanationHandler() {
	}

	public static ExplanationHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ExplanationHandler();
		}

		return INSTANCE;
	}

	public void addexplanation(List<SearchObject> search) {

		for (SearchObject s : search) {
			Scanner scanner = new Scanner(s.getScript());
			String scriptExplanation = new String();

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (line.contains("REFACTOR")) {
					scriptExplanation += "(<comment type=\"line\">\n//EXPANATION " + s.getExplanation() + " "
							+"\n</comment>,";
					scriptExplanation += line + "\n" + scanner.nextLine() + ")\n ";
				} else {
					scriptExplanation += line + "\n";
				}
			}
			scanner.close();
			s.setScript(scriptExplanation);
		}

	}

}
