package sk.fiit.dp.refactor.dbs.connector;

import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;

import com.xqj2.XQConnection2;

import net.xqj.basex.BaseXXQDataSource;

public final class BaseXConnector {
	
	private XQConnection2 connection;
	private XQExpression expression;
	
	String name = "admin";
	String password = "admin";
	
	public void createDatabase(String database) throws XQException {
		if(connection != null && !connection.isClosed()) {
			connection.close();
		}
		
		XQDataSource source = new BaseXXQDataSource();
		source.setProperty("serverName", "localhost");
		source.setProperty("port", "1984");
		
		connection = (XQConnection2) source.getConnection(name, password);
		connection.createExpression().executeCommand("CREATE DB " + database);
		connection.close();
		
		source = new BaseXXQDataSource();
		source.setProperty("serverName", "localhost");
		source.setProperty("port", "1984");
		source.setProperty("databaseName", database);
		connection = (XQConnection2) source.getConnection(name, password);
		getExpression().executeCommand("SET EXPORTER indent=no");
		getExpression().executeCommand("SET STRIPNS ON");
		getExpression().executeCommand("SET CHOP OFF");
	}
	
	public void destroyDatabase(String database) throws XQException {
		if(connection != null && !connection.isClosed()) {
			getExpression().executeCommand("DROP DB " + database);
			connection.close();
		}
	}
	
	public XQExpression getExpression() throws XQException {
		if(expression == null || expression.isClosed()) {
			expression = connection.createExpression();
		}
		
		return expression;
	}
	
	public XQConnection2 getConnection() throws XQException {
		return connection;
	}
}