package sk.fiit.dp.refactor.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQException;

import org.eclipse.jgit.api.errors.GitAPIException;

import sk.fiit.dp.refactor.command.explanation.ExplanationCommandHandler;
import sk.fiit.dp.refactor.command.explanation.ExplanationCommentHandler;
import sk.fiit.dp.refactor.command.explanation.XpathScriptModifier;
import sk.fiit.dp.refactor.command.sonarQube.SonarProperties;
import sk.fiit.dp.refactor.command.sonarQube.SonarQubeWrapper;
import sk.fiit.dp.refactor.dbs.BaseXManager;
import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.helper.IdGenerator;
import sk.fiit.dp.refactor.helper.JsonFileWriter;
import sk.fiit.dp.refactor.helper.SonarIssuesProcessor;
import sk.fiit.dp.refactor.helper.TimeStampGenerator;
import sk.fiit.dp.refactor.model.JessInput;
import sk.fiit.dp.refactor.model.JessOutput;
import sk.fiit.dp.refactor.model.SearchObject;
import sk.fiit.dp.refactor.model.SonarIssue;;

public class RefactorCommandHandler {

	private static RefactorCommandHandler INSTANCE;

	private GitCommandHandler gitCommand = GitCommandHandler.getInstance();
	private ConversionCommandHandler conversionCommand = ConversionCommandHandler.getInstance();
	private SearchCommandHandler searchCommand = SearchCommandHandler.getInstance();
	private BaseXManager baseX = BaseXManager.getInstance();
	private PostgreManager postgre = PostgreManager.getInstance();
	private RuleEngineCommandHandler ruleCommand = RuleEngineCommandHandler.getInstance();
	private ExplanationCommandHandler explainCommand = ExplanationCommandHandler.getInstance();
	private ExplanationCommentHandler explainCommentsHandler = ExplanationCommentHandler.getInstance();
	private SonarQubeWrapper sonarHandler = SonarQubeWrapper.getInstance();
	private TimeStampGenerator timeGenerator = TimeStampGenerator.getInstance();
	private SmellPathFinder smellPathFinder = SmellPathFinder.getInstance();
	private String id;

	private String sonarOutput;
	private List<SonarIssue> sonarIssues;

	private RefactorCommandHandler() {
	}

