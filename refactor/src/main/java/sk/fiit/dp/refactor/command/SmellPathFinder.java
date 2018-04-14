package sk.fiit.dp.refactor.command;

import java.sql.SQLException;
import java.util.List;

import javax.xml.xquery.XQException;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Occurs;

import sk.fiit.dp.refactor.dbs.BaseXManager;
import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.model.JessInput;

public class SmellPathFinder {

	private static SmellPathFinder INSTANCE;
	private BaseXManager basex;
	private PostgreManager pgmanager;

	private SmellPathFinder() {
		basex = BaseXManager.getInstance();
		pgmanager = PostgreManager.getInstance();
	}

	public static SmellPathFinder getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SmellPathFinder();
		}
		return INSTANCE;
	}

	public void findPathsToSmells(List<JessInput> smells) {
		for (JessInput ocurence : smells) {

			try {
				String localisationsScript = pgmanager.getSmellLocalisatorScript(ocurence.getRefCode());
				List<String> path = basex.applyPositionXQuery(localisationsScript, "tag", ocurence.getCode());

				ocurence.setXpathPosition(ProcesOutput(path));

			} catch (SQLException | XQException e) {
				e.printStackTrace();
			}
		}
	}

	private String ProcesOutput(List<String> xpathOut) {
		String processedPath = "";
		String path = "";
		for (int i = 0; i < xpathOut.size(); i++) {
			if (xpathOut.get(i).startsWith("CC:")) {
				if (xpathOut.get(i).equals("CC:"))
					path += "CC:default::";
				else
					path += xpathOut.get(i) + "::";
			} else if (xpathOut.get(i).startsWith("C:")) {
				path += xpathOut.get(i) + "::";
			} else if (xpathOut.get(i).startsWith("M:")) {
				path += xpathOut.get(i) + "::";
			} else {
				processedPath = xpathOut.get(i);

			}
		}
		// System.out.println("smellpathfinder:" + processedPath);
		if (processedPath.contains("function")) {
			processedPath = processedPath.substring(processedPath.lastIndexOf("function"));
			// System.out.println("smellpathfinder:" + processedPath);
		}
		processedPath = processedPath.replaceAll("[a-z]+/|[a-z]+_[a-z]+/", "");
		// System.out.println("smellpathfinder:" + processedPath);
		if (processedPath.length() > 0) {
			processedPath = "NODE:" + processedPath;
		}
		processedPath = processedPath.replaceAll("/", "::NODE:");
		if (processedPath.length() > 0) {
			processedPath += "::";
		}
		// System.out.println("smellpathfinder:" + processedPath);
		// System.out.println("processed path: " + path + processedPath);
		return path + processedPath;
	}

}
