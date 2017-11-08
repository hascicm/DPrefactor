package sk.fiit.dp.refactor.command.explanation;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jess.Activation;
import jess.JessEvent;
import jess.JessListener;

import sk.fiit.dp.refactor.model.JessInput;

public class RuleEngineEventHandler implements JessListener {

	final static Logger logger = LoggerFactory.getLogger(RuleEngineEventHandler.class);
	private static RuleEngineEventHandler INSTANCE = null;
	private List<String> firedRules;
	private JessInput current;

	RuleEngineEventHandler() {
		firedRules = new ArrayList<String>();
	}

	public static RuleEngineEventHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RuleEngineEventHandler();
		}
		return INSTANCE;
	}
	@Override
	public void eventHappened(JessEvent je) {
		int type = je.getType();
		switch (type) {
		case JessEvent.RESET:
			logger.info("rule engine reset");
			break;
		case JessEvent.DEFRULE_FIRED:

			logger.info(((Activation) je.getObject()).getRule().getName());
			firedRules.add("for:   " + current.getCode() + "    " + current.getRefCode() + " rule was used: "
					+ ((Activation) je.getObject()).getRule().getName() + "   "
					+ ((Activation) je.getObject()).getRule().getDocstring());
			break;
		default:
			// ignore
		}
	}

	public void setCurrentInput(JessInput current) {
		this.current = current;
	}

	public void reset() {
		firedRules = new ArrayList<String>();
		current = null;
	}

	public List<String> getExpanation() {
		return firedRules;
	}



}