	public static RefactorCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RefactorCommandHandler();
		}

		return INSTANCE;
	}

	/**
	 * Hlavna metoda riadiaca proces refaktorovania
	 * 
	 * @param repo
	 * @param password
	 * @param name
	 * @param repairBranch
	 * @param explanationToSearch
	 * @return
	 */
	public Map<String, Integer> executeRefactoring(String repo, String name, String password, String searchBranch,
			String repairBranch, List<String> toSearch, List<String> allowedRefactoring, boolean explanationToSearch,
			boolean createRepairRecord, SonarProperties sonarProps) {
		id = "Refactor" + IdGenerator.generateId();

		try {

			// 0. time generator reset
			timeGenerator.resetTimeStamp();

			// 1. Vytvori lokalnu kopiu Git repozitara
			gitCommand.cloneRepository(repo, name, password, id);

			// TODO
			// SONAR
			if (sonarProps.isSonarEnabled()) {
				sonarHandler.setSonarProps(sonarProps);
				sonarHandler.analyzeProject(id, gitCommand.getRepoDirectory());
				sonarOutput = sonarHandler.getIssues(id);
				sonarHandler.deleteProject(id);

				sonarIssues = SonarIssuesProcessor.convertSonarOutput(sonarOutput);
				SonarIssuesProcessor.addSonarIssuesToCode(gitCommand.getRepoDirectory(), sonarIssues);

			}
			// SONAR

			// 2. Vytvori branch pre vyhladavanie
			gitCommand.createBranch(searchBranch);

			// 3. Prevedu sa Java subory do XML reprezentacie
			List<File> xmlFiles = conversionCommand.convertJavaToXml(gitCommand.getRepoDirectory());

			// 4. Pripravi sa BaseX databaze
			baseX.prepareDatabase(id);

			// 5. XML subory sa importuju do databazy
			baseX.projectToDatabase(xmlFiles);

			// NEW pripravenie vyhladavacich skriptov s vysvetlenim a exportom
			// casti kodu
			List<SearchObject> search = searchCommand.prepareSearchScripts(toSearch);

			// 6. Vykona sa vyhladavanie
			List<JessInput> searchResults = searchCommand.search(search);

			// vlozia sa znacky pre vizualizaiu
			explainCommentsHandler.insertVisualisationMarker(searchResults);

			// vlozia sa vysvetlovacie komentare
			if (explanationToSearch) {
				explainCommentsHandler.insertExplanationComments(searchResults);
			}

			// NEW vratenie ciest z databazy
			smellPathFinder.findPathsToSmells(searchResults);

			// vytvoria sa zaznamy o automatickej oprave
			
			if (createRepairRecord) {
				explainCommand.createRepairRecord(repo, searchResults);
				explainCommand.getSmellySourceCode(searchResults);
			}

			// 7. Exportuje sa databaza
			baseX.exportDatabase(gitCommand.getRepoDirectory());

			// 8. Exportovane subory sa presunu na povodnu poziciu
			conversionCommand.moveFilesToOriginalLocation(xmlFiles, gitCommand.getRepoDirectory());

			// 9. Vykona sa konverzia XML suborov do Javy
			conversionCommand.convertXmlToJava(xmlFiles);

			// NEW - SONAR - L.H.
			if (sonarProps.isSonarEnabled()) {
				JsonFileWriter.writeJsonToFile(sonarOutput, gitCommand.getRepoDirectory() + "\\sonar_output.json");
			}

			// 10. Vykona sa push search branch na git
			gitCommand.pushBranch(searchBranch, name, password);

			// 11. Pripravi sa branch pre vykonanie opravy
			gitCommand.createBranch(repairBranch);

			// 12. Pravidlovy stroj rozhodne o pouzitom refaktorovani
			List<JessOutput> requiredRefactoring = ruleCommand.run(searchResults);

			// System.out.println("---------JESS-----------------------------");
			//
			// for (JessOutput o : requiredRefactoring) {
			// System.out.println("tags: " + o.getTag());
			// System.out.println("method: " + o.getRefactoringMethod());
			// }
			// System.out.println("--------------------------------------");

			// 13. Vykona sa refaktoring
			System.out.println("---------REFACTOR-----------------------------");
			applyRefactoring(requiredRefactoring, allowedRefactoring);

			// TODO explanation
			if (createRepairRecord) {
				explainCommand.processJessListenerOutput();
				explainCommand.getRepairedSourceCode(searchResults);
				explainCommand.printrecords();
				explainCommand.pushRecordsToPostgres();

			}
			// 14. Exportuje sa databaza
			baseX.exportDatabase(gitCommand.getRepoDirectory());

			// 15. Exportovane subory sa presunu na povodnu poziciu
			conversionCommand.moveFilesToOriginalLocation(xmlFiles, gitCommand.getRepoDirectory());

			// 16. Vykona sa konverzia XML suborov do Javy
			conversionCommand.convertXmlToJava(xmlFiles);

			// 17. Vykona sa push repair branch na git
			gitCommand.pushBranch(repairBranch, name, password);

			// 18. Vymaze sa docasna BaseX databaza TODO
			// baseX.cleanDatabase(id);

			// 19. Odstrani sa lokalna git kopia
			gitCommand.deleteLocalDirectory();

			return searchCommand.processResults(searchResults);
		} catch (IOException | GitAPIException | InterruptedException | XQException | SQLException e) {
			e.printStackTrace();
		}

		return new HashMap<String, Integer>();

	}

	/**
	 * Aplikuju sa povolene refaktorovacie operacie podla vystupu z Jess
	 * expertneho systemu
	 * 
	 * @param requiredRefactoring
	 * @param allowedRefactoring
	 * @throws SQLException
	 * @throws XQException
	 */
	public void applyRefactoring(List<JessOutput> requiredRefactoring, List<String> allowedRefactoring)
			throws SQLException, XQException {
		for (JessOutput refactoring : requiredRefactoring) {
			if (allowedRefactoring.contains(refactoring.getRefactoringMethod())) {
				String script = postgre.getRepairScript(refactoring.getRefactoringMethod());

				if (script != null && !"".equals(script)) {
					baseX.applyRepairXQuery(script, "tag", refactoring.getTag());
				}
			}
		}
	}

}
