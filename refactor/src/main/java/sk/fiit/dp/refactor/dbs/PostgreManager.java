package sk.fiit.dp.refactor.dbs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.refactor.dbs.connector.PotgreConnector;
import sk.fiit.dp.refactor.model.RepairObject;
import sk.fiit.dp.refactor.model.SearchObject;
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
					rs.getString("explanation"));
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
					rs.getString("explanation"));
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
			System.out.println("pgtest  " + record.getRefactoringCode() + "  " + smelltypeid);
		}
		String jessObject = "ROW('" + record.getUsedJessRule().getRuleName() + "','"
				+ record.getUsedJessRule().getDocString() + "')";
		String columns = "(gitreponame, refactoringcode, path, beforerepair,afterrepair,jessdecision,smelltype_id)";
		query = "INSERT INTO records " + columns + "  VALUES('" + record.getGitRepository() + "','"
				+ record.getRefactoringCode() + "','" + record.getPath() + "','" + record.getCodeBeforeRepair() + "','"
				+ record.getCodeAfterRepair() + "'," + jessObject + ",'" + smelltypeid + "')";
		statement.executeUpdate(query);

	}
}