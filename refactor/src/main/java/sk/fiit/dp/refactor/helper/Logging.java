package sk.fiit.dp.refactor.helper;

import java.util.logging.Logger;

public class Logging {
	private static Logger JessLoger = null;

	public static Logger getJessLogger() {
		if (JessLoger == null) {
			JessLoger = Logger.getLogger("JESS LOG");
		}

		return JessLoger;
	}
}
