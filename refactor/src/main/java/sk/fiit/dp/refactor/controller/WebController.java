package sk.fiit.dp.refactor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import sk.fiit.dp.refactor.command.RefactorCommandHandler;
import sk.fiit.dp.refactor.command.ResourceCommandHandler;

@Path("")
public class WebController {

	private ResourceCommandHandler resourceCommand = ResourceCommandHandler.getInstance();
	private RefactorCommandHandler refactorCommand = RefactorCommandHandler.getInstance();
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
	@Path("/execute/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String execute(String input) {
		
		JSONObject json = new JSONObject(input);
		JSONArray toSearch = json.getJSONArray("searchCodes");
		JSONArray toRepair = json.getJSONArray("repairCodes");
		boolean explanationToSearch = json.getBoolean("explanationSearch");
		
		//TODO
		List<String> searchMethods = new ArrayList<>();
		for (int i = 0; i < toSearch.length(); ++i) {
			searchMethods.add(toSearch.getString(i));
		}

		List<String> allowedRefactoring = new ArrayList<>();
		for (int i = 0; i < toRepair.length(); ++i) {
			allowedRefactoring.add(toRepair.getString(i));
		}

		Map<String, Integer> results = refactorCommand.executeRefactoring(json.getString("repo"),
				json.getString("name"), json.getString("password"),
				json.getString("searchBranch"),
				json.getString("repairBranch"), searchMethods, allowedRefactoring,explanationToSearch);

		JSONObject response = new JSONObject();
		for (String key : results.keySet()) {
			response.put(key, results.get(key));
		}


		
		return response.toString();
	}
}
