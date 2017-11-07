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
					System.out.println("adding explanation to search script " + s.getName());
					scriptExplanation += "(<comment type=\"line\">\n//EXPANATION " + s.getExplanation() + " "
							+ "\n</comment>,";
					scriptExplanation += line + "\n" + scanner.nextLine() + ")\n ";
				} else {
					scriptExplanation += line + "\n";
				}
			}
			scanner.close();
			s.setScript(scriptExplanation);
		}

	}

	public void addOutputCommand(List<SearchObject> search) {
		for (SearchObject s : search) {
			Scanner scanner = new Scanner(s.getScript());
			String scriptExplanation = new String();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("declare variable")) {
					scriptExplanation += "declare variable $explanation external;\n";
					scriptExplanation += line + "\n";
				} else if (line.equals(")")) {
					scriptExplanation += ",file:append($explanation, concat(\"NAME: \", $code, $position ,\"&#10;\",$node , \"&#10;\"))";
					scriptExplanation += line + "\n";
				} else {
					scriptExplanation += line + "\n";
				}

			}
			scanner.close();
			s.setScript(scriptExplanation);
		}
	}

	public String addRefactoringExplanation(String script) {
		Scanner scanner = new Scanner(script);
		String scriptWithOutput = new String();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			scriptWithOutput += line + "\n";
		}

		return scriptWithOutput;
	}

}
