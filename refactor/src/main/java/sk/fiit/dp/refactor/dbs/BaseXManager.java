package sk.fiit.dp.refactor.dbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQResultSequence;

import com.xqj2.XQConnection2;

import sk.fiit.dp.refactor.command.GitCommandHandler;
import sk.fiit.dp.refactor.dbs.connector.BaseXConnector;

public class BaseXManager {

	private static BaseXManager INSTANCE;

	private BaseXConnector connector;
	private XQConnection2 connection;
	private XQExpression expression;

	private BaseXManager() {
		connector = new BaseXConnector();
	}

	public static BaseXManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new BaseXManager();
		}

		return INSTANCE;
	}

	/**
	 * Priprava novej databazy
	 * 
	 * @param database
	 * @throws XQException
	 */
	public void prepareDatabase(String database) throws XQException {
		connector.createDatabase(database);
		connection = connector.getConnection();
		expression = connector.getExpression();
	}

	/**
	 * Vycistrenie existujucej databazy
	 * 
	 * @param database
	 * @throws XQException
	 */
	public void cleanDatabase(String database) throws XQException {
		connector.destroyDatabase(database);
	}

	/**
	 * Vlozenie XML dokumentu do databazy
	 * 
	 * @param file
	 * @param fileName
	 * @throws IOException
	 * @throws XQException
	 */
	public void insertDocument(FileInputStream file, String fileName) throws IOException, XQException {
		XQItem item = connection.createItemFromDocument(file, null, null);
		connection.insertItem(fileName, item, null);
	}

	/**
	 * Exportovanie dokumentov z databazy
	 * 
	 * @param path
	 * @throws XQException
	 */
	public void exportDatabase(String path) throws XQException {
		expression.executeCommand("EXPORT " + path);
	}

	/**
	 * Aplikovanie XQuery skriptu
	 * 
	 * @param content
	 * @throws XQException
	 */
	public void applyXQuery(String content) throws XQException {
		expression.executeQuery(content);
	}

	/**
	 * Aplikovanie XQuery search skriptu s viazanim premennej na skript
	 * 
	 * @param content
	 * @param variableName
	 * @param value
	 * @throws XQException
	 */
	public void applySearchXQuery(String content, String variableName, String value, boolean exportNode)
			throws XQException {
		expression.bindString(new QName(variableName), value, null);
		if (exportNode) {
			expression.bindString(new QName("explanation"),
					GitCommandHandler.getInstance().getRepoDirectory() + "\\explanation.txt", null);
		}
		//System.out.println(content);
		XQResultSequence x = expression.executeQuery(content);
		// TODO process output
		while (x.next()) {
			// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			// System.out.println(x.getAtomicValue());
		}
	}

	/**
	 * Aplikovanie XQuery skriptu s viazanim premennej na skript
	 * 
	 * @param content
	 * @param variableName
	 * @param value
	 * @throws XQException
	 */
	public void applyRepairXQuery(String content, String variableName, String value, boolean exportNode)
			throws XQException {
		expression.bindString(new QName(variableName), value, null);
		// BIND OF EXPLANATION FILE
		if (exportNode) {
			expression.bindString(new QName("explanation"),
					GitCommandHandler.getInstance().getRepoDirectory() + "\\explanationRepair.txt", null);
		}
		// TODO process output
		XQResultSequence x = expression.executeQuery(content);
		while (x.next()) {
			// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			// System.out.println(x.getAtomicValue());
		}
	}
	/**
	 * 
	 * @param script
	 * @param variableName
	 * @param value
	 * @return positions
	 * @throws XQException
	 */
	public List<String> applyPositionXQuery(String script, String variableName, String value) throws XQException {
		expression.bindString(new QName(variableName), value, null);
		List<String> out = new ArrayList<String>();
		XQResultSequence x = expression.executeQuery(script);
		while (x.next()) {
			out.add(x.getAtomicValue());
		}
		return out;
	}

	/**
	 * Importovanie celeho projektu do databazy
	 * 
	 * @param xmlFiles
	 * @throws IOException
	 * @throws XQException
	 */
	public void projectToDatabase(List<File> xmlFiles) throws IOException, XQException {
		for (File file : xmlFiles) {
			FileInputStream stream = new FileInputStream(file);
			insertDocument(stream, file.getName());
			stream.close();
		}
	}
}