package sk.fiit.dp.pathFinder.dataprovider;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.configuration.LocationProcessor;
import sk.fiit.dp.pathFinder.dataprovider.dbsManager.PostgresManager;
import sk.fiit.dp.pathFinder.entities.Dependency;
import sk.fiit.dp.pathFinder.entities.DependencyRepair;
import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.pathFinder.entities.Pattern;
import sk.fiit.dp.pathFinder.entities.PatternRepair;
import sk.fiit.dp.pathFinder.entities.PatternSmellUse;
import sk.fiit.dp.pathFinder.entities.Repair;
import sk.fiit.dp.pathFinder.entities.Repair.RepairUse;
import sk.fiit.dp.pathFinder.entities.SmellType;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.refactor.model.JessInput;

public class DatabaseDataProvider implements DataProvider {
	private static DatabaseDataProvider INSTANCE = null;
	private List<Repair> repairs = null;
	private List<SmellType> smells = null;
	private List<Pattern> patterns = null;
	private State root;

	public static DatabaseDataProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DatabaseDataProvider();
		}
		return INSTANCE;
	}

	private DatabaseDataProvider() {
		smells = PostgresManager.getInstance().getSmellTypes();
		repairs = PostgresManager.getInstance().getRepairs(smells);
		patterns = PostgresManager.getInstance().getPatterns(smells);
		int[] selectedSmells = { 15, 32, 1, 9, 31, 8, 3, 22, 30, 2, 10, 4, 25, 21 };
		int[] selectedRepairs = { 87, 92, 88, 93, 61, 94, 81, 74, 73, 50, 79, 84, 80, 82, 15, 14, 12, 21, 65, 83, 85 };
		reduceDBdata(selectedSmells, selectedRepairs);
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
	public List<Pattern> getPatterns() {
		return patterns;
	}

	@Override
	public State getRootState() {
		return root;
	}

	public List<State> prepareRootStateList(List<JessInput> searchResults) {
		List<State> result = new ArrayList<State>();
		this.initializeRootState(searchResults);
		result.add(this.getRootState());
		return result;
	}

	@Override
	public void initializeRootState(List<JessInput> searchResults) {
		this.root = new State();
		List<SmellOccurance> smellOccurances = new ArrayList<SmellOccurance>();
		for (JessInput searchResult : searchResults) {
			SmellType smell = this.getSmellType(searchResult.getRefCode());

			List<Location> locationList = LocationProcessor.processLocationString(searchResult.getXpatPosition());

			SmellOccurance ocurance = new SmellOccurance(smell, locationList, searchResult.getCode());
			smellOccurances.add(ocurance);
		}
		this.root.setSmells(smellOccurances);
	}

	//It was used for testing purpose...
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
					for (RepairUse ru : r.getRepairUses()) {
						if (reducedSmells.contains(ru.getSmell())) {
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

	@Deprecated
	public List<Pattern> getPatternsOLD() {

		List<Pattern> result = new ArrayList<Pattern>();

		// --------------------------------
		String patternDesc = "(-)Catch and Rethrow (1)Remove Exception Throw (+)Empty Catch Clausule => "
				+ " (-)Empty Catch Clausule (2) Log Exception (+) null";
		Pattern p1 = new Pattern(patternDesc);
		p1.setActionField(LocationPartType.NODE);
		p1.setUsedRepair(new PatternRepair(patternDesc, 95, 0));
		PatternSmellUse psu1 = new PatternSmellUse();
		psu1.setMain(true);
		psu1.setSmellType(this.getSmellTypes().get(29));

		p1.setFixedSmells(new ArrayList<PatternSmellUse>());
		p1.getFixedSmells().add(psu1);

		p1.setResidualSmells(new ArrayList<SmellType>());

		result.add(p1);

		return result;
	}

}
