package sk.fiit.dp.refactor.command.explanation;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.model.SearchObject;

public class XpathScriptModifier {
	private static XpathScriptModifier INSTANCE;
	private PostgreManager pg;

	private XpathScriptModifier() {
		pg = PostgreManager.getInstance();
	}

	public static XpathScriptModifier getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new XpathScriptModifier();
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
					scriptExplanation += "(<comment type=\"line\">\n" + prepareExplanationString(s.getCode()) + " "
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

	private String prepareExplanationString(String code) {
		String explanationString = "";
		try {
			explanationString = pg.getExplanationForSearchScript(code);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println("xpathmodifier prepared string " + explanationString);
		return explanationString;

	}

	public void addOutputCommand(List<SearchObject> search) {
		for (SearchObject s : search) {
			Scanner scanner = new Scanner(s.getScript());
			String scriptExplanation = new String();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith("declare variable")) {
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
		scanner.close();
		return scriptWithOutput;
	}

}
