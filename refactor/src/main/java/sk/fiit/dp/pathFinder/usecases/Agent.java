package sk.fiit.dp.pathFinder.usecases;

import java.util.List;

import sk.fiit.dp.pathFinder.entities.stateSpace.*;

public class Agent implements Runnable{

	private PathSearchStrategy pathSearchStrategy;
	private List<Relation> rootRelations;
	private List<Relation> bestPath;
	
	public Agent(List<Relation> rootRelations, PathSearchStrategy pathSearchStrategy) {
		super();
		this.pathSearchStrategy = pathSearchStrategy;
		this.rootRelations = rootRelations;
	}
	
	public PathSearchStrategy getPathSearchStrategy() {
		return pathSearchStrategy;
	}

	public void setPathSearchStrategy(PathSearchStrategy pathSearchStrategy) {
		this.pathSearchStrategy = pathSearchStrategy;
	}

	public List<Relation> getRootRelations() {
		return rootRelations;
	}

	public void setRootRelations(List<Relation> rootRelation) {
		this.rootRelations = rootRelation;
	}

	public List<Relation> getBestPath() {
		return bestPath;
	}

	public void setBestPath(List<Relation> bestPath) {
		this.bestPath = bestPath;
	}

	@Override
	public void run() {
		
		List<Relation> tempPath = null;
		for(Relation rel : this.rootRelations){
			
			//TODO prerobit depth parameter
			tempPath = this.pathSearchStrategy.findPath(rel.getToState(), 1);
			
			if(this.bestPath != null){
				
				State bestState = this.bestPath.get(this.bestPath.size() - 1).getToState();
				double bestValue = bestState.getFitness() + bestState.getDepth() + this.bestPath.get(0).getUsedRepair().getWeight(this.getBestPath().get(0).getFixedSmellOccurance().getSmell());
				
				State currentState = tempPath.get(tempPath.size() - 1).getToState();
				double currentValue = currentState.getFitness() + currentState.getDepth() + tempPath.get(tempPath.size() - 1).getUsedRepair().getWeight(tempPath.get(tempPath.size() - 1).getFixedSmellOccurance().getSmell());
				
				if(currentValue < bestValue){
					this.bestPath = tempPath;
				}
				
				
			}else{
				this.bestPath = tempPath;
			}
		}	
		
	}
}
