package sk.fiit.dp.refactor.command.explanation;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xquery.XQException;

import sk.fiit.dp.pathFinder.dataprovider.dbsManager.PostgresManager;
import sk.fiit.dp.pathFinder.entities.SmellType;
import sk.fiit.dp.refactor.dbs.BaseXManager;
import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.model.JessInput;

public class ExplanationCommentHandler {
	public static ExplanationCommentHandler INSTANCE = null;
	private PostgreManager pg;
	private BaseXManager baseX;

	public static ExplanationCommentHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ExplanationCommentHandler();
		}
		return INSTANCE;
	}

	private ExplanationCommentHandler() {
		pg = PostgreManager.getInstance();
		baseX = BaseXManager.getInstance();
	}

	public void insertExplanationComments(List<JessInput> searchResults) {
		for (JessInput curr : searchResults) {
			String smellCode = curr.getCode();
			String comment = prepareExplanationString(curr.getRefCode());

			insertCommentBeforeSmell(smellCode, comment);
		}
	}

	private String prepareExplanationString(String code) {
		String explanationString = "";
		try {
			explanationString = pg.getExplanationForSearchScript(code);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return explanationString;

	}

	public void insertCommentAfterSmell(String smellCode, String comment) {
		String queryPart = " <comment type = \"line\">" + comment + " </comment>";

		String query = " for $node in xquery:eval(fn:concat(\"//\", '" + smellCode
				+ "')) return ( insert node element {xs:QName('" + smellCode + "AfterComment')} {" + queryPart
				+ "} after $node)";
		try {
			baseX.applyXQuery(query);
		} catch (XQException e) {
			Logger.getLogger("BaseX").log(Level.SEVERE, "Connection to basex failed", e);
		}
	}

	public void insertCommentBeforeSmell(String smellCode, String comment) {
		String queryPart = " <comment type = \"line\">" + comment + " </comment>";

		String query = "for $node in xquery:eval(fn:concat(\"//\", '" + smellCode
				+ "')) return ( insert node element {xs:QName('" + smellCode + "BeforeComment')} {" + queryPart
				+ "} before $node)";

		try {
			baseX.applyXQuery(query);
		} catch (XQException e) {
			Logger.getLogger("BaseX").log(Level.SEVERE, "Connection to basex failed", e);
		}
	}

	public void insertVisualisationMarker(List<JessInput> searchResults) {

		for (JessInput curr : searchResults) {
			String smelltype = "unknown";
			for (SmellType currSmell : PostgresManager.getInstance().getSmellTypes()) {
				if (currSmell.getCode() != null && currSmell.getCode().equals(curr.getRefCode())) {
					smelltype = currSmell.getName();
				}
			}
			String marker = "//SMELL: #SmellType(" + smelltype + ")\n";
			insertCommentBeforeSmell(curr.getCode(), marker);
		}
	}

	public void insertSmellRefCodeTags(List<JessInput> searchResults) {
		for (JessInput curr : searchResults) {
			insertCommentAfterSmell(curr.getCode(),  "// smelltag start : " + curr.getCode());
			insertCommentBeforeSmell(curr.getCode(), "// smelltag end   : " + curr.getCode());

		}
	}
}
