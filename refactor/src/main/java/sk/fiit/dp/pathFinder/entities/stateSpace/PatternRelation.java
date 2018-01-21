package sk.fiit.dp.pathFinder.entities.stateSpace;

import sk.fiit.dp.pathFinder.entities.Pattern;

public class PatternRelation extends Relation {
	private Pattern usedPattern;

	public Pattern getUsedPattern() {
		return usedPattern;
	}

	public void setUsedPattern(Pattern usedPattern) {
		this.usedPattern = usedPattern;
	}
}
