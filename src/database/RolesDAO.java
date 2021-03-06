/**
 * 
 */
package database;

import java.sql.SQLException;

import data.Role;

/**
 * Manages Roles
 */
public class RolesDAO {
	private static final String INSERT_ROLE_SQL = "INSERT INTO user_roles (idAccount, idRole) VALUES (?, ?)";
	private static final String GET_ROLE_SQL = "SELECT idRole FROM user_roles WHERE idAccount = ? AND idRole = ?";
	private static final String REMOVE_ROLE_SQL = "DELETE FROM user_roles WHERE idAccount = ? AND idRole = ?";
	private static final String REMOVE_ALL_ROLES_SQL = "DELETE FROM user_roles WHERE idAccount = ?";
	
	private final Database database;
	
	/** Creates a new RolesDAO */
	RolesDAO(Database database) {
		this.database = database;
	}
	
	/** Assigns the role to the account with the given idAccount */
	public void assign(int idAccount, Role role) throws SQLException {
		database.execute(INSERT_ROLE_SQL, idAccount, role.id);
	}
	
	/** Checks whether the user in question has the given role */
	public boolean has(int idAccount, Role role) throws SQLException {
		return database.execute((row, t) -> true, false, GET_ROLE_SQL, idAccount, role.id);
	}
	
	/** Removes the role form the user */
	public void unassign(int idAccount, Role role) throws SQLException {
		database.execute(REMOVE_ROLE_SQL, idAccount, role.id);
	}
	
	/** Removes all the roles from the user */
	public void unassignAll(int idAccount) throws SQLException {
		database.execute(REMOVE_ALL_ROLES_SQL, idAccount);
	}
}
