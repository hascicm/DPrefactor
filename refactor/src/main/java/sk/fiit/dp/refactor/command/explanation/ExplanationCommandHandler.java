package sk.fiit.dp.refactor.command.explanation;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.google.common.io.Files;

import sk.fiit.dp.refactor.command.ConversionCommandHandler;
import sk.fiit.dp.refactor.command.GitCommandHandler;
import sk.fiit.dp.refactor.dbs.BaseXManager;
import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.helper.TimeStampGenerator;
import sk.fiit.dp.refactor.model.JessInput;
import sk.fiit.dp.refactor.model.explanation.JessListenerOutput;
import sk.fiit.dp.refactor.model.explanation.RepairRecord;

public class ExplanationCommandHandler {
	private static ExplanationCommandHandler INSTANCE;
	private GitCommandHandler git = GitCommandHandler.getInstance();
	private PostgreManager pg = PostgreManager.getInstance();
	private TimeStampGenerator timeStampGenerator = TimeStampGenerator.getInstance();
	private ConversionCommandHandler conversion = ConversionCommandHandler.getInstance();
	private BaseXManager baseX;

	private List<RepairRecord> records;

	private ExplanationCommandHandler() {
		baseX = BaseXManager.getInstance();
	}

	public static ExplanationCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ExplanationCommandHandler();
		}
		return INSTANCE;
	}

	public void createRepairRecord(String repo, List<JessInput> searchResults) {

		// initialize all records based on search results
		records = processSearchResults(searchResults);

		// set repo for all initialized records
		for (RepairRecord record : records) {
			record.setGitRepository(repo);
			record.setTimeStamp(timeStampGenerator.getTime());
		}


	}

	private List<RepairRecord> processSearchResults(List<JessInput> searchResults) {
		List<RepairRecord> records = new ArrayList<RepairRecord>();

		for (JessInput curr : searchResults) {
			RepairRecord act = new RepairRecord();
			act.setPath(curr.getXpatPosition());

			act.setRefcode(curr.getRefCode());
			act.setRefactoringCode(curr.getCode());
			records.add(act);
		}
		return records;
	}

	public void pushRecordsToPostgres() {
		for (RepairRecord record : records) {
			try {
				pg.AddRepairRecord(record);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void printrecords() {
		System.out.println("results--------------\n-----------------\n-----------------");
		for (RepairRecord record : records) {
			System.out.println("code w  : " + record.getRefactoringCode());
			System.out.println("code w/o: " + record.getRefcode());
			System.out.println("git     : " + record.getGitRepository());
			System.out.println("position: " + record.getPath());
			System.out.println("time    : " + record.getTimeStamp());
			if (record.getUsedJessRule() != null) {
				System.out.println("jess ---------------------");
				System.out.println("code       : " + record.getUsedJessRule().getCode());
				System.out.println("refcode    : " + record.getUsedJessRule().getRefCode());
				System.out.println("rule name  : " + record.getUsedJessRule().getRuleName());
				System.out.println("doc string : " + record.getUsedJessRule().getDocString());
			}
			System.out.println("----------smelly code -----------");
			System.out.println(record.getCodeBeforeRepair());
			System.out.println("----------repaired code----------");
			System.out.println(record.getCodeAfterRepair());
			System.out.println("---------------------------------");
		}
	}

	public void processJessListenerOutput() {
		for (RepairRecord record : records) {
			for (JessListenerOutput curr : RuleEngineEventHandler.getInstance().getListenerOutputObjects()) {
				if (record.getRefactoringCode().equals(curr.getCode())) {
					record.setUsedJessRule(curr);
				}
			}
		}
	}

	public void getRepairedSourceCode(List<JessInput> searchResults) {
		for (JessInput curr : searchResults) {
			String repairedCode = baseX.retrieveRepairedCourceCode(curr.getCode());
			processRepairedCode(curr.getCode(), repairedCode);
		}
		for (RepairRecord record : records) {
			String source = record.getCodeAfterRepair();
			//String withoutXMLTags = removeXMLTags(source);
			record.setCodeAfterRepair(formatSourceCodeString(source));
		}
	}

	private void processRepairedCode(String code, String repairedCode) {
		for (RepairRecord record : records) {
			if (record.getRefactoringCode().equals(code)) {
				
				record.setCodeAfterRepair(repairedCode);
			}
		}
	}

	public void getSmellySourceCode(List<JessInput> searchResults, String repoPath) {
		for (JessInput curr : searchResults) {
			String smellyCode = baseX.retrieveSmellySourceCode(curr.getCode());
			processSmellyCode(curr.getCode(), smellyCode);
		}
		// clear code of XML tags and format it 
		for (RepairRecord record : records) {
			String source = record.getCodeBeforeRepair();
			//String withoutXMLTags = removeXMLTags(source);
			record.setCodeBeforeRepair(formatSourceCodeString(source));
		}

	}

	private String removeXMLTags(String code) {
		return code.replaceAll("<comment.*>.*(\\s*)</comment>", "").replaceAll("<[^>]+>", "").replaceAll("\\s+", " ");
	}

	/**
	 * method for formating source code from string 
	 * @param code
	 *            source code without any formating
	 * @return if formatter is capable of formating source code,then formated
	 *         source code is returned, else code from argument is returned
	 */
	private String formatSourceCodeString(String code) {
		CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(null);

		TextEdit textEdit = codeFormatter.format(CodeFormatter.K_UNKNOWN, code, 0, code.length(), 0, null);
		IDocument doc = new Document(code);
		try {
			textEdit.apply(doc);
			return doc.get();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			return code;
		}
		return null;
	}

	private void processSmellyCode(String code, String sourceCode) {
		for (RepairRecord record : records) {
			if (record.getRefactoringCode().equals(code)) {
				record.setCodeBeforeRepair(sourceCode);
			}
		}
	}

}
