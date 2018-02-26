package sk.fiit.dp.pathFinder.dataprovider.dbsManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;

import net.xqj.basex.bin.ac;
import sk.fiit.dp.pathFinder.entities.DependencyPlaceType;
import sk.fiit.dp.pathFinder.entities.DependencyRepair;
import sk.fiit.dp.pathFinder.entities.DependencyType;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.pathFinder.entities.Pattern;
import sk.fiit.dp.pathFinder.entities.PatternRepair;
import sk.fiit.dp.pathFinder.entities.PatternSmellUse;
import sk.fiit.dp.pathFinder.entities.Repair;
import sk.fiit.dp.pathFinder.entities.SmellType;

public class PostgresManager {

	private Statement statement;
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
						rs.getString("code"));
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
					
					//TODO 
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

//	public static void main(String[] args) {
//		List<SmellType> s = getInstance().getSmellTypes();
//		List<Pattern> p = getInstance().getPatterns(s);
//		for (Pattern x : p) {
//			System.out.println("----------");
//			System.out.println("id     " + x.getId());
//			System.out.println("desc   " + x.getDescription());
//			System.out.println("act f  " + x.getActionField());
//			System.out.println("----fixed----");
//			for (PatternSmellUse y : x.getFixedSmells()) {
//				System.out.println(y.getSmellType().getName());
//			}
//			System.out.println("----caused----");
//			for (SmellType z : x.getResidualSmells()) {
//				System.out.println(z.getName());
//			}
//		}
//	}

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
}
