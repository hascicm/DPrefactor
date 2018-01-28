package sk.fiit.dp.pathFinder.entities.stateSpace;

import java.util.HashMap;
import java.util.Map;

import sk.fiit.dp.pathFinder.entities.Pattern;
import sk.fiit.dp.pathFinder.entities.PatternSmellUse;;

public class PatternRelation extends Relation {
	private Pattern usedPattern;
	private Map<PatternSmellUse, SmellOccurance> patternSmellUses = new HashMap<PatternSmellUse, SmellOccurance>(); 

	public Pattern getUsedPattern() {
		return usedPattern;
	}

	public void setUsedPattern(Pattern usedPattern) {
		this.usedPattern = usedPattern;
	}

	public Map<PatternSmellUse, SmellOccurance> getPatternSmellUses() {
		return patternSmellUses;
	}

	public void setPatternSmellUses(Map<PatternSmellUse, SmellOccurance> patternSmellUses) {
		this.patternSmellUses = patternSmellUses;
	}
	
	public String toString(){
		return super.toString() + " PATTERN";
	}
}
