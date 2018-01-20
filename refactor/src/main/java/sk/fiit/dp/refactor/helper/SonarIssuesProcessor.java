package sk.fiit.dp.refactor.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import sk.fiit.dp.refactor.model.SonarIssue;

public class SonarIssuesProcessor {
	
	
	public static List<SonarIssue> convertSonarOutput(String sonarOutput){
		
		ArrayList<SonarIssue> results = new ArrayList<SonarIssue>(); 
		
		JSONObject sonarIssuesJson = new JSONObject(sonarOutput);
		
		
		if(sonarIssuesJson.getInt("total") > 0){
			
			JSONArray issuesJson = sonarIssuesJson.getJSONArray("issues");
			for(int i = 0; i < sonarIssuesJson.getInt("total"); i++){
				JSONObject issueJson = issuesJson.getJSONObject(i);
				
				int startLine = 0; 
				int endLine = -1; 
				
				if(issueJson.has("textRange")){
					
					JSONObject textRangeJson = issueJson.getJSONObject("textRange");
					
					startLine = textRangeJson.getInt("startLine");
					endLine = textRangeJson.getInt("endLine");
							
					
				}else if(issueJson.has("line")){
					startLine = issueJson.getInt("line"); 
				}
				
				results.add(new SonarIssue(i,startLine, endLine, issueJson.getString("message"), issueJson.getString("component"))); 
			}
		}
			
		return results; 
	}
	
	public static void addSonarIssuesToCode(String path, List<SonarIssue> issues){
		
		//SORT BY FILE
		issues.sort((o1, o2) -> o1.getFile().compareTo(o2.getFile()));
		
		for(SonarIssue sonarIssue : issues){
			
			String relativePath = sonarIssue.getFile().split(":", 2)[1];
			
			Path filePath = Paths.get(path + "\\" + relativePath);
			
			try {
				
				List<String> lines = Files.readAllLines(filePath);
				int count = 1;
				int inputCount = 0; 
				int endInputCount = -1;
				
				if(sonarIssue.getStartLine() != 0){
					for(String line : lines){
						inputCount++;
						if(line.startsWith("//SONAR_")){
							continue;
						}
						
						if(count == sonarIssue.getStartLine()){
							break;
						}
	
						count++; 
					}
				}else{
					inputCount = 1;
				}
				
				if(sonarIssue.getEndLine() != -1){
					
					endInputCount = 0;
					count = 1;
					
					for(String line : lines){
						endInputCount++;
						if(line.startsWith("//SONAR_")){
							continue;
						}
						
						if(count == sonarIssue.getEndLine()){
							break;
						}
	
						count++; 
					}
				}
								
				if(endInputCount != -1){
					lines.add(endInputCount, "//SONAR_" + sonarIssue.getId() + "_END");
				}
				
				lines.add(inputCount-1, "//SONAR_" + sonarIssue.getId() + " : " + sonarIssue.getMessage());
			
				
					
				
				Files.write(filePath, lines, StandardCharsets.UTF_8); 
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	
	
}
