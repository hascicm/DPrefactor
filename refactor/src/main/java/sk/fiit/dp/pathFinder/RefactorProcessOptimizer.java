package sk.fiit.dp.pathFinder;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.internal.eval.EvaluationConstants;

import sk.fiit.dp.pathFinder.dataprovider.DataProvider;
import sk.fiit.dp.pathFinder.dataprovider.DatabaseDataProvider;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.pathFinder.usecases.patternDetection.PatternDetector;
import sk.fiit.dp.pathFinder.usecases.relationProcessing.MinMaxProbabilityCalculationStrategy;
import sk.fiit.dp.pathFinder.usecases.relationProcessing.RelationCreator;
import sk.fiit.dp.pathFinder.usecases.stateSpaceBrowsing.AntColonyPathSearchMultithreded;
import sk.fiit.dp.pathFinder.usecases.stateSpaceBrowsing.BeePathSearchStrategy;
import sk.fiit.dp.pathFinder.usecases.stateSpaceBrowsing.DefaultPathSearchStrategy;
import sk.fiit.dp.pathFinder.usecases.stateSpaceBrowsing.MonteCarloSearchStrategy;
import sk.fiit.dp.pathFinder.usecases.stateSpaceBrowsing.PathSearchStrategy;
import sk.fiit.dp.refactor.controller.Evaluation;

public class RefactorProcessOptimizer {

	private DataProvider dataProvider;
	private PathSearchStrategy pathSearchStrategy;
	private List<Relation> optimalPath;

	public RefactorProcessOptimizer(String method) {
		init(method);
	}

	private void init(String method) {
		// this.dataProvider = new DatabaseDataProvider();
		this.dataProvider = DatabaseDataProvider.getInstance();

		if (method.equals("A*")) {
			Logger.getLogger("pathfinter").log(Level.INFO, "starting pathfinding using A*");
			this.pathSearchStrategy = new DefaultPathSearchStrategy(
					new RelationCreator(this.dataProvider.getSmellTypes(), this.dataProvider.getRepairs()));
		} else if (method.equals("bee")) {
			Logger.getLogger("pathfinter").log(Level.INFO, "starting pathfinding using bee hive colony");
			this.pathSearchStrategy = new BeePathSearchStrategy(
					new RelationCreator(this.dataProvider.getSmellTypes(), this.dataProvider.getRepairs()));
		} else if (method.equals("ant")) {
			Logger.getLogger("pathfinter").log(Level.INFO, "starting pathfinding using atificial ant colony");
			this.pathSearchStrategy = new AntColonyPathSearchMultithreded(
					new RelationCreator(this.dataProvider.getSmellTypes(), this.dataProvider.getRepairs()));
		} else if (method.equals("mc")) {
			Logger.getLogger("pathfinter").log(Level.INFO, "starting pathfinding using monte carlo");
			this.pathSearchStrategy = new MonteCarloSearchStrategy(
					new RelationCreator(this.dataProvider.getSmellTypes(), this.dataProvider.getRepairs()));
		}
		//

		// Probability Calculation Strategy
		this.pathSearchStrategy.setProbabolityCalculationStrategy(new MinMaxProbabilityCalculationStrategy());

		// if is apply pattern
		this.pathSearchStrategy.setPatternDetector(new PatternDetector(this.getDataProvider().getPatterns()));
	}

	public void findRefactoringPath(State rootState) {
		//For multiagent A*
		// MultiAgent ma = new MultiAgent();
		
		Long startTime = System.currentTimeMillis();
		optimalPath = this.pathSearchStrategy.findPath(rootState, 0);
		System.out.println("");
		System.out.println("Time: ");
		System.out.println(((startTime - System.currentTimeMillis()) / 1000.0));
		
	}

	public void setPathSearchStrategy(PathSearchStrategy pathSearchStrategy) {
		this.pathSearchStrategy = pathSearchStrategy;
	}

	public List<Relation> getOptimalPath() {
		return optimalPath;
	}

	public DataProvider getDataProvider() {
		return this.dataProvider;
	}
}
