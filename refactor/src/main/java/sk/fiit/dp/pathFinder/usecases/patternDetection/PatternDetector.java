package sk.fiit.dp.pathFinder.usecases.patternDetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.pathFinder.entities.Pattern;
import sk.fiit.dp.pathFinder.entities.PatternSmellUse;
import sk.fiit.dp.pathFinder.entities.stateSpace.PatternRelation;
import sk.fiit.dp.pathFinder.entities.stateSpace.Relation;
import sk.fiit.dp.pathFinder.entities.stateSpace.SmellOccurance;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.pathFinder.usecases.helpers.PlaceComparator;

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
		
		Map<PatternSmellUse, SmellOccurance> patternSmellUses = new HashMap<PatternSmellUse, SmellOccurance>(); 
		
		for(SmellOccurance smellOccurance : state.getSmells()){
			for(Pattern pattern : patterns){
				for(PatternSmellUse psu : pattern.getFixedSmells()){
					if(psu.isMain() && psu.getSmellType() == smellOccurance.getSmell()){
						
						boolean isCorrect = true;
						for(PatternSmellUse tempPsu : pattern.getFixedSmells()){
							if(!tempPsu.isMain()){
								boolean foundSmell = false;
								for(SmellOccurance tempSmellOccurance : state.getSmells()){
									if(tempPsu.getSmellType() == tempSmellOccurance.getSmell()){
										
										List<LocationPart> commonPath = PlaceComparator.findCommonDestinationPath(smellOccurance.getLocations().get(0).getLocation(),
														tempSmellOccurance.getLocations().get(0).getLocation());
										
										if(containsActionFiled(commonPath, pattern.getActionField())){
											foundSmell = true;
											patternSmellUses.put(tempPsu, tempSmellOccurance);
											break;
										}else{
											foundSmell = false;
										}
										
									}
								}
								
								if(!foundSmell){
									isCorrect = false;
								}	
							}
							if(!isCorrect){
								patternSmellUses.clear();
								break;
							}
						}
						
						//FOUND PATTERN
						if(isCorrect){
							PatternRelation patternRelation = new PatternRelation();
							patternRelation.setFixedSmellOccurance(smellOccurance);
							patternRelation.setFromState(state);
							patternRelation.setUsedPattern(pattern);
							patternRelation.setUsedRepair(pattern.getUsedRepair());
							
							patternSmellUses.put(psu, smellOccurance);
							patternRelation.setPatternSmellUses(patternSmellUses);
							
							List<Relation> rels = new ArrayList<Relation>();
							rels.add(patternRelation);
							state.setRelations(rels);
													
							
							return true;
							
						}else{
							//NEXT pattern
							break;
						}
					}
				}
			}
		}
		
		//PATTERN NOT FOUND
		return false;
	}

	private boolean containsActionFiled(List<LocationPart> commonPath, LocationPartType actionField) {
		
		for(int i = commonPath.size()-1; i >=0; i-- ){

			if(commonPath.get(i).getLocationPartType() == actionField){
				return true;
			}

		}
		return false;
	}
	
}
