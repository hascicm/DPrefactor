package sk.fiit.dp.refactor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import sk.fiit.dp.pathFinder.entities.OptimalPathForCluster;
import sk.fiit.dp.refactor.command.PathFinderCommandHandler;
import sk.fiit.dp.refactor.command.RefactorCommandHandler;
import sk.fiit.dp.refactor.command.ResourceCommandHandler;
import sk.fiit.dp.refactor.command.sonarQube.SonarProperties;

@Path("")
public class WebController {

	private ResourceCommandHandler resourceCommand = ResourceCommandHandler.getInstance();
	private RefactorCommandHandler refactorCommand = RefactorCommandHandler.getInstance();
	private PathFinderCommandHandler pathFinderCommand = PathFinderCommandHandler.getInstance();

	@GET
	@Path("/rulesdefinition")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRulesDefinition() {
		JSONObject result = new JSONObject();
		result.append("value", resourceCommand.getRulesDefinition());

		return result.toString();
	}

	@POST
	@Path("/rulesdefinition")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setRulesDefinition(String input) {
		JSONObject json = new JSONObject(input);

		resourceCommand.setRules((String) json.get("value"));
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSearchMethods() {
		return resourceCommand.getSearchMethods();
	}

	@GET
	@Path("/search/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSearchScript(@PathParam("code") String code) {
		return resourceCommand.getSearchScript(code);
	}

	@GET
	@Path("/repair")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRefactorMethods() {
		return resourceCommand.getRefactoringMethods();
	}

	@GET
	@Path("/repair/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRefactorScript(@PathParam("code") String code) {
		return resourceCommand.getRepairScript(code);
	}

	@GET
	@Path("/records")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRefactoringRecords() {
		return resourceCommand.getRefactoringRecords();
	}

	@GET
	@Path("/pathfinerresults")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPathFinderResults() {
		return resourceCommand.getPathFinderRecords();
	}

	@GET
	@Path("/PathFinderAnalysisDetail/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPathFinderAnalysisDetail(@PathParam("id") int id) {
		return resourceCommand.getPathFinderRecordDetail(id);
	}

	@GET
	@Path("/PathFinderAnalysisCluster/{analysisid}/{clusterid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPathFinderAnalysisCluster(@PathParam("analysisid") int analysisId,
			@PathParam("clusterid") int clusterNumber) {
		return resourceCommand.getPathFinderAnalysisCluster(analysisId, clusterNumber);
	}

	@GET
	@Path("/getPathFinderRepair/{clusterid}/{repairid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPathFinderResultRepair(@PathParam("clusterid") int clusterid,
			@PathParam("repairid") int repairid) {
		String x = resourceCommand.getPathFinderResultRepair(clusterid, repairid);
		System.out.println(x);
		return x;
	}

	@GET
	@Path("/records/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRefactoringRecord(@PathParam("id") int id) {
		return resourceCommand.getRefactoringRecord(id);
	}

	@POST
	@Path("/repair/{code}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setRefactorScript(@PathParam("code") String code, String input) {
		resourceCommand.setRepairScript(code, input);
	}

	@POST
	@Path("/search/{code}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setSearchScript(@PathParam("code") String code, String input) {
		resourceCommand.setSearchScript(code, input);
	}

	@PUT
	@Path("/search/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addSearchScript(String input) {
		resourceCommand.addSearchScript(input);
	}

	@PUT
	@Path("/repair/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addRepairScript(String input) {
		resourceCommand.addRepairScript(input);

	}

	@PUT
	@Path("/executePathFinder/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String executePathFinder(String input) {

		JSONObject json = new JSONObject(input);
		JSONArray toSearch = json.getJSONArray("searchCodes");
		boolean explanationToSearch = json.getBoolean("explanationSearch");
		boolean clusteringEnabled = true;
		String gituser = json.getString("name");
		String gitpass = json.getString("password");
		String method = json.getString("selectedMethod");
		SonarProperties sonarProps = new SonarProperties();
		sonarProps.setSonarEnabled(json.getBoolean("isSonarEnabled"));
		if (sonarProps.isSonarEnabled()) {
			sonarProps.setHostName(json.getString("sonarHost"));
			sonarProps.setLoginName(json.getString("sonarLogin"));
			sonarProps.setLoginPassword(json.getString("sonarPassword"));
		}

		List<String> searchMethods = new ArrayList<>();
		for (int i = 0; i < toSearch.length(); ++i) {
			searchMethods.add(toSearch.getString(i));
		}

		List<OptimalPathForCluster> results = pathFinderCommand.executePathFinder(json.getString("repo"), gituser,
				gitpass, json.getString("searchBranch"), searchMethods, explanationToSearch, clusteringEnabled,
				sonarProps, method);

		JSONArray response = new JSONArray();
		for (OptimalPathForCluster r : results) {
			response.put(r.toJSON());
		}
		Logger.getLogger("webcontroller").log(Level.INFO, "final part");
		System.out.println(response.toString());
		return response.toString();
	}

	@PUT
	@Path("/execute/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String execute(String input) {

		JSONObject json = new JSONObject(input);
		JSONArray toSearch = json.getJSONArray("searchCodes");
		JSONArray toRepair = json.getJSONArray("repairCodes");
		boolean explanationToSearch = json.getBoolean("explanationSearch");
		boolean createrepairrecord = json.getBoolean("createrepairrecord");
		String gituser = json.getString("name");
		String gitpass = json.getString("password");

		SonarProperties sonarProps = new SonarProperties();
		sonarProps.setSonarEnabled(json.getBoolean("isSonarEnabled"));
		if (sonarProps.isSonarEnabled()) {
			sonarProps.setHostName(json.getString("sonarHost"));
			sonarProps.setLoginName(json.getString("sonarLogin"));
			sonarProps.setLoginPassword(json.getString("sonarPassword"));
		}

		// TODO
		List<String> searchMethods = new ArrayList<>();
		for (int i = 0; i < toSearch.length(); ++i) {
			searchMethods.add(toSearch.getString(i));
		}

		List<String> allowedRefactoring = new ArrayList<>();
		for (int i = 0; i < toRepair.length(); ++i) {
			allowedRefactoring.add(toRepair.getString(i));
		}

		Map<String, Integer> results = refactorCommand.executeRefactoring(json.getString("repo"), gituser, gitpass,
				json.getString("searchBranch"), json.getString("repairBranch"), searchMethods, allowedRefactoring,
				explanationToSearch, createrepairrecord, sonarProps);

		JSONObject response = new JSONObject();
		for (String key : results.keySet()) {
			response.put(key, results.get(key));
		}
		return response.toString();
	}
}
