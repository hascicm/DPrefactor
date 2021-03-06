package sk.fiit.dp.refactor.command.sonarQube;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import org.json.JSONObject;
import org.sonarsource.scanner.api.EmbeddedScanner;

public class SonarQubeWrapper {

	private static final String PROJECT_BASEDIR_PROPERTY = "sonar.projectBaseDir";
	private boolean isUploaded = false;
	private SonarProperties sonarProps;

	private static SonarQubeWrapper INSTANCE = null;

	private SonarQubeWrapper() {

	}

	public static SonarQubeWrapper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SonarQubeWrapper();
		}
		return INSTANCE;
	}

	public void analyzeProject(String projectKey, String repoPath) {

		Properties allProps = new Properties();
		// allProps.put(PROJECT_BASEDIR_PROPERTY,
		// "C:\\Users\\Lukas\\workspace_neon_ee\\SONARScanner");
		allProps.put(PROJECT_BASEDIR_PROPERTY, repoPath);
		allProps.put("sonar.host.url", this.sonarProps.getHostName());
		allProps.put("sonar.projectKey", projectKey);
		// allProps.put("sonar.projectName", "MyFirstSonarProject");
		allProps.put("sonar.login", this.sonarProps.getLoginName());
		allProps.put("sonar.password", this.sonarProps.getLoginPassword());
		allProps.put("sonar.sources", repoPath);
		allProps.put("sonar.java.binaries", repoPath + "\\bin");

		EmbeddedScanner runner = EmbeddedScanner.create(new Logger()).addGlobalProperties(allProps);

		try {
			runner.start();
			runner.runAnalysis(allProps);
		}catch(IllegalStateException exeption){
			//TODO - osetrenie vynimky
		} finally {
			runner.stop();
			this.isUploaded = true;
		}
	}

	//Prerobit na iny thread
	public String getIssues(String projectKey) {
		
		StringBuilder sb = new StringBuilder();
		String urlString = this.getSonarProps().getHostName() + "/api/issues/search?componentRoots=" + projectKey;
		System.out.println();
		
		while (!this.isUploaded) {
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			while(true){
				URL url = new URL(urlString);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
				
				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}
	
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
	
				String response;
				while ((response = br.readLine()) != null) {
					sb.append(response);
				}
				
				conn.disconnect();
				JSONObject json = new JSONObject(sb.toString());
				
				//wait for upload on server
				if(json.getJSONArray("components").length() > 0){
					break;
				}
				
				Thread.sleep(100);
				sb = new StringBuilder();
				System.out.print(".");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb.toString();
	}

	public boolean deleteProject(String projectKey) {

		String urlString = this.getSonarProps().getHostName() + "/api/projects/delete?key=" + projectKey;

		try {
			URL url = new URL(urlString);
			String encoding = Base64.getEncoder().encodeToString("admin:admin".getBytes("utf-8"));

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", "Basic " + encoding);
			InputStream content = (InputStream) connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public SonarProperties getSonarProps() {
		return sonarProps;
	}

	public void setSonarProps(SonarProperties sonarProps) {
		this.sonarProps = sonarProps;
	}
}
