package sk.fiit.dp.refactor.command.explanation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.refactor.command.GitCommandHandler;
import sk.fiit.dp.refactor.dbs.PostgreManager;
import sk.fiit.dp.refactor.model.JessInput;
import sk.fiit.dp.refactor.model.explanation.JessListenerOutput;
import sk.fiit.dp.refactor.model.explanation.RepairExplanationTempObject;
import sk.fiit.dp.refactor.model.explanation.RepairRecord;

public class ExplanationCommandHandler {
	private static ExplanationCommandHandler INSTANCE;
	private GitCommandHandler git = GitCommandHandler.getInstance();
	private PostgreManager pg = PostgreManager.getInstance();
	private List<RepairRecord> records;

	private ExplanationCommandHandler() {
	}

	public static ExplanationCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ExplanationCommandHandler();
		}
		return INSTANCE;
	}

	public void createRepairRecord(String repo, List<JessInput> searchResults) {

		// initialize all records based on serach results
		records = processSearchExplanationFile();
		// set repo for all initialized records
		for (RepairRecord record : records) {
			record.setGitRepository(repo);
		}
		// link results from jess
		processJessListenerOutput();
		// proces repair file and link it to records
		processRepairExplanationFile();
		linkToResultFile(searchResults);
		printrecords();

		pushRecordsToPostgres();

	}

	private void linkToResultFile(List<JessInput> searchResults) {
		for (RepairRecord record : records) {
			for (JessInput curr : searchResults) {
				if (record.getRefactoringCode().equals(curr.getCode())) {
					record.setPath(curr.getPosition());
				}
			}
		}
	}

	private void pushRecordsToPostgres() {
		for (RepairRecord record : records) {
			try {
				pg.AddRepairRecord(record);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void printrecords() {
		System.out.println("results--------------\n-----------------\n-----------------");
		for (RepairRecord record : records) {
			System.out.println("refcode : " + record.getRefactoringCode());
			System.out.println("git     : " + record.getGitRepository());
			System.out.println("position: " + record.getPath());
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

	private List<RepairRecord> processSearchExplanationFile() {
		System.out.println("search file content");
		List<RepairRecord> records = new ArrayList<RepairRecord>();

		try {
			List<String> lines = Files.readAllLines(Paths.get(git.getRepoDirectory() + "\\explanation.txt"));
			String codeFromFile = "";
			RepairRecord curent = null;

			for (String l : lines) {
				if (l.startsWith("NAME: ")) {
					if (curent != null) {
						curent.setCodeBeforeRepair(codeFromFile);
						curent.setPath("");
						records.add(curent);
					}
					curent = new RepairRecord();
					curent.setRefactoringCode(l.replace("NAME: ", ""));
					curent.setRefcode(l.replace("NAME: ", "").replaceAll("[0-9]", ""));
					codeFromFile = "";
				} else {
					codeFromFile += l + "\n";
				}
			}
			if (curent != null) {
				curent.setCodeBeforeRepair(codeFromFile);
				curent.setPath("");
				records.add(curent);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("-------------------");
		return records;
	}

	private void processRepairExplanationFile() {
		System.out.println("repair file content");
		List<RepairExplanationTempObject> parsedRepairedSources = new ArrayList<RepairExplanationTempObject>();

		try {
			List<String> lines = Files.readAllLines(Paths.get(git.getRepoDirectory() + "\\explanationRepair.txt"));
			String sourceCodeFromFile = "";
			RepairExplanationTempObject o = null;

			for (String l : lines) {
				if (l.startsWith("NAME: ")) {
					if (o != null) {
						o.setCode(sourceCodeFromFile);
						parsedRepairedSources.add(o);
					}
					o = new RepairExplanationTempObject();
					o.setRefactoringCode(l.replace("NAME: ", ""));
					sourceCodeFromFile = "";

				} else {
					sourceCodeFromFile += l + "\n";
				}
				if (o != null) {
					o.setCode(sourceCodeFromFile);
					parsedRepairedSources.add(o);
				}
				System.out.println(l);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (RepairExplanationTempObject temp : parsedRepairedSources) {
			for (RepairRecord record : records) {
				if (temp.getRefactoringCode().equals(record.getRefactoringCode())) {
					record.setCodeAfterRepair(temp.getCode());
				}
			}

		}
		System.out.println("-------------------");
	}

	private void processJessListenerOutput() {
		for (RepairRecord record : records) {
			for (JessListenerOutput curr : RuleEngineEventHandler.getInstance().getListenerOutputObjects()) {
				if (record.getRefactoringCode().equals(curr.getCode())) {
					record.setUsedJessRule(curr);
				}
			}
		}
	}

}
