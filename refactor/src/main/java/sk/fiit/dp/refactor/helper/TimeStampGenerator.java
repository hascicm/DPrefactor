package sk.fiit.dp.refactor.helper;

import java.util.Calendar;

public class TimeStampGenerator {
	private static TimeStampGenerator INSTANCE = null;
	private long time;

	public static TimeStampGenerator getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TimeStampGenerator();
		}

		return INSTANCE;
	}

	private TimeStampGenerator() {
		resetTimeStamp();
	}

	public void resetTimeStamp() {
		time = Calendar.getInstance().getTimeInMillis();
	}

	public long getTime() {
		return time;
	}
}
