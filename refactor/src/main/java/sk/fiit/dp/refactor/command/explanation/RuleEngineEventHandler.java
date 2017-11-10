package sk.fiit.dp.refactor.command.explanation;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jess.Activation;
import jess.JessEvent;
import jess.JessListener;

import sk.fiit.dp.refactor.model.JessInput;
import sk.fiit.dp.refactor.model.explanation.JessListenerOutput;

public class RuleEngineEventHandler implements JessListener {

	final static Logger logger = LoggerFactory.getLogger(RuleEngineEventHandler.class);
	private static RuleEngineEventHandler INSTANCE = null;
	private JessInput current;
	private List<JessListenerOutput> firedRulesList;

	public RuleEngineEventHandler() {
		firedRulesList = new ArrayList<JessListenerOutput>();

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

			JessListenerOutput outputObject = new JessListenerOutput(current.getCode(), current.getRefCode(),
					((Activation) je.getObject()).getRule().getName(),
					((Activation) je.getObject()).getRule().getDocstring());

			firedRulesList.add(outputObject);
			break;
		default:
			// ignore
		}
	}

	public void setCurrentInput(JessInput current) {
		this.current = current;
	}

	public void reset() {
		firedRulesList = new ArrayList<JessListenerOutput>();
		current = null;
	}

	public List<JessListenerOutput> getListenerOutputObjects() {
		return firedRulesList;
	}

}
