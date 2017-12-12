package sk.fiit.dp.pathFinder.dataprovider;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.dataprovider.dbsManager.PostgresManager;
import sk.fiit.dp.pathFinder.entities.Dependency;
import sk.fiit.dp.pathFinder.entities.DependencyRepair;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.pathFinder.entities.Repair;
import sk.fiit.dp.pathFinder.entities.Repair.RepairUse;
import sk.fiit.dp.pathFinder.entities.SmellType;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.refactor.model.JessInput;

public class DatabaseDataProvider implements DataProvider {

	private List<Repair> repairs = null;
	private List<SmellType> smells = null;
	private State root;

	public DatabaseDataProvider() {
		smells = PostgresManager.getInstance().getSmellTypes();
		repairs = PostgresManager.getInstance().getRepairs(smells);
		// initRoot();
	}

	@Override
	public List<Repair> getRepairs() {
		return repairs;
	}

	@Override
	public List<SmellType> getSmellTypes() {
		return smells;
	}

	@Override
	public State getRootState() {
		return root;
	}

	@Override
	public void initializeRootState(List<JessInput> searchResults) {
		this.root = new State();
		List<SmellOccurance> smellOccurances = new ArrayList<SmellOccurance>();
		for (JessInput searchResult : searchResults) {
			SmellType smell = this.getSmellType(searchResult.getRefCode());

			List<Location> locationList = new ArrayList<Location>();
			List<LocationPart> locationParts = new ArrayList<LocationPart>();

			String[] strLocations = searchResult.getXpatPosition().split("::");
			for (String str : strLocations) {
				locationParts.addAll(processStringToLocationPart(str));
			}
			locationList.add(new Location(locationParts));
			SmellOccurance ocurance = new SmellOccurance(smell, locationList);
			smellOccurances.add(ocurance);
		}
		this.root.setSmells(smellOccurances);
	}

	List<LocationPart> processStringToLocationPart(String s) {
		String[] strParts = s.split(":");
		String type = strParts[0];
		String id = strParts[1];
		LocationPartType locationPartType = null;
		if (type.equals("CC")) {
			return ProcessPackageString(id);
		} else if (type.equals("C")) {
			locationPartType = LocationPartType.CLASS;
		} else if (type.equals("M")) {
			locationPartType = LocationPartType.METHOD;
		} else if (type.equals("NODE")) {
			locationPartType = LocationPartType.NODE;
		} else if (type.equals("A")) {
			locationPartType = LocationPartType.ATTRIBUTE;
		} else if (type.equals("P")) {
			locationPartType = LocationPartType.PARAMETER;
		} else if (type.equals("POS")) {
			locationPartType = LocationPartType.POSITION;
		}

		List<LocationPart> list = new ArrayList<LocationPart>();
		list.add(new LocationPart(locationPartType, id));
		return list;
	}

	List<LocationPart> ProcessPackageString(String packageStr) {
		List<LocationPart> list = new ArrayList<LocationPart>();
		String[] packages = packageStr.split("\\.");
		for (String s : packages) {
			list.add(new LocationPart(LocationPartType.PACKAGE, s));
		}
		return list;
	}

