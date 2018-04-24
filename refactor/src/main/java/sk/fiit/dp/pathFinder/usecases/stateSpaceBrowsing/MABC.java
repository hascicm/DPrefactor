package sk.fiit.dp.pathFinder.usecases.stateSpaceBrowsing;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.pathFinder.usecases.patternDetection.PatternDetector;
import sk.fiit.dp.pathFinder.usecases.relationProcessing.RelationCreator;
import sk.fiit.dp.pathFinder.usecases.stateSpaceBrowsing.BeePathSearchStrategy.Bee;

public class MABC extends BeePathSearchStrategy implements Runnable {

	private List<BeeSpace> colony;
	
	
	public MABC(RelationCreator relationCreator, long rootStateFitness, PatternDetector patternDetector) {
		super(relationCreator);
		this.colony = new ArrayList<BeeSpace>(); 
		this.rootStateSmellsWeight = rootStateFitness;
		
		if(patternDetector != null){
			this.setPatternDetector(patternDetector);
		}
	}

	@Override
	public void run() {
		
		for(BeeSpace bs : this.colony){
			exploreSpace(bs.b, bs.s, bs.depth);
		}
	}
	
	public void addBee(Bee b, State s, Integer depth){
		this.colony.add(new BeeSpace(b, s, depth));
	}
	
	private class BeeSpace{
		Bee b; 
		State s; 
		Integer depth;
		
		BeeSpace(Bee b, State s, Integer depth){
			this.b = b;
			this.s = s;
			this.depth = depth;
		}
	}

}
