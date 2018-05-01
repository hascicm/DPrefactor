package sk.fiit.dp.refactor.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import sk.fiit.dp.pathFinder.entities.OptimalPathForCluster;
import sk.fiit.dp.pathFinder.entities.stateSpace.State;
import sk.fiit.dp.refactor.command.PathFinderCommandHandler;
import sk.fiit.dp.refactor.command.sonarQube.SonarProperties;

public class Evaluation {

	public static double elapsedTime = 0;
	final static int numOfTests = 10;

	public static void main(String[] args) {
		PathFinderCommandHandler pathFinderCommand = PathFinderCommandHandler.getInstance();
		PathFinderCommandHandler.shouldReduce = true;
		String repo = "https://github.com/hascicm/DP_Refactor_Search_Space";
		String gituser = "username";
		String gitpass = "pass";
		String searchbranch = "pathfinder_eval";
		List<String> searchMethods = new ArrayList<String>();
		// 'CR','MCH','LM','LPL','DC','ECC','LC','LAZC','MAGIC','SS','FE'
		searchMethods.add("CR");
		searchMethods.add("MCH");
		searchMethods.add("LM");
		searchMethods.add("LPL");
		searchMethods.add("DC");
		searchMethods.add("ECC");
		searchMethods.add("LC");
		searchMethods.add("LAZC");
		searchMethods.add("MAGIC");
		searchMethods.add("SS");
		searchMethods.add("FE");

		boolean explanationToSearch = false;

		List<Boolean> clusteringToTest = new ArrayList<Boolean>();
		clusteringToTest.add(true);
		clusteringToTest.add(false);

		SonarProperties sonarProps = new SonarProperties();
		sonarProps.setSonarEnabled(false);

		List<String> methodToTest = new ArrayList<String>();
		//methodToTest.add("mc");
		// methodToTest.add("A*");
		// methodToTest.add("bee");
		methodToTest.add("ant");

		List<Integer> smellCountTOTest = new ArrayList<>();
		// smellCountTOTest.add(20);
		// smellCountTOTest.add(5);
		 smellCountTOTest.add(10);
		 smellCountTOTest.add(15);
		 smellCountTOTest.add(20);
		 smellCountTOTest.add(25);
		 smellCountTOTest.add(30);
//		smellCountTOTest.add(40);

		List<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		for (boolean clustering : clusteringToTest) {

			for (String method : methodToTest) {

				for (Integer smellCount : smellCountTOTest) {
					PathFinderCommandHandler.ReduceToNumberOfSmells = smellCount;

					for (int testNumber = 1; testNumber <= numOfTests; testNumber++) {
						State finalState = null;
						ArrayList<String> currResult;
						List<OptimalPathForCluster> result = pathFinderCommand.executePathFinder(repo, gituser, gitpass,
								searchbranch, searchMethods, explanationToSearch, clustering, sonarProps, method);
						for (OptimalPathForCluster r : result) {
							if (r != null && !r.getOptimalPath().isEmpty()) {
								finalState = r.getOptimalPath().get(r.getOptimalPath().size() - 1).getToState();
								System.out.println("method: " + method + "\tsmellcount: "
										+ PathFinderCommandHandler.ReduceToNumberOfSmells +"\tclustering:" + clustering +"\ttestnumber: "
										+ testNumber + "\ttime:" + elapsedTime + "\t finalstate: "
										+ finalState.toString());
							}
							
							elapsedTime = PathFinderCommandHandler.elapsedTime;
							currResult = new ArrayList<>();

							currResult.add(method);
							currResult.add("" + PathFinderCommandHandler.ReduceToNumberOfSmells);
							currResult.add("" + testNumber);
							currResult.add("" + clustering);
							currResult.add("" + elapsedTime);
							if (r != null && !r.getOptimalPath().isEmpty()) {
								currResult.add("" + finalState.getFitness());
								currResult.add("" + finalState.getDepth());
								currResult.add("" + finalState.getSmells().size());
								currResult.add(finalState.toString());
							}
							results.add(currResult);

						}
					}
				}
				// change method
			}
		}

		try

		{
			writeCSV(results);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (ArrayList<String> r : results) {
			for (String x : r) {
				System.out.print(x + "\t");
			}
			System.out.println();
		}
	}

	public static void setResultIme(double d) {
		elapsedTime = d;
	}

	private static final String SAMPLE_CSV_FILE = "./evaluation.csv";

	public static void writeCSV(List<ArrayList<String>> results) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));

				CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("method", "smellcount",
						"testnumber", "clustering", "time", "Fitness", "Depth", "NumOfSmells", "finalstate"));) {

			for (ArrayList<String> r : results) {
				csvPrinter.printRecord(Arrays.asList(r.toArray()));
			}

			csvPrinter.flush();
		}
	}
}
