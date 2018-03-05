package sk.fiit.dp.pathFinder.dataprovider.dbsManager;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.print.JobSettings;
import sk.fiit.dp.pathFinder.dataprovider.DatabaseDataProvider;
import sk.fiit.dp.pathFinder.entities.DependencyPlaceType;
import sk.fiit.dp.pathFinder.entities.DependencyRepair;
import sk.fiit.dp.pathFinder.entities.DependencyType;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.pathFinder.entities.OptimalPathForCluster;
import sk.fiit.dp.pathFinder.entities.Pattern;
import sk.fiit.dp.pathFinder.entities.PatternRepair;
import sk.fiit.dp.pathFinder.entities.PatternSmellUse;
import sk.fiit.dp.pathFinder.entities.Repair;
import sk.fiit.dp.pathFinder.entities.SmellType;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;

public class PostgresManager {

	private Statement statement;
	private Statement statement2;
	private static PostgresManager instance = null;

	public static PostgresManager getInstance() {
		if (instance == null) {
			instance = new PostgresManager();
		}
		return instance;
	}

	private PostgresManager() {
		PostgresConnector PpostgresConnector;
		try {
			PpostgresConnector = new PostgresConnector();
			statement = PpostgresConnector.getStatement();
			statement2 = PpostgresConnector.getStatement();
		} catch (ClassNotFoundException | SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}
	}

