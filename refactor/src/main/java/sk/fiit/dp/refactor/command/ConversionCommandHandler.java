package sk.fiit.dp.refactor.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversionCommandHandler {
	final static Logger logger = LoggerFactory.getLogger(ConversionCommandHandler.class);
	private static ConversionCommandHandler INSTANCE;
	
	private ConversionCommandHandler() {
	}
	
	public static ConversionCommandHandler getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new ConversionCommandHandler();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Konvertuje vsetky java subory v zadanom adresari na xml reprezentaciu.
	 * Vykonava sa rekurzivne nad podaresarmi
	 * @param directory
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public List<File> convertJavaToXml(String directory) throws IOException, InterruptedException {
		List<File> xmlFiles = new ArrayList<File>();
		Path dirPath = Paths.get(directory);
		
		Files.walk(dirPath)
		.filter(path -> path.getFileName().toString().endsWith(".java"))
		.forEach(path -> {
			String filePath = path.toAbsolutePath().toString();
			String xmlFile = filePath.replace(".java", ".xml");
			executeSrcML(filePath, xmlFile);

			try {
				Path xmlPath = Paths.get(xmlFile);
				String content = new String(Files.readAllBytes(xmlPath));
				content = content.replaceAll(" xmlns=.*java\"", "");
//				String fail = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
//				content = content.replace(fail, "");
//				content = content.replaceAll("</unit>\n", "</unit>");
//				content = tagTextInXml(content);
//				content = content.replace("\n</text><unit>", "<unit>");
				Files.write(xmlPath, content.getBytes());
			} catch (IOException e) {
				logger.info(e.getMessage());
			}

			xmlFiles.add(new File(xmlFile));
		});
		
		return xmlFiles;
	}
	
	/**
	 * Konverzia XML suborov do Java zdrojoveho kodu
	 * @param xmlFiles
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void convertXmlToJava(List<File> xmlFiles) throws InterruptedException, IOException {
		for(File file : xmlFiles) {
			String filePath = file.getAbsolutePath().toString();
			String javaFile = filePath.replace(".xml", ".java");
			executeSrcML(filePath, javaFile);
			//TODO delete xml files 
			//Files.delete(file.toPath());
		}
		
//		Path dirPath = Paths.get(directory);
//		
//		Files.walk(dirPath)
//		.filter(path -> path.getFileName().toString().endsWith(".xml"))
//		.forEach(path -> {
//			String filePath = path.toAbsolutePath().toString();
//			String javaFile = filePath.replace(".xml", ".java");
//			executeSrcML(filePath, javaFile);
//			try {
//				Files.delete(path);
//			} catch (IOException e) {
//				logger.error(e.getMessage(), e);
//			}
//		});
	}
	
	/**
	 * Vykona srcML program.
	 * Musi byt nainstalovany v danom adresari.
	 * @param input
	 * @param output
	 */
	public void executeSrcML(String input, String output) {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("C:\\Program Files (x86)\\srcML\\bin\\srcML.exe", input, "-o", output);
			builder.redirectErrorStream(true);
			Process process = builder.start();
			process.waitFor();
			logger.info("File " + input + " was converted into " + output);
		} catch (IOException | InterruptedException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Vyexportovane subory sa presunu na povodnu lokaciu
	 * @param files
	 * @param directory
	 * @throws IOException
	 */
	public void moveFilesToOriginalLocation(List<File> files, String directory) throws IOException {
		for(File file : files) {
			byte[] content = Files.readAllBytes(Paths.get(directory + "\\" + file.getName()));
			Files.write(Paths.get(file.getAbsolutePath()), content);
			Files.delete(Paths.get(directory + "\\" + file.getName()));
		}
	}
	
	public void removeCustomElements(String directory) throws IOException {
		File project = new File(directory);
		File[] files = project.listFiles();
		
		List<String> foundResult = Files.readAllLines(Paths.get("D:\\result.txt"));
		
		for(String result : foundResult) {
			for(int i = 0; i < files.length; ++i) {
				if(files[i].getName().endsWith(".xml")) {
					String context = new String(Files.readAllBytes(Paths.get(files[i].getAbsolutePath())));
					context = context.replace("</" + result + ">", "");
					context = context.replace("<" + result + ">", "<comment type=\"line\">" + "//CODE-SMELL:" + result + "</comment>");
					Files.write(Paths.get(files[i].getAbsolutePath()), context.getBytes());
				}
			}
		}
	}
	
//	private String tagTextInXml(String xmlContent) {
//		xmlContent = xmlContent.replaceAll(">(?=(?<=>)[^<>]+)", "><text>");
//		return xmlContent.replaceAll("(?<=[^>])<(?!text)", "</text><");
//		Pattern pattern = Pattern.compile("(?<=>)[^<>]+(?=<)");
//		Matcher matcher = pattern.matcher(xmlContent);
//		
//		List<String> matches = new ArrayList<String>();
//		while(matcher.find()) {
//			matches.add(matcher.group());
//		}
//		
//		int index = 0;
//		StringBuffer result = new StringBuffer();
//		
//		matcher.reset();
//		while(matcher.find()) {
//			matcher.appendReplacement(result, "<text>" + matches.get(index) + "</text>");
//			++index;
//		}
//		
//		return result.toString();
//	}
	
//	private String removeTextTagsFromXml(String xmlContent) {
//		return xmlContent.replace("</?text>", "");
//	}
}