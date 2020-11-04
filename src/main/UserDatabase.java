package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @Description: derby数据库
 * @author xiocui
 *
 */
public class UserDatabase {
	// ## DEFINE VARIABLES SECTION ##
	// define the driver to use
	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	// the database name
	String dbName = "USERDB";
	// define the Derby connection URL to use
	String connectionURL = "jdbc:derby:" + dbName + ";create=true";
	Connection conn;

	public UserDatabase() {

		// ## LOAD DRIVER SECTION ##
		try {
			/*
			 * * Load the Derby driver.* When the embedded Driver is used this action start
			 * the Derby engine.* Catch an error and suggest a CLASSPATH problem
			 */
			Class.forName(driver);
			System.out.println(driver + " loaded. ");
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
			System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		}
		String createString = "create table USERTABLE " + "(USERNAME varchar(20) primary key not null, "
				+ "HASHEDPWD char(32) for bit data, " + "REGISTERTIME timestamp default CURRENT_TIMESTAMP, "
				+ "SALT char(32) for bit data, " + "PHONE varchar(50))";

		try {
			DriverManager.setLogWriter(new PrintWriter(new File("aaa.txt")));
			// Create (if needed) and connect to the database
			conn = DriverManager.getConnection(connectionURL);
			// Create a statement to issue simple commands.
			Statement s = conn.createStatement();
			// Call utility method to check if table exists.
			// Create the table if needed
			if (!checkTable(conn)) {
				System.out.println(" . . . . creating table USERTABLE");
				s.execute(createString);
			}
			s.close();
			System.out.println("Database openned normally");
		} catch (SQLException e) {
			errorPrint(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Insert a new user into the USERTABLE table
	public boolean insertUser(String userName, String userPwd, String phone) {
		try {
			Security.addProvider(new BouncyCastleProvider());
			if (!userName.isEmpty() && !userPwd.isEmpty()) {
				PreparedStatement psTest = conn.prepareStatement("select * from USERTABLE where USERNAME=?",
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				psTest.setString(1, userName);
				ResultSet rs = psTest.executeQuery();
				rs.last();
				int n = rs.getRow();
				psTest.close();
				if (n == 0) {
					PreparedStatement psInsert = conn.prepareStatement("insert into USERTABLE values (?,?,?,?,?)");

					byte[] salt = new byte[32];
					new SecureRandom().nextBytes(salt);
					MessageDigest digest = MessageDigest.getInstance("SM3");
					digest.update(userPwd.getBytes());
					digest.update(salt);
					byte[] resultHash = digest.digest();

					System.out.println("注册时：         " + resultHash.toString());

					psInsert.setString(1, userName);
					psInsert.setBytes(2, resultHash);
					psInsert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					psInsert.setBytes(4, salt);
					psInsert.setString(5, phone);
					psInsert.executeUpdate();
					psInsert.close();
					return true;
				}
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("用户" + userName + "已经存在");
		return false;
	}

	public boolean deleteUser(String userName, String userPwd) {
		if (checkUserPassword(userName, userPwd) == true) {
			try {
				PreparedStatement psDelete = conn.prepareStatement("delete from USERTABLE where USERNAME=?");
				psDelete.setString(1, userName);
				int n = psDelete.executeUpdate();
				psDelete.close();
				if (n > 0) {
					System.out.println("成功删除用户" + userName);
					return true;
				} else {
					System.out.println("删除用户" + userName + "失败");
					return false;
				}
			} catch (SQLException e) {
				errorPrint(e);
			}
		}
		return false;
	}

	// check if userName with password userPwd can logon
	public boolean checkUserPassword(String userName, String userPwd) {

		try {
			Security.addProvider(new BouncyCastleProvider());
			if (!userName.isEmpty() && !userPwd.isEmpty()) {

				PreparedStatement pStatement = conn.prepareStatement("select SALT from USERTABLE where USERNAME=?");
				pStatement.setString(1, userName);
				ResultSet rSet = pStatement.executeQuery();
				rSet.next();
				byte[] salt = new byte[32];
				salt = rSet.getBytes("SALT");

				MessageDigest digest = MessageDigest.getInstance("SM3");
				digest.update(userPwd.getBytes());
				digest.update(salt);
				byte[] resultHash = digest.digest();

				PreparedStatement psTest = conn.prepareStatement(
						"select * from USERTABLE where USERNAME=? and HASHEDPWD=?", ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				psTest.setString(1, userName);
				psTest.setBytes(2, resultHash);
				ResultSet rs = psTest.executeQuery();
				rs.last();
				int n = rs.getRow();
				pStatement.close();
				psTest.close();
				return n > 0 ? true : false;

			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	// show the information of all users in table USERTABLE, should be called
	// before the program exited
	public void showAllUsers() {
		String printLine = "  ______________当前所有注册用户______________";
		try {
			Statement s = conn.createStatement();
			// Select all records in the USERTABLE table
			ResultSet users = s.executeQuery(
					"select USERNAME, HASHEDPWD, REGISTERTIME, SALT, PHONE from USERTABLE order by REGISTERTIME");

			// Loop through the ResultSet and print the data
			System.out.println(printLine);
			while (users.next()) {
				System.out.println("User-Name: " + users.getString("USERNAME") // 用户名
						+ " Hashed-Pasword: " + Base64.getEncoder().encodeToString(users.getBytes("HASHEDPWD")) // 口令HASH值的BASE64编码
						+ " Regiester-Time " + users.getTimestamp("REGISTERTIME")// 注册时间
						+ " SALT " + users.getString("SALT") + " PHONE " + Integer.parseInt(users.getString("PHONE")));
			}
			System.out.println(printLine);
			// Close the resultSet
			s.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 关闭数据库
	public void shutdownDatabase() {
		/***
		 * In embedded mode, an application should shut down Derby. Shutdown throws the
		 * XJ015 exception to confirm success.
		 ***/
		if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
			boolean gotSQLExc = false;
			try {
				conn.close();
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException se) {
				if (se.getSQLState().equals("XJ015")) {
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				System.out.println("Database did not shut down normally");
			} else {
				System.out.println("Database shut down normally");
			}
		}
	}

	/*** Check for USER table ****/
	public boolean checkTable(Connection conTst) throws SQLException {
		try {
			Statement s = conTst.createStatement();
			s.execute("update USERTABLE set USERNAME= 'TEST', REGISTERTIME = CURRENT_TIMESTAMP where 1=3");
		} catch (SQLException sqle) {
			String theError = (sqle).getSQLState();
			// System.out.println(" Utils GOT: " + theError);
			/** If table exists will get - WARNING 02000: No row was found **/
			if (theError.equals("42X05")) // Table does not exist
			{
				return false;
			} else if (theError.equals("42X14") || theError.equals("42821")) {
				System.out
						.println("checkTable: Incorrect table definition. Drop table USERTABLE and rerun this program");
				throw sqle;
			} else {
				System.out.println("checkTable: Unhandled SQLException");
				throw sqle;
			}
		}
		return true;
	}

	// Exception reporting methods with special handling of SQLExceptions
	static void errorPrint(Throwable e) {
		if (e instanceof SQLException) {
			SQLExceptionPrint((SQLException) e);
		} else {
			System.out.println("A non SQL error occured.");
			e.printStackTrace();
		}
	}

	// Iterates through a stack of SQLExceptions
	static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	}
}
