package sk.fiit.dp.pathFinder.usecases;



import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import sk.fiit.dp.pathFinder.entities.stateSpace.PatternRelation;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public abstract class PathSearchStrategy {
	
	protected Set<String> visitedStates;
	protected State localMaximum = null;
	protected PriorityQueue<GraphRelation> queue;
	protected RelationCreator relationCreator;
	protected PatternDetector patternDetector;
	protected int lastStateId = 0;
	protected long rootStateSmellsWeight = 0;
	
	private static double PROBABILITY_THRASHOLD = 0.00;
	private ProbabilityCalculationStrategy probabolityCalculationStrategy = new AndOrProbabilityCalculationStrategy(); 
	
	protected boolean isPatternDetection = false;
	
	public PathSearchStrategy(RelationCreator relationCreator){
		this.relationCreator = new RelationCreator(relationCreator);
	}
	
	public PathSearchStrategy(RelationCreator relationCreator, PatternDetector patternDetector){
		this.relationCreator = new RelationCreator(relationCreator);
		this.isPatternDetection = true;
		this.patternDetector = patternDetector;
	}
	
	//GETTERS AND SETTERS
	public ProbabilityCalculationStrategy getProbabolityCalculationStrategy() {
		return probabolityCalculationStrategy;
	}

	public void setProbabolityCalculationStrategy(ProbabilityCalculationStrategy probabolityCalculationStrategy) {
		this.probabolityCalculationStrategy = probabolityCalculationStrategy;
	}
	
	
	//GETTERS AND SETTERS

	public PatternDetector getPatternDetector() {
		return patternDetector;
	}

	public void setPatternDetector(PatternDetector patternDetector) {
		this.isPatternDetection = true;
		this.patternDetector = patternDetector;
	}

	public abstract List<Relation> findPath(State rootState, int depth);
			
	protected void applyRepair(List<Relation> rels){
		
		Relation rel = null;
		int length = rels.size();
		for(int i = 0; i < length; i++){
			
			rel = rels.get(i);
			
			
			State s = StateProcessor.applyRepair(rel);
			
			
			s.setSourceRelation(rel);
			s.setDepth(rel.getFromState().getDepth() + 1);
			rel.setToState(s);
					
			//sort smells in new state by ID and location (only key/source location )
			s.getSmells().sort((o1, o2) -> {
				if (o1.getSmell().getId().compareTo(o2.getSmell().getId()) == 0) {
		            return o1.getLocations().get(0).toString().compareTo(o2.getLocations().get(0).toString());
		        } else {
		            return o1.getSmell().getId().compareTo(o2.getSmell().getId());
		        }
			});
			
		}
	}
	
	protected boolean isVisited(State s){	
		return this.visitedStates.contains(StateProcessor.createHash(s)) ? true : false; 			
	}
	
	
	protected boolean isLowProbability(State s){
		
		boolean result = false;
		
		if(s.getSourceRelation() != null){
			if(s.getSourceRelation().getProbability() < PROBABILITY_THRASHOLD){
				result = true;
			}
		}
		return result; 
	}
	
	protected int calculateHeuristic(Relation r){
		
		int result = 0;
		
		if(r != null){
			result = (int) r.getToState().getFitness();
		}
		
		
		return result; 
	}
	
	protected void calculateEndNodeFitness(List<Relation> relations) {
		
		for(Relation rel: relations){
			StateProcessor.calculateFitness(rel.getToState(), this.rootStateSmellsWeight);
		}	
	}
	
	//nemam
	protected void calculateProbabilityOfRelations(List<Relation> relations){
		for(Relation rel : relations){
			rel.calculateProbability(this.probabolityCalculationStrategy);
		}
	}
	
	
	protected class GraphRelation implements Comparable<GraphRelation>{
		
		private Relation relation;

		public Relation getRelation() {
			return relation;
		}

		public GraphRelation(Relation relation) {
			super();
			this.relation = relation;
		}
		
		@Override
		public int compareTo(GraphRelation o) {
				return Integer.compare(calculateHeuristic(o.getRelation()), calculateHeuristic(this.getRelation()));
		}		
	}
	
	protected void addRelationToQueue(Relation r){
		this.queue.add(new GraphRelation(r));
	}
	
	protected void addRelationsToQueue(List<Relation> relations){		
		for(Relation r : relations)
			this.queue.add(new GraphRelation(r));
	}
	
	protected void init(State rootState, int depth) {
		
		//init root atributes
		this.rootStateSmellsWeight = StateProcessor.calculateSmellsWeight(rootState);
		
		// init rootState
		rootState.setDepth(depth);
		rootState.setId(lastStateId++);
		StateProcessor.calculateFitness(rootState, this.rootStateSmellsWeight);
		
		createRelation(rootState);
		applyRepair(rootState.getRelations());
		calculateEndNodeFitness(rootState.getRelations());
		calculateProbabilityOfRelations(rootState.getRelations());
		
	}
	
	protected void expandCurrentState(State currentState){
		
		createRelation(currentState);
		applyRepair(currentState.getRelations());
		calculateEndNodeFitness(currentState.getRelations());
		calculateProbabilityOfRelations(currentState.getRelations());
	}
	
	protected void createRelation(State currentState){
		
		boolean foundPattern = false;
		if(this.isPatternDetection){
			if(this.patternDetector.checkPattern(currentState)){
				foundPattern = true;
			}
		}
		
		//PATTERN NOT FOUND
		if(!foundPattern){		
			relationCreator.addRelationsToState(currentState);
		}
	}
	
}
