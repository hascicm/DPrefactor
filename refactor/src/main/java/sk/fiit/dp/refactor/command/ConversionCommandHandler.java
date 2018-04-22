package sk.fiit.dp.refactor.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.fiit.dp.refactor.model.explanation.RepairRecord;

public class ConversionCommandHandler {
	final static Logger logger = LoggerFactory.getLogger(ConversionCommandHandler.class);
	private static ConversionCommandHandler INSTANCE;

	private ConversionCommandHandler() {
	}

	public static ConversionCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ConversionCommandHandler();
		}

		return INSTANCE;
	}

	/**
	 * Konvertuje vsetky java subory v zadanom adresari na xml reprezentaciu.
	 * Vykonava sa rekurzivne nad podaresarmi
	 * 
	 * @param directory
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public List<File> convertJavaToXml(String directory) throws IOException, InterruptedException {
		List<File> xmlFiles = new ArrayList<File>();
		Path dirPath = Paths.get(directory);

		Files.walk(dirPath).filter(path -> path.getFileName().toString().endsWith(".java")).forEach(path -> {
			String filePath = path.toAbsolutePath().toString();
			String xmlFile = filePath.replace(".java", ".xml");
			executeSrcML(filePath, xmlFile);

			try {
				Path xmlPath = Paths.get(xmlFile);
				String content = new String(Files.readAllBytes(xmlPath));
				content = content.replaceAll(" xmlns=.*java\"", "");
				// String fail = "<?xml version=\"1.0\" encoding=\"UTF-8\"
				// standalone=\"yes\"?>";
				// content = content.replace(fail, "");
				// content = content.replaceAll("</unit>\n", "</unit>");
				// content = tagTextInXml(content);
				// content = content.replace("\n</text><unit>", "<unit>");
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
	 * 
	 * @param xmlFiles
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void convertXmlToJava(List<File> xmlFiles) throws InterruptedException, IOException {
		for (File file : xmlFiles) {
			String filePath = file.getAbsolutePath().toString();
			String javaFile = filePath.replace(".xml", ".java");
			executeSrcML(filePath, javaFile);
			Files.delete(file.toPath());
		}

		// Path dirPath = Paths.get(directory);
		//
		// Files.walk(dirPath)
		// .filter(path -> path.getFileName().toString().endsWith(".xml"))
		// .forEach(path -> {
		// String filePath = path.toAbsolutePath().toString();
		// String javaFile = filePath.replace(".xml", ".java");
		// executeSrcML(filePath, javaFile);
		// try {
		// Files.delete(path);
		// } catch (IOException e) {
		// logger.error(e.getMessage(), e);
		// }
		// });
	}

	/**
	 * Vykona srcML program. Musi byt nainstalovany v danom adresari.
	 * 
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
	 * 
	 * @param files
	 * @param directory
	 * @throws IOException
	 */
	public void moveFilesToOriginalLocation(List<File> files, String directory) throws IOException {
		for (File file : files) {
			byte[] content = Files.readAllBytes(Paths.get(directory + "\\" + file.getName()));
			Files.write(Paths.get(file.getAbsolutePath()), content);
			Files.delete(Paths.get(directory + "\\" + file.getName()));
		}
	}

	public void removeCustomElements(String directory) throws IOException {
		File project = new File(directory);
		File[] files = project.listFiles();

		List<String> foundResult = Files.readAllLines(Paths.get("D:\\result.txt"));

		for (String result : foundResult) {
			for (int i = 0; i < files.length; ++i) {
				if (files[i].getName().endsWith(".xml")) {
					String context = new String(Files.readAllBytes(Paths.get(files[i].getAbsolutePath())));
					context = context.replace("</" + result + ">", "");
					context = context.replace("<" + result + ">",
							"<comment type=\"line\">" + "//CODE-SMELL:" + result + "</comment>");
					Files.write(Paths.get(files[i].getAbsolutePath()), context.getBytes());
				}
			}
		}
	}

	public String convertXMLStringToJavaString(String smellyCode) {
		String result = "";
		File temp = null;
		try {
			temp = File.createTempFile("stringconverter_tempfile", ".xml");

			// create temp file
			System.out.println("create temp file");
			// write xml string to it
			BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
			bw.write(smellyCode);
			bw.close();
			System.out.println("write xml string to it");
			// execute srcml
		} catch (IOException e) {
			e.printStackTrace();
		}

		String javaFile = temp.getAbsolutePath().replace(".xml", ".java");
		System.out.println("converting " + temp.getAbsolutePath());
		System.out.println("to         " + javaFile);
		executeSrcML(temp.getAbsolutePath(), javaFile);

		try {

			System.out.println(temp.getAbsolutePath());

			BufferedReader br = new BufferedReader(new FileReader(javaFile));

			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				result += currentLine;
			}
			System.out.println("------------------conversion result");
			System.out.println(result);

			File output = new File(javaFile);

			br.close();
			output.delete();
			temp.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
		// if (!result.isEmpty()) {
		// return formatSourceCode(result);
		// } else
		// return "";
	}

	private String formatSourceCode(String code) {

		CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(null);

		TextEdit textEdit = codeFormatter.format(CodeFormatter.K_UNKNOWN, code, 0, code.length(), 0, null);
		IDocument doc = new Document(code);
		try {
			textEdit.apply(doc);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return doc.get();
	}

	public void convertJavaToXml(Map<String, File> map) {
		for (Entry<String, File> entry : map.entrySet()) {
			String xmlFile = entry.getValue().getAbsolutePath();
			
			String javaFile = xmlFile.replace(".xml", ".java");
			executeSrcML(xmlFile, javaFile);

		}

	}

	// private String tagTextInXml(String xmlContent) {
	// xmlContent = xmlContent.replaceAll(">(?=(?<=>)[^<>]+)", "><text>");
	// return xmlContent.replaceAll("(?<=[^>])<(?!text)", "</text><");
	// Pattern pattern = Pattern.compile("(?<=>)[^<>]+(?=<)");
	// Matcher matcher = pattern.matcher(xmlContent);
	//
	// List<String> matches = new ArrayList<String>();
	// while(matcher.find()) {
	// matches.add(matcher.group());
	// }
	//
	// int index = 0;
	// StringBuffer result = new StringBuffer();
	//
	// matcher.reset();
	// while(matcher.find()) {
	// matcher.appendReplacement(result, "<text>" + matches.get(index) +
	// "</text>");
	// ++index;
	// }
	//
	// return result.toString();
	// }

	// private String removeTextTagsFromXml(String xmlContent) {
	// return xmlContent.replace("</?text>", "");
	// }
}