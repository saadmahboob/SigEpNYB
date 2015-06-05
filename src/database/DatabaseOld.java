/**
 * 
 */
package database;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import data.AccountData;
import data.Event;
import data.Roles;


/**
 * Accesses the database
 */
public class DatabaseOld implements AutoCloseable {
	private final Connection connection;
	
	/** Creates a new database connection 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException */
	public DatabaseOld() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Properties properties = new Properties();
		properties.put("user", "fratsite");
		properties.put("password", "jeff");
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/fratdata", properties);
	}
	
	/** Generates a random alpha-numeric string */
	private String generateRandomStr(int length) {
		return new BigInteger(length * 5, new SecureRandom()).toString(32);
	}
	
	/** Removes all inactive tokens */
	/*public int cleanTokens() throws SQLException {
		int numCleaned = 0;
		
		Statement statement = connection.createStatement();
		ResultSet results = statement.executeQuery("SELECT * FROM tokens");
		while (results.next()) {
			if (checkToken(results.getString("token"))) {
				numCleaned++;
			}
		}
		
		statement.close();
		return numCleaned;
	}*/
	
	/** Updates the given token's lastActive field to now 
	 * @throws SQLException */
	private void tokenActivity(String token) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(String.format("UPDATE tokens SET lastActive=? WHERE token='%s'", token));
		statement.setTimestamp(1, getNow());
		statement.executeUpdate();
		
		statement.close();
	}
	
	/** Gets the account for the given token or null if the token is invalid 
	 * @throws SQLException */
	public AccountData getAccount(String token) throws SQLException {
		//TODO check token
		Statement statement = connection.createStatement();
		String sql = String.format("SELECT * FROM accounts JOIN tokens ON accounts.idAccount=tokens.idAccount WHERE token = '%s'", token);
		ResultSet results = statement.executeQuery(sql);
		if (results.next()) {
			int id = results.getInt("idAccount");
			String netid = results.getString("netid");
			String firstName = results.getString("firstName");
			String lastName = results.getString("lastName");
			statement.close();
			return new AccountData(id, netid, firstName, lastName);
		} else {
			statement.close();
			return null;
		}
	}
	
	/** Gets all of the accounts */
	public AccountData[] getAccounts() throws SQLException {
		Statement statement = connection.createStatement();
		String sql = "SELECT idAccount, netid, firstName, lastName FROM accounts";
		ResultSet results = statement.executeQuery(sql);
		
		List<AccountData> accounts = new LinkedList<>();
		while (results.next()) {
			int id = results.getInt("idAccount");
			String netid = results.getString("netid");
			String firstName = results.getString("firstName");
			String lastName = results.getString("lastName");
			accounts.add(new AccountData(id, netid, firstName, lastName));
		}
		statement.close();
		return accounts.toArray(new AccountData[accounts.size()]);
	}
	
	/** Creates an account 
	 * @throws SQLException */
	public void createAccount(String netid, String firstName, String lastName) throws SQLException {
		String password = generateRandomStr(5);
		
		Statement statement = connection.createStatement();
		String sql = String.format("INSERT INTO accounts (netid, password, firstName, lastName)"
				+ "VALUES ('%s', '%s', '%s', '%s')", netid, password, firstName, lastName);
		statement.executeUpdate(sql);
		
		ResultSet results = statement.executeQuery(String.format("SELECT idAccount from accounts WHERE netid='%s'", netid));
		results.next();
		int idAccount = results.getInt("idAccount");
		
		statement.executeUpdate(String.format("INSERT INTO user_roles (idAccount, idRole) VALUES (%d, 1)", idAccount));
		
		statement.close();
	}
	
	/** Deletes an account 
	 * @throws SQLException */
	public void deleteAccount(String netid) throws SQLException {
		Statement statement = connection.createStatement();
		ResultSet results = statement.executeQuery(String.format("SELECT idAccount FROM accounts WHERE netid='%s'", netid));
		results.next();
		int idAccount = results.getInt("idAccount");
		statement.executeUpdate(String.format("DELETE FROM user_roles WHERE idAccount=%d", idAccount));
		statement.executeUpdate(String.format("DELETE FROM accounts WHERE idAccount=%d", idAccount));
		statement.close();
	}
	
	/** Creates an event 
	 * @throws SQLException */
	public void createEvent(String title, Date start, Date end, String description) throws SQLException {
		Statement statement = connection.createStatement();
		
		DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		String startStr = dateFormat.format(start);
		String endStr = dateFormat.format(end);
		
		String sql = String.format("INSERT INTO events (title, startTime, endTime, description) VALUES ('%s', '%s', '%s', '%s')", title, startStr, endStr, description);
		statement.executeUpdate(sql);
		statement.close();
	}
	
	/** Gets all the events between beginSearch and endSearch */
	public List<Event> getEvents(Date beginSearch, Date endSearch) {
		//TODO implement
		return null;
	}
	
	/** Deletes an event */
	public void deleteEvent(int id) {
		//TODO implement
	}
	
	/** Gets the roles associated with the given token 
	 * @throws SQLException */
	public Roles getRoles(String token) throws SQLException {
		Statement statement = connection.createStatement();
		String sql = String.format("SELECT roles.name, pages.name, pages.href FROM tokens "
				+ "JOIN accounts ON tokens.idAccount=accounts.idAccount "
				+ "JOIN user_roles ON accounts.idAccount=user_roles.idAccount "
				+ "JOIN roles ON user_roles.idRole=roles.idRole "
				+ "JOIN role_pages ON roles.idRole=role_pages.idRole "
				+ "JOIN pages ON role_pages.idPage=pages.idPage "
				+ "WHERE tokens.token='%s'", token);
		ResultSet results = statement.executeQuery(sql);
		
		Roles roles = new Roles();
		while (results.next()) {
			roles.addLink(results.getString(1), results.getString(2), results.getString(3));
		}
		
		statement.close();
		return roles;
	}
	
	/** Checks whether the user with the given token has the permission in question 
	 * @throws SQLException */
	public boolean hasPermission(String token, int idPermission) throws SQLException {
		Statement statement = connection.createStatement();
		String sql = String.format("SELECT tokens.token FROM tokens "
				+ "JOIN accounts ON tokens.idAccount=accounts.idAccount "
				+ "JOIN user_roles ON accounts.idAccount=user_roles.idAccount "
				+ "JOIN roles ON user_roles.idRole=roles.idRole "
				+ "JOIN role_permissions ON roles.idRole=role_permissions.idRole "
				+ "JOIN permissions ON role_permissions.idPermission=permissions.idPermission "
				+ "WHERE tokens.token='%s' AND permissions.idPermission=%d", token, idPermission);
		ResultSet results = statement.executeQuery(sql);
		boolean ret = results.next();
		statement.close();
		return ret;
	}
	
	/** Closes the connection to the database 
	 * @throws SQLException */
	@Override
	public void close() throws SQLException {
		connection.close();
	}
	
	/** Gets the current time */
	private Timestamp getNow() {
		return new Timestamp(Calendar.getInstance().getTime().getTime());
	}
}