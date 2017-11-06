package sk.fiit.dp.refactor.helper;

import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;



public class JsonFileWriter {
	
	public static void writeJsonToFile(String jsonString, String path){
		
		try (FileWriter file = new FileWriter(path)) {
				
			
			JSONObject obj = new JSONObject(jsonString);
		
			file.write(obj.toString(4));
			file.flush();
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
