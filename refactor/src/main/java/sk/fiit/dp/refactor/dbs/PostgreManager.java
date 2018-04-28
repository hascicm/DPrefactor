package sk.fiit.dp.refactor.dbs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import sk.fiit.dp.pathFinder.configuration.LocationProcessor;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.refactor.dbs.connector.PotgreConnector;
import sk.fiit.dp.refactor.model.RepairObject;
import sk.fiit.dp.refactor.model.SearchObject;
import sk.fiit.dp.refactor.model.explanation.JessListenerOutput;
import sk.fiit.dp.refactor.model.explanation.RepairRecord;

public class PostgreManager {

	private static PostgreManager INSTANCE;
	private PotgreConnector connector;
	private Statement statement;

	public static PostgreManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PostgreManager();
		}

		return INSTANCE;
	}

	public PostgreManager() {
		try {
			connector = new PotgreConnector();
			statement = connector.getStatement();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ziska vsetky vyhladavacie objekty
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<SearchObject> getSearchObjects() throws SQLException {
		List<SearchObject> results = new ArrayList<>();
		String query = "SELECT * FROM search";
		ResultSet rs = statement.executeQuery(query);

		while (rs.next()) {
			SearchObject result = new SearchObject(rs.getString("code"), rs.getString("name"), rs.getString("script"),
					rs.getString("explanation"), rs.getString("position"));
			results.add(result);
		}

		return results;
	}

	/**
	 * Ziska vyhladavaci skript podla kodu
	 * 
	 * @param code
	 * @return
	 * @throws SQLException
	 */
	public String getSearchScript(String code) throws SQLException {
		String query = "SELECT script FROM search WHERE code = '" + code + "'";
		ResultSet rs = statement.executeQuery(query);

		rs.next();
		String result = rs.getString("script");
		return result == null ? "" : result;
	}

	/**
	 * Ziska vsetky opravovacie objekty
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<RepairObject> getRepairObjects() throws SQLException {
		List<RepairObject> results = new ArrayList<>();
		String query = "SELECT * FROM refactor";
		ResultSet rs = statement.executeQuery(query);

		while (rs.next()) {
			RepairObject result = new RepairObject(rs.getString("code"), rs.getString("name"), rs.getString("script"),
					rs.getString("explanation"));
			results.add(result);
		}

		return results;
	}

	/**
	 * Ziska opravovaci skipt podla kodu
	 * 
	 * @param code
	 * @return
	 * @throws SQLException
	 */
	public String getRepairScript(String code) throws SQLException {
		String query = "SELECT script FROM refactor WHERE code = '" + code + "'";
		ResultSet rs = statement.executeQuery(query);

		rs.next();
		String result = rs.getString("script");
		return result == null ? "" : result;
	}

	public String getExplanationForScript(String code) throws SQLException {
		String query = "SELECT explanation FROM refactor WHERE code = '" + code + "'";
		ResultSet rs = statement.executeQuery(query);

		rs.next();
		String result = rs.getString("explanation");
		return result == null ? "" : result;
	}

	/**
	 * Aktualizacia opravovacieho skriptu
	 * 
	 * @param code
	 * @param script
	 * @throws SQLException
	 */
	public void updateRepairScript(String code, String script) throws SQLException {
		String query = "UPDATE refactor SET script = '" + script + "' WHERE code = '" + code + "'";
		statement.executeUpdate(query);
	}

	/**
	 * Ziskania nazvu pachu podla kodu
	 * 
	 * @param code
	 * @return
	 * @throws SQLException
	 */
	public String getSearchNameByCode(String code) throws SQLException {
		String query = "Select name FROM search WHERE code = '" + code + "'";
		ResultSet rs = statement.executeQuery(query);

		rs.next();
		String result = rs.getString("name");
		return result;
	}

	/**
	 * Aktualizacia vyhladavacieho skriptu
	 * 
	 * @param code
	 * @param script
	 * @throws SQLException
	 */
	public void updateSearchScript(String code, String script) throws SQLException {
		String query = "UPDATE search SET script = '" + script + "' WHERE code = '" + code + "'";
		statement.executeUpdate(query);
	}

	/**
	 * Pridanie vyhladavacieho skriptu
	 * 
	 * @param code
	 * @param name
	 * @param script
	 * @throws SQLException
	 */
	public void addSearchScript(String code, String name, String script) throws SQLException {
		String query = "INSERT INTO search VALUES('" + code + "', '" + name + "', '" + script + "')";
		statement.executeUpdate(query);
	}

	/**
	 * Pridanie opravovacieho skriptu
	 * 
	 * @param code
	 * @param name
	 * @param script
	 * @throws SQLException
	 */
	public void addRepairScript(String code, String name, String script) throws SQLException {
		String query = "INSERT INTO refactor VALUES('" + code + "', '" + name + "', '" + script + "')";
		statement.executeUpdate(query);
	}

	/**
	 * Nacitanie vsetkych aktivnych vyhladavacich objektov
	 * 
	 * @param searchRequest
	 * @return
	 * @throws SQLException
	 */
	public List<SearchObject> loadActiveSearch(List<String> searchRequest) throws SQLException {
		List<SearchObject> results = new ArrayList<>();
		String inString = "(";
		System.out.println(inString);
		for (String s : searchRequest) {
			inString = inString + "'" + s + "',";
		}

		System.out.println(inString);
		inString = inString.substring(0, inString.length() - 1);
		System.out.println(inString);
		inString = inString + ")";
		System.out.println(inString);
		String query = "SELECT * FROM search WHERE code IN " + inString + " AND script IS NOT NULL AND script != ''";
		ResultSet rs = statement.executeQuery(query);

		while (rs.next()) {
			SearchObject result = new SearchObject(rs.getString("code"), rs.getString("name"), rs.getString("script"),
					rs.getString("explanation"), rs.getString("position"));
			results.add(result);
		}

		return results;
	}

	/**
	 * vlozenie zaznamu o oprave
	 * 
	 * @param RepairRecord
	 * @return
	 * @throws SQLException
	 */
	public void AddRepairRecord(RepairRecord record) throws SQLException {
		String query = "select * from smelltype where code = '" + record.getRefcode() + "'";
		ResultSet rs = statement.executeQuery(query);
		int smelltypeid = 0;
		while (rs.next()) {
			smelltypeid = rs.getInt("id");
		}
		String jessObject = "ROW('','')";
		if (record.getUsedJessRule() != null) {
			jessObject = "ROW('" + record.getUsedJessRule().getRuleName() + "','"
					+ record.getUsedJessRule().getDocString() + "')";
		} else {
		}
		String columns = "(gitreponame, refactoringcode, path, beforerepair,afterrepair,jessdecision,smelltype_id,timestamp)";
		query = "INSERT INTO records " + columns + "  VALUES('" + record.getGitRepository() + "','"
				+ record.getRefactoringCode() + "','" + record.getPath() + "','" + record.getCodeBeforeRepair() + "','"
				+ record.getCodeAfterRepair() + "'," + jessObject + ",'" + smelltypeid + "','" + record.getTimeStamp()
				+ "')";
		statement.executeUpdate(query);

	}

	public List<RepairRecord> getRepairRecords() throws SQLException {
		List<RepairRecord> records = new ArrayList<>();

		String query = "select * from records r join smelltype s on s.id=r.smelltype_id order by timestamp desc";
		ResultSet rs = statement.executeQuery(query);
		while (rs.next()) {
			RepairRecord act = new RepairRecord();
			act.setId(rs.getInt("id"));
			act.setGitRepository(rs.getString("gitreponame"));
			act.setPath(rs.getString("path"));
			act.setRefactoringCode(rs.getString("refactoringcode"));
			act.setCodeBeforeRepair(rs.getString("beforerepair"));
			act.setCodeAfterRepair(rs.getString("afterrepair"));
			act.setSmellDescription(rs.getString("description"));
			act.setSmellName(rs.getString("name"));
			act.setTimeStamp(rs.getLong("timestamp"));
			records.add(act);
		}
		return records;
	}

	public RepairRecord getRepairRecord(int id) throws SQLException {
		String query = "select "
				+ "r.id,timestamp,gitreponame,path,refactoringcode,beforerepair,afterrepair,name,description,(jessdecision).rulename as jessname, (jessdecision).docstring as jessdesc"
				+ " from records r join smelltype s on s.id=r.smelltype_id where r.id = " + id;
		ResultSet rs = statement.executeQuery(query);

		RepairRecord act = new RepairRecord();
		rs.next();
		act.setId(rs.getInt("id"));
		act.setGitRepository(rs.getString("gitreponame"));
		List<Location> locationObjects = LocationProcessor.processLocationString(rs.getString("path"));
		act.setLocationJSON(createLocationJSON(locationObjects));
		act.setPath(rs.getString("path"));
		act.setRefactoringCode(rs.getString("refactoringcode"));
		act.setCodeBeforeRepair(rs.getString("beforerepair"));
		act.setCodeAfterRepair(rs.getString("afterrepair"));
		act.setSmellDescription(rs.getString("description"));
		act.setSmellName(rs.getString("name"));
		act.setTimeStamp(rs.getLong("timestamp"));

		act.setUsedJessRule(new JessListenerOutput(rs.getString("jessname"), rs.getString("jessdesc")));

		act.setPossibleRepairs(getPossibleRepairForSmellbyRecordId(id));

		return act;
	}

	private JSONArray createLocationJSON(List<Location> locationObjects) {
		JSONArray result = new JSONArray();
		for (Location location : locationObjects) {
			JSONObject currLoc = new JSONObject();
			String pack = "";
			String clas = "";
			String metd = "";
			for (LocationPart locationPart : location.getLocation()) {

				if (locationPart.getLocationPartType() == LocationPartType.PACKAGE) {
					if (pack.equals("")) {
						pack = locationPart.getId();
					} else {
						pack = pack + "." + locationPart.getId();
					}
				} else if (locationPart.getLocationPartType() == LocationPartType.CLASS) {
					if (clas.equals("")) {
						clas = locationPart.getId();
					} else {
						clas = clas + " nested class: " + locationPart.getId();
					}
				} else if (locationPart.getLocationPartType() == LocationPartType.METHOD) {
					metd = locationPart.getId();
				}
			}
			currLoc.put("package", pack);
			currLoc.put("class", clas);
			currLoc.put("method", metd);
			result.put(currLoc);
		}
		return result;
	}

	public String getPossibleRepairForSmellbyRecordId(int id) throws SQLException {
		String possibleRepairs = "";
		String query = "select re.name from records r join smelltype s on s.id=r.smelltype_id "
				+ "join repairsmelltype rs on s.id = rs.smell_id join repair re on re.id = rs.repair_id "
				+ "where r.id = " + id;
		ResultSet rs = statement.executeQuery(query);
		while (rs.next()) {
			possibleRepairs += rs.getString("name").trim() + ", ";
		}
		rs.close();

		return possibleRepairs;
	}

	public String getPossibleRepairForSmellbySmellId(int id) throws SQLException {
		String possibleRepairs = "";
		String query = "select r.name from smelltype s join repairsmelltype rs on s.id = rs.smell_id "
				+ "join repair r on r.id = rs.repair_id where s.id = " + id;
		ResultSet rs = statement.executeQuery(query);
		while (rs.next()) {
			possibleRepairs += rs.getString("name").trim() + ", ";
		}
		rs.close();

		return possibleRepairs;
	}

	public String getExplanationForSearchScript(String code) throws SQLException {
		String result = "";
		String query = "select * from smelltype where code = '" + code + "'";
		ResultSet rs = statement.executeQuery(query);
		int id = 0;
		while (rs.next()) {
			id = rs.getInt("id");

			result += createComment("//EXPLANATION smellname : " + rs.getString("name"));
			result += createComment("\n//EXPLANATION description :" + rs.getString("description"));
		}
		rs.close();
		if (id != 0)
			result += createComment(
					"\n//EXPLANATION possible repairs :" + getPossibleRepairForSmellbySmellId(id) + "\n");
		return result;
	}

	private static String createComment(String c) {
		String queryPart = " <comment type = \"line\">" + c + " </comment>";
		return queryPart;
	}

	public String getSmellLocalisatorScript(String code) throws SQLException {
		String query = "select code,sl.script from search s join smelllocalisator sl on sl.scope=s.position where code = '"
				+ code + "'";
		ResultSet rs = statement.executeQuery(query);
		rs.next();
		String script = rs.getString("script");
		return script;

	}

}