	public List<SmellType> getSmellTypes() {
		List<SmellType> smells = new ArrayList<>();
		String query = "SELECT * FROM smelltype order by id";
		ResultSet rs;
		try {
			rs = statement.executeQuery(query);
			while (rs.next()) {
				SmellType smell = new SmellType(rs.getInt("id"), rs.getString("name"), rs.getInt("weight"),
						rs.getString("code"), rs.getString("description"));
				smells.add(smell);
			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}

		return smells;
	}

	private SmellType getSmellById(int id, List<SmellType> smells) {
		for (SmellType s : smells) {
			if (s.getId() == id)
				return s;
		}
		return null;
	}

	public List<Repair> getRepairs(List<SmellType> smells) {

		List<Repair> repairs = new ArrayList<>();
		String query = "select * from (select repair.id as rapairid,name,weight,repairsmelltype.smell_id, "
				+ "'' as dependencytype,'' as actionfield,'' as locationparttype, 0 as probability " + "from repair  "
				+ "left join repairsmelltype on repair.id=repairsmelltype.repair_id union all  "
				+ "select repair.id as rapairid,name,'0' as weight,smell_id,"
				+ "dependencytype, rd.actionField,rd.locationparttype, probability  "
				+ "from repair  join repairdependencies rd on repair.id=rd.repair_id "
				+ "order by rapairid,dependencytype desc,smell_id )   as result";
		// System.out.println(query);
		ResultSet rs;
		try {
			rs = statement.executeQuery(query);
			boolean repair = false;
			Repair r = null;
			DependencyRepair dr = null;
			String name = "";

			while (rs.next()) {
				if (!rs.getString("name").equals(name)) {
					if (repair && r != null)
						repairs.add(r);
					else if (!repair && dr != null)
						repairs.add(dr);

					if (rs.getString("dependencytype").equals("")) {
						repair = true;
						r = new Repair(rs.getString("name"));
						r.setId(rs.getInt("rapairid"));
						name = r.getName();
						if (rs.getInt("smell_id") != 0) {
							r.addSmellCoverage(getSmellById(rs.getInt("smell_id"), smells), (rs.getInt("weight")));
						}
					} else if (!rs.getString("dependencytype").equals("")) {
						repair = false;
						dr = new DependencyRepair(rs.getString("name"));
						dr.setId(rs.getInt("rapairid"));
						name = dr.getName();
						DependencyType type;
						if (rs.getString("dependencytype").equals("solve"))
							type = DependencyType.SOLVE;
						else
							type = DependencyType.CAUSE;
						String actionField = rs.getString("actionfield");
						String locationPartType = rs.getString("locationparttype");
						dr.addDependency(type, getSmellById(rs.getInt("smell_id"), smells), rs.getDouble("probability"),
								resolveActionField(actionField), resolveLocationPartType(locationPartType));
					}
				} else {
					if (repair) {
						r.addSmellCoverage(getSmellById(rs.getInt("smell_id"), smells), (rs.getInt("weight")));
					} else if (!repair && !rs.getString("dependencytype").equals("")) {
						DependencyType type = null;
						if (rs.getString("dependencytype").equals("solve")) {
							type = DependencyType.SOLVE;
						} else
							type = DependencyType.CAUSE;
						String actionField = rs.getString("actionfield");
						String locationPartType = rs.getString("locationparttype");
						dr.addDependency(type, getSmellById(rs.getInt("smell_id"), smells), rs.getDouble("probability"),
								resolveActionField(actionField), resolveLocationPartType(locationPartType));
					} else if (!repair && rs.getString("dependencytype").equals("")) {
						dr.addSmellCoverage(getSmellById(rs.getInt("smell_id"), smells), (rs.getInt("weight")));
					}
				}
			}

			if (repair && r != null)
				repairs.add(r);
			else if (!repair && dr != null)
				repairs.add(dr);
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}

		return repairs;
	}

	public List<Pattern> getPatterns(List<SmellType> smells) {
		List<Pattern> patterns = new ArrayList<Pattern>();
		String querry = "select * from ("
				+ "select pattern.id,locationparttype,description, 'solve' as type, ismain,smelltype_id "
				+ "from pattern join patternsmelltypesolve on pattern.id=patternsmelltypesolve.pattern_id "
				+ "union all "
				+ "select pattern.id,locationparttype,description, 'cause' as type, false as ismain,smelltype_id "
				+ "from pattern join patternsmelltypecause on pattern.id=patternsmelltypecause.pattern_id) "
				+ " as result order by id,type";
		try {
			ResultSet rs;
			rs = statement.executeQuery(querry);
			Pattern actualRecord = null;
			PatternSmellUse newPSU = null;
			int curentid = -1;

			boolean finishedsolve = true;
			boolean finishedcause = true;

			while (rs.next()) {
				if (curentid != rs.getInt("id")) {
					if (!finishedsolve)
						actualRecord.getFixedSmells().add(newPSU);
					if (!finishedcause)
						actualRecord.getResidualSmells().add(getSmellById(rs.getInt("smelltype_id"), smells));
					curentid = rs.getInt("id");
					actualRecord = new Pattern();
					actualRecord.setId(rs.getInt("id"));
					actualRecord.setDescription(rs.getString("description"));
					actualRecord.setActionField(resolveActionField(rs.getString("locationparttype")));

					// TODO delete this after pattern repair functionality check
					actualRecord.setUsedRepair(new PatternRepair(rs.getString("description")));

					patterns.add(actualRecord);
					finishedsolve = true;
					finishedcause = true;
					actualRecord.setFixedSmells(new ArrayList<PatternSmellUse>());
					actualRecord.setResidualSmells(new ArrayList<SmellType>());
				}
				if (rs.getString("type").equals("solve")) {
					newPSU = new PatternSmellUse();
					newPSU.setMain(rs.getBoolean("ismain"));
					newPSU.setSmellType(getSmellById(rs.getInt("smelltype_id"), smells));
					actualRecord.getFixedSmells().add(newPSU);
					finishedsolve = false;
				} else if (rs.getString("type").equals("cause")) {
					actualRecord.getResidualSmells().add(getSmellById(rs.getInt("smelltype_id"), smells));
					finishedsolve = false;
				}
			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}

		return patterns;
	}

	public void addResultRecord(String repo, String name, long timestamp, List<OptimalPathForCluster> results)
			throws SQLException {
		int analysisID = addAnalysisRecord(repo, name, timestamp);
		int clusterNumber = 1;
		for (OptimalPathForCluster cluster : results) {
			addClusterRecord(cluster, analysisID, clusterNumber);
			clusterNumber++;
		}

	}

	private int addAnalysisRecord(String repo, String name, long timestamp) throws SQLException {
		String query = "insert into pathfinderanalysis (git,gituser,time) values ('" + repo + "','" + name + "','"
				+ timestamp + "')";
		statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = statement.getGeneratedKeys();
		int analysisID = -1;
		while (rs.next()) {
			analysisID = rs.getInt(1);
		}
		return analysisID;
	}

	private Map<SmellOccurance, Integer> addClusterRecord(OptimalPathForCluster cluster, int analysisID,
			int clusterNumber) throws SQLException {
		String query = "insert into cluster (pathfinderanalysis_id,cluster_number) values (" + analysisID + ","
				+ clusterNumber + ")";
		statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = statement.getGeneratedKeys();
		int clusterID = -1;
		while (rs.next()) {
			clusterID = rs.getInt(1);
		}
		Map<SmellOccurance, Integer> repairOrderMap = new HashMap<>();

		for (SmellOccurance smellOcc : cluster.getRootState().getSmells()) {
			int id = addSmellOccurenceRecord(smellOcc, clusterID);
			repairOrderMap.put(smellOcc, id);
		}

		int repairOrder = 1;
		for (Relation r : cluster.getOptimalPath()) {
			int smellOccID = repairOrderMap.get(r.getFixedSmellOccurance());
			addRepairSequencePartRecord(clusterID, smellOccID, r.getUsedRepair().getId(), repairOrder);
			repairOrder++;
		}
		return repairOrderMap;
	}

	private int addSmellOccurenceRecord(SmellOccurance smellOcc, int clusterID) throws SQLException {
		String query = "insert into smelloccurrence (cluster_id,smell_id) values ('" + clusterID + "','"
				+ smellOcc.getSmell().getId() + "')";

		statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = statement.getGeneratedKeys();
		int smellOccID = -1;
		while (rs.next()) {
			smellOccID = rs.getInt(1);
		}
		addSmellPositionRecord(smellOcc.getLocations(), smellOccID);
		return smellOccID;
	}

	private void addSmellPositionRecord(List<Location> locations, int smellOccID) throws SQLException {
		for (Location l : locations) {
			int locationPartOrder = 0;
			int LocationID = 0;
			for (LocationPart lp : l.getLocation()) {
				String query = "insert into smellposition (smelloccurrence_id,type,name,positionorder,locationid) values "
						+ "('" + smellOccID + "','" + lp.getLocationPartType().toString() + "','" + lp.getId() + "',"
						+ locationPartOrder + "," + LocationID + ")";
				locationPartOrder++;
				statement.executeUpdate(query);
			}
			LocationID++;
		}
	}

	private void addRepairSequencePartRecord(int clusterID, Integer fixedSmellOccID, int usedRepairID, int repairOrder)
			throws SQLException {
		String query = "insert into repairsequencepart (smelloccurrence_id,repair_id,cluster_id,repairorder) values "
				+ "('" + fixedSmellOccID + "','" + usedRepairID + "','" + clusterID + "'," + repairOrder + ")";
		statement.executeUpdate(query);

	}

	private DependencyPlaceType resolveLocationPartType(String act) {
		if (act == null)
			return null;
		if (act.equals("internal")) {
			return DependencyPlaceType.INTERNAL;
		} else if (act.equals("external")) {
			return DependencyPlaceType.EXTERNAL;
		}
		return null;
	}

	private LocationPartType resolveActionField(String act) {
		if (act == null)
			return null;

		act = act.toLowerCase();

		if (act.equals("method")) {
			return LocationPartType.METHOD;
		} else if (act.equals("package")) {
			return LocationPartType.PACKAGE;
		} else if (act.equals("class")) {
			return LocationPartType.CLASS;
		} else if (act.equals("parameter")) {
			return LocationPartType.PARAMETER;
		} else if (act.equals("position")) {
			return LocationPartType.POSITION;
		} else if (act.equals("node")) {
			return LocationPartType.NODE;
		} else if (act.equals("attribute")) {
			return LocationPartType.ATTRIBUTE;
		}
		return null;
	}

	public JSONArray getPathFinderResultRecords() {
		JSONArray result = new JSONArray();
		String querry = "select * from pathfinderanalysis";
		ResultSet rs;
		try {
			rs = statement.executeQuery(querry);
			while (rs.next()) {
				JSONObject current = new JSONObject();
				current.append("id", rs.getInt("id"));
				current.append("git", rs.getString("git"));
				current.append("gituser", rs.getString("gituser"));
				
				Date date = new Date(rs.getLong("time"));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				current.append("time", sdf.format(date));
				result.put(current);
			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}
		return result;
	}

	public JSONObject getPathFinderResultRecordDetail(int id) {
		JSONObject result = new JSONObject();
		String qurry = "select * from pathfinderanalysis pfa where pfa.id = " + id;
		ResultSet rs;
		try {
			rs = statement.executeQuery(qurry);
			while (rs.next()) {
				result.append("git", rs.getString("git"));
				result.append("gituser", rs.getString("gituser"));

				Date date = new Date(rs.getLong("time"));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				result.append("time", sdf.format(date));

			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}

		return result;
	}

	public JSONObject getPathFinderAnalysisCluster(int analysisId, int ClusterNumber) {
		JSONObject test = new JSONObject();
		JSONArray result = new JSONArray();
		boolean inserted = false;
		String querry = "select c.id as clusterid, st.name,st.description,sc.id from cluster c join smelloccurrence sc on c.id= sc.cluster_id "
				+ "join smelltype st on st.id = sc.smell_id where c.pathfinderanalysis_id = " + analysisId
				+ " and c.cluster_number = " + ClusterNumber + " order by sc.id";
		ResultSet rs;
		try {
			rs = statement.executeQuery(querry);
			JSONObject curr = null;
			while (rs.next()) {
				if (!inserted) {
					test.put("clusterid", rs.getString("clusterid"));
					inserted = true;
				}
				curr = new JSONObject();
				curr.append("id", rs.getInt("id"));
				curr.append("smellname", rs.getString("name"));
				curr.append("description", rs.getString("description"));
				result.put(curr);

			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}
		test.put("smells", result);

		return test;
	}

	private JSONArray getSmellLocationByID(int smelloccID) {
		System.out.println("4");
		JSONArray result = new JSONArray();
		String querry = "select * from smellposition sp where sp.smelloccurrence_id = " + smelloccID
				+ "order by locationid,positionorder";
		ResultSet rs;
		List<LocationPart> locparts = new ArrayList<LocationPart>();
		Location loc = null;
		try {

			rs = statement2.executeQuery(querry);
			int locationid = -1;
			while (rs.next()) {
				if (locationid != rs.getInt("locationid")) {
					if (loc != null) {
						result.put(loc.toJSON());
						locparts = new ArrayList<LocationPart>();
					}
					loc = new Location(locparts);
					locationid = rs.getInt("locationid");
				}
				locparts.add(new LocationPart(LocationPartType.valueOf(rs.getString("type")), rs.getString("name")));
			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}
		System.out.println("5");

		return result;

	}

	public JSONObject getPathFinderResultRepair(int clusterid, int repairid) {
		JSONObject result = new JSONObject();
		String querry = "select st.name as smell ,r.name as repair, repairorder from repairsequencepart rsp "
				+ "join repair r on r.id = rsp.repair_id join smelloccurrence so on so.id=rsp.smelloccurrence_id "
				+ "join smelltype st on st.id = smell_id where rsp.cluster_id =" + clusterid + " and repairorder = "
				+ repairid;
		ResultSet rs;

		System.out.println(querry);
		try {
			rs = statement.executeQuery(querry);
			while (rs.next()) {
				result.put("repair", rs.getString("repair"));
				result.put("smell", rs.getString("smell"));
				result.put("order", rs.getInt("repairorder"));
			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}

		return result;
	}

	public JSONObject getPathFinderAnalysisInfo(int analysisId) {
		JSONObject result = new JSONObject();
		String query = "select count(*) as result from pathfinderanalysis pfa join cluster c on c.pathfinderanalysis_id = pfa.id where pfa.id = "
				+ analysisId;
		ResultSet rs = null;

		try {
			rs = statement.executeQuery(query);
			while (rs.next()) {
				result.put("clustecount", rs.getInt("result"));
			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}

		return result;
	}

	public JSONObject getPathFinderClusterInfo(int clusterId) {

		JSONObject result = new JSONObject();
		String query = "		select count(*) as result from cluster c join repairsequencepart rsp on c.id = rsp.cluster_id where c.id = "
				+ clusterId;
		System.out.println(query);
		ResultSet rs = null;

		try {
			rs = statement.executeQuery(query);
			while (rs.next()) {
				result.put("repaircount", rs.getInt("result"));
			}
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "database connection failed", e);
		}
		System.out.println(result);
		return result;
	}
}
