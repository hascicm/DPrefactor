package sk.fiit.dp.refactor.helper;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resources {
	public static Path getPath(String name) {
		URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(name);
		try {
			return Paths.get(resourceUrl.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
