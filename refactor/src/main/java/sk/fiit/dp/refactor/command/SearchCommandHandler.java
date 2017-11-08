package sk.fiit.dp.refactor.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQException;

import sk.fiit.dp.refactor.command.explanation.XpathScriptModifier;
import sk.fiit.dp.refactor.dbs.BaseXManager;
import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.model.JessInput;
import sk.fiit.dp.refactor.model.SearchObject;

public class SearchCommandHandler {

	private static SearchCommandHandler INSTANCE;

	private PostgreManager postgre = PostgreManager.getInstance();
	private BaseXManager baseX = BaseXManager.getInstance();
	private GitCommandHandler git = GitCommandHandler.getInstance();

	private SearchCommandHandler() {
	}

	public static SearchCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SearchCommandHandler();
		}

		return INSTANCE;
	}

	/**
	 * Pridanie vysvetlenia do vyhladavacieho skriptu
	 * 
	 * @param searchRequest
	 * @param withExplanation
	 * @return
	 * @throws SQLException
	 */
	public List<SearchObject> prepareSearchScripts(List<String> searchRequest, boolean withExplanation,
			boolean exprortNode) throws SQLException {
		List<SearchObject> preparedSearchObjects = postgre.loadActiveSearch(searchRequest);
		if (withExplanation) {
			XpathScriptModifier.getInstance().addexplanation(preparedSearchObjects);
		}
		if (exprortNode) {
			XpathScriptModifier.getInstance().addOutputCommand(preparedSearchObjects);
		}
		return preparedSearchObjects;
	}

	/**
	 * Vyhladavania antivzorov v kode
	 * 
	 * @param searchRequest
	 * @return
	 * @throws SQLException
	 * @throws XQException
	 * @throws IOException
	 */
	public List<JessInput> search(List<SearchObject> searchObjects, boolean exprortNode)
			throws XQException, IOException {
		applySearch(searchObjects, exprortNode);
		return processSearchResults();
	}

	/**
	 * Vykonanie jednotlivych vyhladavacich pravidiel
	 * 
	 * @param searchObjects
	 * @throws XQException
	 */
	private void applySearch(List<SearchObject> searchObjects, boolean exprortNode) throws XQException {
		for (SearchObject search : searchObjects) {
			String script = search.getScript();
			baseX.applySearchXQuery(script, "resultFile", git.getRepoDirectory() + "\\Result.txt", exprortNode);
		}
	}

	/**
	 * Spracovanie vysledkov vyhladavania ulozenych v informacnom subore
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<JessInput> processSearchResults() throws IOException {
		List<JessInput> searchResults = new ArrayList<>();

		List<String> lines = Files.readAllLines(Paths.get(git.getRepoDirectory() + "\\Result.txt"));
		JessInput result = null;
		for (String line : lines) {
			if (line.startsWith("NAME: ")) {
				if (result != null) {
					searchResults.add(result);
				}
				result = new JessInput();
				result.setCode(line.substring(line.indexOf(" ") + 1));

				int i;
				for (i = 0; i < result.getCode().length(); ++i) {
					if (Character.isDigit(result.getCode().charAt(i))) {
						break;
					}
				}

				result.setRefCode(result.getCode().substring(0, i));
			} else if (line.startsWith("PATH: ")) {
				String cleanPath = line.substring(line.indexOf(" ") + 1);
				String[] parents = cleanPath.split("/");
				result.setParents(Arrays.asList(parents));
			} else if (line.startsWith("SIZE: ")) {
				result.setSize(Integer.valueOf(line.substring(line.indexOf(" ") + 1)));
			}

		}
		if (lines.get(lines.size() - 1).startsWith("NAME: ")) {
			result = new JessInput();
			result.setCode(lines.get(lines.size() - 1).substring(lines.get(lines.size() - 1).indexOf(" ") + 1));
			int i;
			for (i = 0; i < result.getCode().length(); ++i) {
				if (Character.isDigit(result.getCode().charAt(i))) {
					break;
				}
			}
			result.setRefCode(result.getCode().substring(0, i));
			searchResults.add(result);
		}

		return searchResults;

	}

	/**
	 * Spracovania vysledkov vyhladavania pre zobrazenie na webovom rozhrani
	 * 
	 * @param searchResults
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Integer> processResults(List<JessInput> searchResults) throws SQLException {
		Map<String, Integer> results = new HashMap<>();

		for (JessInput result : searchResults) {
			String name = postgre.getSearchNameByCode(result.getRefCode());
			if (results.containsKey(name)) {
				results.replace(name, results.get(name) + 1);
			} else {
				results.put(name, 1);
			}
		}

		return results;
	}
}