	private void initRoot() {

		List<SmellOccurance> smellOccurances = new ArrayList<SmellOccurance>();

		// DataClumps
		smellOccurances.add(new SmellOccurance(this.getSmellType(4)));
		// LazyClass
		smellOccurances.add(new SmellOccurance(this.getSmellType(9)));
		// FeatureEnvy
		smellOccurances.add(new SmellOccurance(this.getSmellType(10)));
		// LongParameterList
		/*
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(3)));
		 * //DataClass smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(8))); //Large class
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(1)));
		 * //Large class smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(1))); //Divergent change
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(13)));
		 * //Feature envy smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(10))); //Switch statement
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(15)));
		 * //Large Class smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(1))); //DataClumps
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(4)));
		 * //Large Class smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(1))); //Shotgun Surgery
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(14)));
		 * //Switch statement smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(15)));
		 * 
		 * //-----------15---------------------- //middle man
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(21)));
		 * //message chain smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(22))); //switch statement
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(15)));
		 * //Feature envy smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(10))); //divergent changes
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(13)));
		 * 
		 * //-----------------20-----------------------
		 * 
		 * //long method smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(2))); //data clumps
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(4)));
		 * //shotgun surgery smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(14))); //large class
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(1))); //lazy
		 * class smellOccurances.add(new SmellOccurance(this.getSmellType(9)));
		 * 
		 * //------------------25--------------------------------------- //long
		 * parameter list smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(3))); //large class
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(1)));
		 * //divergent change smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(13))); //data class
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(8)));
		 * //middle man smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(21)));
		 * 
		 * 
		 * //--------------------30------------------------------------ //data
		 * class smellOccurances.add(new SmellOccurance(this.getSmellType(8)));
		 * //lazy class smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(9))); //large class
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(1))); //long
		 * method smellOccurances.add(new SmellOccurance(this.getSmellType(2)));
		 * //long parameter list smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(3)));
		 * 
		 * 
		 * //---------------------35--------------------------------------
		 * 
		 * //feature envy smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(10))); //divergent change
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(13)));
		 * //shotgun surgery smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(14))); //switch statement
		 * smellOccurances.add(new SmellOccurance(this.getSmellType(15)));
		 * //middle man smellOccurances.add(new
		 * SmellOccurance(this.getSmellType(21)));
		 * 
		 * //---------------------40---------------------------------------
		 */

		this.root = new State();
		this.root.setSmells(smellOccurances);
	}

	private SmellType getSmellType(int id) {

		SmellType result = null;

		for (SmellType st : this.smells) {
			if (st.getId() == id) {
				result = st;
			}
		}

		return result;
	}

	private SmellType getSmellType(String code) {
		for (SmellType st : this.smells) {
			if (st.getCode() != null) {
				if (st.getCode().equals(code)) {
					return st;
				}
			}
		}
		return null;
	}

	public void printSmells() {
		for (SmellType s : smells) {
			System.out.println(s.getName());
			System.out.println(s.getCode());
			System.out.println(s.getWeight());
			System.out.println("-------------------------------");
		}
	}

	public void printRepairs() {
		for (Repair r : repairs) {
			System.out.println("repair  :" + r.getName());
			System.out.println("id         " + r.getId());

			for (SmellType s : r.getSmells()) {
				System.out.println("used for " + s.getName());
			}
			if (r instanceof DependencyRepair) {
				DependencyRepair x = (DependencyRepair) r;
				List<Dependency> deps = x.getDependencies();
				for (Dependency dep : deps) {
					System.out.println("smell      " + dep.getSmell().getName());
					System.out.println("actiontype " + dep.getActionField());
					System.out.println("placetype  " + dep.getPlaceType());
					System.out.println("type       " + dep.getType());
					System.out.println("prob       " + dep.getProbability());
				}
			}
			System.out.println("-------------------------------");
		}

	}

	public void reduceDBdata(int[] selectedSmells, int[] selectedRepairs) {
		List<SmellType> reducedSmells = new ArrayList<>();
		List<Repair> reducedRepairs = new ArrayList<>();

		for (int smellId : selectedSmells) {
			for (SmellType s : this.smells) {
				if (s.getId() == smellId)
					reducedSmells.add(s);
			}
		}

		for (int repairId : selectedRepairs) {
			for (Repair r : this.repairs) {
				if (r.getId() == repairId) {
					reducedRepairs.add(r);
					List<RepairUse> repUseList = new ArrayList<>();
					for (RepairUse ru : r.getRepairUses()){
						if (reducedSmells.contains(ru.getSmell())){
							repUseList.add(ru);
						}
					}
					r.setRepairUses(repUseList);
				}
			}
		}
		this.smells = reducedSmells;
		this.repairs = reducedRepairs;
	}

}