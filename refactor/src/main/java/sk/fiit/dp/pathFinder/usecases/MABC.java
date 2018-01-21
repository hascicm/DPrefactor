package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.entities.stateSpace.State;

public class MABC extends BeePathSearchStrategy implements Runnable {

	private List<BeeSpace> bees;
	
	
	public MABC(RelationCreator relationCreator, long rootStateFitness) {
		super(relationCreator);
		this.bees = new ArrayList<BeeSpace>(); 
		this.rootStateSmellsWeight = rootStateFitness;
	}

	@Override
	public void run() {
		
		for(BeeSpace bs : this.bees){
			exploreSpace(bs.b, bs.s, bs.depth);
		}
	}
	
	public void addBee(Bee b, State s, Integer depth){
		this.bees.add(new BeeSpace(b, s, depth));
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
