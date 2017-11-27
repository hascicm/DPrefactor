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

			String query = "import module namespace functx = \"http://www.functx.com\"; for $node in xquery:eval(\'//"
					+ ocurence.getCode() + "\') return   (db:output( functx:path-to-node($node)), "
					+ "db:output( concat( \"CC:\" , $node/ancestor-or-self::unit[1]/package/name)),  "
					+ "if ($node/descendant-or-self::class[1]) then "
					+ "(let $class := $node/descendant-or-self::class[1] " + "for $x in $class/ancestor-or-self::class "
					+ "return ( db:output(concat(\"C:\" ,$x/name)))) "
					+ "else (for $x in $node/ancestor-or-self::class "
					+ "return ( db:output(concat(\"C:\" ,$x/name))))," + "for $x in $node/ancestor-or-self::function "
					+ "return (db:output(concat(\"M:\" ,$x/name))))";
			// System.out.println(query);

			try {
				String localisationsScript = pgmanager.getSmellLocalisatorScript(ocurence.getRefCode());
				System.out.println("---------------------code " + ocurence.getCode());
				List<String> path = basex.applyPositionXQuery(localisationsScript, "tag", ocurence.getCode());
				for (String p : path) {
					System.out.println("path " + (p));
				}
				// System.out.println("path "+ProcesOutput(path));
				// TODO - parse to pathFinder Input
				ocurence.setXpatPosition(ProcesOutput(path));
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
		processedPath = processedPath.replaceAll("[a-z]+/|[a-z]+_[a-z]+/", "");
		processedPath = processedPath.replaceAll("/", "::");
		System.out.println("processed path: " + path + processedPath);
		return path + processedPath;
	}

}
