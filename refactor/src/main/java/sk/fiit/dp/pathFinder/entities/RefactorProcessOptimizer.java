package sk.fiit.dp.pathFinder.entities;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sk.fiit.dp.pathFinder.dataprovider.DataProvider;
import sk.fiit.dp.pathFinder.dataprovider.DatabaseDataProvider;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.usecases.AntColonyPathSearchMultithreded;
import sk.fiit.dp.pathFinder.usecases.BeePathSearchStrategy;
import sk.fiit.dp.pathFinder.usecases.DefaultPathSearchStrategy;
import sk.fiit.dp.pathFinder.usecases.MinMaxProbabilityCalculationStrategy;
import sk.fiit.dp.pathFinder.usecases.MonteCarloSearchStrategy;
import sk.fiit.dp.pathFinder.usecases.PathSearchStrategy;
import sk.fiit.dp.pathFinder.usecases.RelationCreator;

public class RefactorProcessOptimizer {
	
	private DataProvider dataProvider;
	private PathSearchStrategy pathSearchStrategy;
	private List<Relation> optimalPath; 	

	public RefactorProcessOptimizer(String method){
		init(method);	
	}

	private void init(String method) {
		//this.dataProvider = new DatabaseDataProvider();
		this.dataProvider = new DatabaseDataProvider(); 
		if (method.equals("A*")){
			Logger.getLogger("pathfinter").log(Level.INFO, "starting pathfinding using A*");
			this.pathSearchStrategy = new DefaultPathSearchStrategy(new RelationCreator(this.dataProvider.getSmellTypes(), this.dataProvider.getRepairs()));
		}
		else if (method.equals("bee")){
			Logger.getLogger("pathfinter").log(Level.INFO, "starting pathfinding using bee hive colony");
			this.pathSearchStrategy = new BeePathSearchStrategy(new RelationCreator(this.dataProvider.getSmellTypes(), this.dataProvider.getRepairs()));
		}
		else if (method.equals("ant")){
			Logger.getLogger("pathfinter").log(Level.INFO, "starting pathfinding using atificial ant colony");
			this.pathSearchStrategy = new AntColonyPathSearchMultithreded(new RelationCreator(this.dataProvider.getSmellTypes(), this.dataProvider.getRepairs()));
		}
		else if (method.equals("mc")){
			Logger.getLogger("pathfinter").log(Level.INFO, "starting pathfinding using monte carlo");
			this.pathSearchStrategy = new MonteCarloSearchStrategy(new RelationCreator(this.dataProvider.getSmellTypes(), this.dataProvider.getRepairs()));
		}
		//
	
		//Probability Calculation Strategy
		this.pathSearchStrategy.setProbabolityCalculationStrategy(new MinMaxProbabilityCalculationStrategy());
	}

	public void findRefactoringPath(){
		//MultiAgent ma = new MultiAgent();
		//ma.findPath(this.dataProvider.getRootState(), this.pathSearchStrategy);
		Long startTime = System.currentTimeMillis();
		optimalPath = this.pathSearchStrategy.findPath(this.dataProvider.getRootState(), 0);
		System.out.println("");
		System.out.println("Time: ");
		System.out.println(((startTime - System.currentTimeMillis())/1000.0));
	}
	
	public void setPathSearchStrategy(PathSearchStrategy pathSearchStrategy) {
		this.pathSearchStrategy = pathSearchStrategy;
	}
	
	public List<Relation> getOptimalPath() {
		return optimalPath;
	}
	public DataProvider getDataProvider(){
		return this.dataProvider;
	}

	
}
