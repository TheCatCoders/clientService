/**
 * 
 */
package com.thecat.databaseService.services.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.thecat.databaseService.entities.User;
import com.thecat.databaseService.entities.UserJson;

/**
 * Implement the Database Service
 *
 * @author froberge
 * @since September, 2018
 */
public class DatabaseService {

	private static DatabaseService dbService = null;
	
	private List<User> userList;
	
	/**
	 * Private constructor to prevent the creation of the DatabaseService
	 */
	private DatabaseService(){
		// Initialize the userList
		this.userList = createUserList();
	}
	
	public static DatabaseService getInstance() {
		if ( dbService == null ) {
			dbService = new DatabaseService();
		}

		return dbService;
	}


	/**
	 * Find a given user.
	 * 
	 * @param emailAdress {@link String}
	 * @param password {@link String}
	 * @return {@link User}
	 */
	public User select(String emailAdress, String password) {
		User response = selectUserFromList(emailAdress, password);
		
//		User response = selectUserFromDatabase(emailAdress, password);
		
		return response;
	}

	/**
	 * Select user from the database.
	 * 
	 * @param emailAdress {@link String}
	 * @param password {@link String}
	 * @return {@link User}
	 */
	private User selectUserFromDatabase(String emailAdress, String password) {
		User response = null;
		
		try {		
			Connection connection = getDatabaseConnection();

			if ( connection != null ) {
				String query = "select * from USERS where emailadr = ? and password = ?";
				PreparedStatement stmt = connection.prepareStatement(query);
				
				stmt.setString(1, emailAdress);
				stmt.setString(2, password);
				
				ResultSet rs = stmt.executeQuery();
				
				while (rs.next() ) {
					response = new User();
					response.setName( rs.getString( "NAME" ) );
				}
				
				rs.close();
				connection.close();
				return response;
			} else {
				System.out.println( "no connection" );
			}
		} catch (Exception e ) {
			System.out.println( e );
		}
		
		return response;
	}

	/**
	 * Select user from the list
	 * 
	 * @param emailAdress {@link String}
	 * @param password {@link String}
	 * @return {@link UserJson}
	 */
	private User selectUserFromList(String emailAdress, String password) {
		return this.userList.stream().filter( u -> ( u.getEmailAddress().equals(emailAdress)
			&& u.getPassword().equals(password) ) ).findFirst().orElse(null);
	}
	
	/**
	 * Add a new user for a system
	 * 
	 * @param user {@link UserJson}
	 * @return {@link Boolean}
	 */
	public boolean register(UserJson user) {
		
//		insertUserInList(user);

		insertUserInDatabase(user);
				
		return true;
	}

	

	/**
	 * Create a basic list of customer instead of using a database.
	 * 
	 * @return {@link ArrayList}
	 */
	private List<User> createUserList() {
	    List<User> list = new LinkedList<User>();
	    list.add(
	        new User(
	        "default",
	        User.Gender.MALE,
	        LocalDate.now().minusYears(17),
	        "default@example.com",
	        "default123"));
	    
	    return list;
	}
	
	/**
	 * Insert a new user in the list.
	 * 
	 * @param user {@link UserJson}
	 */
	private void insertUserInList(UserJson user) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		this.userList.add(
	        new User(
	        user.getUsername(),
	        User.Gender.valueOf(user.getGender().toUpperCase()),
	        LocalDate.parse(user.getAge(), formatter),
	        user.getEmailAdr(),
	        user.getPassword()));
	}
	
	/**
	 *  This method create a connection to the database.
	 *  
	 * @return {@link Connection}
	 * @throws Exception
	 */
	private Connection getDatabaseConnection() throws Exception{
		StringBuffer dbUrl = new StringBuffer( "jdbc:postgresql://" );
		dbUrl.append( System.getenv( "POSTGRESQL_SERVICE_HOST" ) );
		dbUrl.append( "/" );
		dbUrl.append( System.getenv( "POSTGRESQL_DATABASE" ) );
		
		
		String username = System.getenv( "POSTGRESQL_USER" );
		String password = System.getenv( "PGPASSWORD" );
		
		try {
				
			Connection connection = DriverManager.getConnection( dbUrl.toString(), username, password ); 
			
			if ( connection != null ) {
				return connection; 
			} else {
				return null;
			}
		} catch ( Exception e ) {
			throw e;
		}
	}

	/**
	 * Insert a new user in the database.
	 *
	 * @param user {@link UserJson}
	 */
	private void insertUserInDatabase(UserJson user) {
		try {
			Connection connection = getDatabaseConnection();

			if ( connection != null ) {
				String query = "INSERT INTO USERS " +
						"(ID, NAME, GENDER, BIRTHDATE, EMAILADR, PASSWORD, CREATE_DATE) " +
						"VALUES " +
						"(nextval('users_seq'), ?,?,to_date(?,'yyyy/mm/dd'),?,?,to_date(?,'yyyy/mm/dd'))";

				PreparedStatement stmt = connection.prepareStatement(query);

				stmt.setString(1, user.getUsername());
				stmt.setString(2, user.getGender());
				stmt.setString(3, user.getAge());
				stmt.setString(4, user.getEmailAdr());
				stmt.setString(5, user.getPassword());
				stmt.setString(6, LocalDateTime.now().toString());

				stmt.executeUpdate();
				connection.close();
			} else {
				System.out.println( "no connection" );
			}
		} catch (Exception e ) {
			System.out.println( e );
		}
	}

	/**
	 * Create the database schea needed for the coffee shop user and the product to start with.
	 * This is a work around to speed things up.
	 *
	 * @return boolean
	 */
	public boolean createDatabase() {

		boolean response = false;
		String scriptFile = "dbcripts/creationScript.sql";
		BufferedReader in = null;

		try {
			Connection connection = getDatabaseConnection();

			if (connection != null) {
				Statement stmt = connection.createStatement();
				in = new BufferedReader(new FileReader( scriptFile ) );
				String line;
				StringBuffer sb = new StringBuffer();

				//Read the script
				while ((line = in.readLine()) != null) {
					sb.append(line + "\n ");
				}

				in.close();
				System.out.println("Here is the SQL script \n " + sb.toString());
				stmt.executeUpdate(sb.toString());
				response = true;
			}
		} catch( Exception e ) {
			System.out.println("problem creating the script \n " + e.getMessage());
		}

		return response;
	}


/*	private void createDatabase() throws SQLException {

		try {
			Connection connection = getDatabaseConnection();

			if ( connection != null ) {
				Statement stmt = connection.createStatement();
				String createSequence = "CREATE SEQUENCE users_seq "
										+ "START WITH 1 "
										+ "INCREMENT BY   1";
		
				stmt.executeUpdate(createSequence);
				System.out.println( "sequence is created" );
		
		
				String createTable = "CREATE TABLE USERS " +
									"(ID INT NOT NULL, " +
									"NAME VARCHAR(100) NOT NULL, " +
									"GENDER VARCHAR(6) NOT NULL, " +
									"BIRTHDATE DATE, " +
									"EMAILADR VARCHAR(100) NOT NULL, " +
									"PASSWORD  VARCHAR(20)  NOT NULL, " +
									"CREATE_DATE DATE, " +
									"PRIMARY KEY (ID))";

				stmt.executeUpdate(createTable);
				System.out.println( "table is created" );

				connection.close();
			} else {
				System.out.println( "no connection" );
			}
		} catch (Exception e ) {
			System.out.println( e );
	}

*/
}
