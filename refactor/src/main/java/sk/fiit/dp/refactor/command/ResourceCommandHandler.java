package sk.fiit.dp.refactor.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import sk.fiit.dp.pathFinder.dataprovider.dbsManager.PostgresManager;
import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.helper.Resources;
import sk.fiit.dp.refactor.helper.Str;
import sk.fiit.dp.refactor.model.RepairObject;
import sk.fiit.dp.refactor.model.SearchObject;
import sk.fiit.dp.refactor.model.explanation.RepairRecord;

public class ResourceCommandHandler {
	private static ResourceCommandHandler INSTANCE;

	private ResourceCommandHandler() {
	}

	public static ResourceCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ResourceCommandHandler();
		}

		return INSTANCE;
	}

	/**
	 * Vratenie Jess pravidiel
	 * 
	 * @return
	 */
	public String getRulesDefinition() {
		try {
			byte[] content = Files.readAllBytes(Resources.getPath(Str.RULES.val()));
			return new String(Base64.getEncoder().encode(content));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Aktualizacia Jess pravidiel
	 * 
	 * @param input
	 */
	public void setRules(String input) {
		Path resourcePath = Resources.getPath(Str.RULES.val());

		try {
			Files.write(resourcePath, input.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Vratenie vyhladavacich operacii
	 * 
	 * @return
	 */
	public String getSearchMethods() {
		JSONArray methods = new JSONArray();

		List<SearchObject> searchObjects;
		try {
			searchObjects = PostgreManager.getInstance().getSearchObjects();
			for (SearchObject searchObject : searchObjects) {
				methods.put(searchObject.asJson());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return methods.toString();
	}

	/**
	 * Vratenie refaktorovacich operacii
	 * 
	 * @return
	 */
	public String getRefactoringMethods() {
		JSONArray methods = new JSONArray();

		List<RepairObject> repairObjects;
		try {
			repairObjects = PostgreManager.getInstance().getRepairObjects();
			for (RepairObject repairObject : repairObjects) {
				methods.put(repairObject.asJson());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return methods.toString();
	}

	public String getRefactoringRecords() {
		JSONArray records = new JSONArray();

		List<RepairRecord> repairRecords;
		try {
			repairRecords = PostgreManager.getInstance().getRepairRecords();
			for (RepairRecord r : repairRecords) {
				records.put(r.asJson());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records.toString();
	}

	public String getRefactoringRecord(int id) {
		JSONObject result = new JSONObject();
		RepairRecord r = null;
		try {
			r = PostgreManager.getInstance().getRepairRecord(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		result = r.asJson();

		return result.toString();
	}

	/**
	 * Vratenie vyhladavacieho skriptu podla kodu
	 * 
	 * @param code
	 * @return
	 */
	public String getSearchScript(String code) {
		JSONObject result = new JSONObject();
		String script;
		try {
			script = PostgreManager.getInstance().getSearchScript(code);
			result.put("value", new String(Base64.getEncoder().encode(script.getBytes())));
			return result.toString();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Vratenie opravovacieho skriptu podla kodu
	 * 
	 * @param code
	 * @return
	 */
	public String getRepairScript(String code) {
		JSONObject result = new JSONObject();
		String script;
		try {
			script = PostgreManager.getInstance().getRepairScript(code);
			result.put("value", new String(Base64.getEncoder().encode(script.getBytes())));
			return result.toString();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Aktualizacia opravovacieho skriptu podla kodu
	 * 
	 * @param code
	 * @param input
	 */
	public void setRepairScript(String code, String input) {
		JSONObject json = new JSONObject(input);
		String script = (String) json.get("script");

		try {
			PostgreManager.getInstance().updateRepairScript(code, script);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Aktualiz8cia vyhladavacieho skriptu podla kodu
	 * 
	 * @param code
	 * @param input
	 */
	public void setSearchScript(String code, String input) {
		JSONObject json = new JSONObject(input);
		String script = (String) json.get("script");

		try {
			PostgreManager.getInstance().updateSearchScript(code, script);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pridanie noveho vyhladavacieho skriptu
	 * 
	 * @param input
	 */
	public void addSearchScript(String input) {
		JSONObject json = new JSONObject(input);
		String script = (String) json.get("script");
		String code = (String) json.get("code");
		String name = (String) json.get("name");

		try {
			PostgreManager.getInstance().addSearchScript(code, name, script);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pridanie noveho opravovacieho skriptu
	 * 
	 * @param input
	 */
	public void addRepairScript(String input) {
		JSONObject json = new JSONObject(input);
		String script = (String) json.get("script");
		String code = (String) json.get("code");
		String name = (String) json.get("name");

		try {
			PostgreManager.getInstance().addRepairScript(code, name, script);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getPathFinderRecords() {
		JSONArray repairRecords = PostgresManager.getInstance().getPathFinderResultRecords();
		return repairRecords.toString();
	}

	public String getPathFinderRecordDetail(int id) {
		JSONObject result = PostgresManager.getInstance().getPathFinderResultRecordDetail(id);
		return result.toString();
	}

	public String getPathFinderAnalysisCluster(int analysisId, int repairNumber) {
		JSONObject result = PostgresManager.getInstance().getPathFinderAnalysisCluster(analysisId, repairNumber);
		return result.toString();
	}

	public String getPathFinderResultRepair(int clusterid, int repairid) {
		JSONObject result = PostgresManager.getInstance().getPathFinderResultRepair(clusterid,repairid);
		return result.toString();
	}

}
