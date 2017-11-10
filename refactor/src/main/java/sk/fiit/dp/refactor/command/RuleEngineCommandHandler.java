package sk.fiit.dp.refactor.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jess.Filter;
import jess.JessEvent;
import jess.JessException;
import jess.Rete;
import sk.fiit.dp.refactor.command.explanation.RuleEngineEventHandler;
import sk.fiit.dp.refactor.helper.Resources;
import sk.fiit.dp.refactor.helper.Str;
import sk.fiit.dp.refactor.model.JessInput;
import sk.fiit.dp.refactor.model.JessOutput;

public class RuleEngineCommandHandler {

	private static RuleEngineCommandHandler INSTANCE;

	private Rete rete;

	private RuleEngineCommandHandler() {
		rete = new Rete();
		reloadRules();
	}

	public static RuleEngineCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RuleEngineCommandHandler();
		}

		return INSTANCE;
	}

	/**
	 * Nacitanie pravidiel zo suboru
	 */
	public void reloadRules() {
		try {
			rete.reset();
			rete.batch(Resources.getPath(Str.RULES.val()).toAbsolutePath().toString());
			rete.setEventMask(rete.getEventMask() | JessEvent.DEFRULE_FIRED | JessEvent.FACT | JessEvent.RESET );
		} catch (JessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Spustenie pravidloveho stroja
	 * 
	 * @param searchResults
	 * @return
	 */
	public List<JessOutput> run(List<JessInput> searchResults) {
		List<JessOutput> refactoringRules = new ArrayList<>();

		for (JessInput input : searchResults) {
			try {
				rete = new Rete();
				RuleEngineEventHandler.getInstance().setCurrentInput(input);
				rete.addJessListener( RuleEngineEventHandler.getInstance());
				reloadRules();
				rete.add(input);

				rete.run();
			} catch (JessException e) {
				e.printStackTrace();
			}

			Iterator<?> result = rete.getObjects(new Filter.ByClass(JessOutput.class));
			while (result.hasNext()) {
				refactoringRules.add((JessOutput) result.next());
			}
		}

		return refactoringRules;
	}
}