package sk.fiit.dp.refactor.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;



public class JsonFileWriter {
	
	public static void writeJsonToFile(String jsonString, String path){
		
		try (FileWriter file = new FileWriter(path)) {
				
			
			JSONObject obj = new JSONObject(jsonString);
		
			file.write(obj.toString(4));
			file.flush();
		
		} catch (IOException e1) {
			Logger.getGlobal().log(Level.SEVERE, "writeJsonToFile", e1);
		}
	}
}
