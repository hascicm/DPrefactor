package sk.fiit.dp.pathFinder.usecases;

import java.util.List;

import sk.fiit.dp.pathFinder.entities.Pattern;
import sk.fiit.dp.pathFinder.entities.stateSpace.PatternRelation;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class PatternDetector {
	private List<Pattern> patterns;

	public PatternDetector(List<Pattern> patterns) {
		super();
		this.patterns = patterns;
	}

	public List<Pattern> getPatterns() {
		return patterns;
	}

	public void setPatterns(List<Pattern> patterns) {
		this.patterns = patterns;
	}
	
	public boolean checkPattern(State state){
		
		//add relation to state
		
		return false;
	}
}
