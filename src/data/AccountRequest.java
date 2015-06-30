/**
 * 
 */
package data;

/**
 * Stores information about an account request
 */
public class AccountRequest {
	private final int idRequest;
	private final String netid;
	private final String firstName;
	private final String lastName;
	private final int idTodo;
	
	/** Creates an Account */
	public AccountRequest(int idRequest, String netid, String firstName, String lastName, int idTodo) {
		this.idRequest = idRequest;
		this.netid = netid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.idTodo = idTodo;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return idRequest;
	}

	/**
	 * @return the netid
	 */
	public String getNetid() {
		return netid;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}
	
	/**
	 * @return the id of the todo
	 */
	public int getIdTodo() {
		return idTodo;
	}
}
