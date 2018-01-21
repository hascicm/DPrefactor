package sk.fiit.dp.pathFinder.dataprovider;

import java.util.List;

import sk.fiit.dp.pathFinder.entities.Pattern;
import sk.fiit.dp.pathFinder.entities.Repair;
import sk.fiit.dp.pathFinder.entities.SmellType;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.refactor.model.JessInput;

public interface DataProvider {
	public List<Repair> getRepairs();
	public List<SmellType> getSmellTypes();
	public State getRootState();
	public void initializeRootState(List<JessInput> searchResults);
	public List<Pattern> getPatterns();
}
