package sk.fiit.dp.refactor.command.sonarQube;

import org.sonarsource.scanner.api.LogOutput;

public class Logger implements LogOutput {
	
	@Override
	public void log(String formattedMessage, Level level) {
		System.out.println(formattedMessage);
	}
}