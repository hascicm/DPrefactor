package sk.fiit.dp.refactor.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;

import sk.fiit.dp.pathFinder.clustering.ClusteringHandler;
import sk.fiit.dp.pathFinder.clustering.model.Cluster;
import sk.fiit.dp.pathFinder.configuration.PathFinderHandler;
import sk.fiit.dp.pathFinder.dataprovider.DatabaseDataProvider;
import sk.fiit.dp.pathFinder.dataprovider.dbsManager.PostgresManager;
import sk.fiit.dp.pathFinder.entities.OptimalPathForCluster;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.refactor.command.explanation.ExplanationCommandHandler;
import sk.fiit.dp.refactor.command.sonarQube.SonarProperties;
import sk.fiit.dp.refactor.command.sonarQube.SonarQubeWrapper;
import sk.fiit.dp.refactor.dbs.BaseXManager;
import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.helper.IdGenerator;
import sk.fiit.dp.refactor.helper.JsonFileWriter;
import sk.fiit.dp.refactor.helper.TimeStampGenerator;
import sk.fiit.dp.refactor.model.JessInput;
import sk.fiit.dp.refactor.model.SearchObject;

public class PathFinderCommandHandler {

	private static PathFinderCommandHandler INSTANCE;

	private GitCommandHandler gitCommand = GitCommandHandler.getInstance();
	private ConversionCommandHandler conversionCommand = ConversionCommandHandler.getInstance();
	private SearchCommandHandler searchCommand = SearchCommandHandler.getInstance();
	private BaseXManager baseX = BaseXManager.getInstance();
	private PostgreManager postgre = PostgreManager.getInstance();
	private RuleEngineCommandHandler ruleCommand = RuleEngineCommandHandler.getInstance();
	private ExplanationCommandHandler explainCommand = ExplanationCommandHandler.getInstance();
	private SonarQubeWrapper sonarHandler = SonarQubeWrapper.getInstance();
	private TimeStampGenerator timeGenerator = TimeStampGenerator.getInstance();
	private SmellPathFinder smellPathFinder = SmellPathFinder.getInstance();
	private String id;

	private String sonarOutput;

	private PathFinderCommandHandler() {
	}

	public static PathFinderCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PathFinderCommandHandler();
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
	public List<OptimalPathForCluster> executePathFinder(String repo, String name, String password, String searchBranch,
			List<String> toSearch, boolean explanationToSearch, boolean clusteringEnabled, SonarProperties sonarProps,
			String method) {
		id = "Refactor" + IdGenerator.generateId();
		
		try {
			// 0. time stamp generator reset
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
				System.out.println(sonarOutput);
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

			// NEW pripravenie vyhladavacich skriptov s vysvetlenim
			List<SearchObject> search = searchCommand.prepareSearchScripts(toSearch, explanationToSearch, false);

			// 6. Vykona sa vyhladavanie
			List<JessInput> searchResults = searchCommand.search(search, false);

			// NEW vratenie polohy pachov
			smellPathFinder.findPathsToSmells(searchResults);

			// System.out.println("------------SEARCH--------------------------");
			//
			// for (JessInput o : searchResults) {
			// System.out.println("tags: " + o.getCode());
			// System.out.println("refcode: " + o.getRefCode());
			// System.out.println("position " + o.getPosition());
			// System.out.println("xpathpos " + o.getXpatPosition());
			// }
			// System.out.println("--------------------------------------");

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

			// TODO explanation
			// if (createRepairRecord) {
			// explainCommand.createRepairRecord(repo, searchResults);
			// }

			// 18. Vymaze sa docasna BaseX databaza
			baseX.cleanDatabase(id);

			// 19. Odstrani sa lokalna git kopia
			gitCommand.deleteLocalDirectory();

			List<State> rootStates;

			// vykona sa rozdelenie stavoveho priestoru na mensie zhluky
			// (clustering)
			if (clusteringEnabled) {
				System.out.println("clustering strarting");
				rootStates = ClusteringHandler.executeClustering(searchResults);
			} else {
				System.out.println("clustering not enabled");
				rootStates = DatabaseDataProvider.getInstance().prepareRootStateList(searchResults);
				
			}

			// vykoná sa hľadanie optimálnej cesty
			List<OptimalPathForCluster> results = PathFinderHandler.executePathFinder(rootStates, method);

//			for (OptimalPathResult current : results) {
//				System.out.println("------------------CLUSTER----------------");
//				System.out.println(current.getRootState().toString());
//					for (Relation r : current.getOptimalPath()) {
//						System.out.println("repair:" + r.getUsedRepair().getName() + " on: "
//								+ r.getFixedSmellOccurance().getSmell().getName());
//					}
//			}
			
			PostgresManager.getInstance().addResultRecord(repo,name,timeGenerator.getTime(),results);
			
//			for (OptimalPathForCluster r : results){
//				System.out.println("-------------------------\n-------------------------\n-------------------------\n       CLUSTER        \n-------------------------\n-------------------------\n-------------------------\n");
//				System.out.println(r.toJSON().toString());
//			}
			
			return results;
		} catch (IOException | GitAPIException | InterruptedException | XQException | SQLException e) {
			e.printStackTrace();
		}
		return new ArrayList<OptimalPathForCluster>();
	}
}
