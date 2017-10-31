package sk.fiit.dp.refactor.dbs.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PotgreConnector {
	
	private Connection connection;
	private Statement statement;
	
	public PotgreConnector() throws SQLException, ClassNotFoundException {
		createConnection();
	}
	
	private void createConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver"); 
		connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/refactor", "postgres", "admin");
		statement = connection.createStatement();
	}
	
	public Statement getStatement() {
		return statement;
	}
}