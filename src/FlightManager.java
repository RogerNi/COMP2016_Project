import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * This is a flight manager to support: (1) add a flight (2) delete a flight (by
 * flight_no) (3) print flight information (by flight_no) (4) select a flight
 * (by source, dest, stop_no = 0) (5) select a flight (by source, dest, stop_no
 * = 1)
 * 
 * @author comp1160/2016
 */

public class FlightManager {

	Scanner in = null;
	Connection conn = null;
	// Database Host
	final String databaseHost = "orasrv1.comp.hkbu.edu.hk";
	// Database Port
	final int databasePort = 1521;
	// Database name
	final String database = "pdborcl.orasrv1.comp.hkbu.edu.hk";
	final String proxyHost = "faith.comp.hkbu.edu.hk";
	final int proxyPort = 22;
	final String forwardHost = "localhost";
	int forwardPort;
	Session proxySession = null;
	boolean noException = true;

	// JDBC connecting host
	String jdbcHost;
	// JDBC connecting port
	int jdbcPort;

	String[] options = { // if you want to add an option, append to the end of
							// this array
			"GUI", "add a flight", "print flight information (by flight_number)", "delete a flight (by flight_number)",
			"select a flight (by DEPARTURE_CITY, DESTINATION_CITY )", "Booking Flights (by Customer_ID, flight_number)",
			"Deleting Flights (by Customer_ID , Booking_ID)", "exit" };

	/**
	 * Get YES or NO. Do not change this function.
	 * 
	 * @return boolean
	 */
	boolean getYESorNO(String message) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(message));
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog dialog = pane.createDialog(null, "Question");
		dialog.setVisible(true);
		boolean result = JOptionPane.YES_OPTION == (int) pane.getValue();
		dialog.dispose();
		return result;
	}

	/**
	 * Get username & password. Do not change this function.
	 * 
	 * @return username & password
	 */
	String[] getUsernamePassword(String title) {
		JPanel panel = new JPanel();
		final TextField usernameField = new TextField();
		final JPasswordField passwordField = new JPasswordField();
		panel.setLayout(new GridLayout(2, 2));
		panel.add(new JLabel("Username"));
		panel.add(usernameField);
		panel.add(new JLabel("Password"));
		panel.add(passwordField);
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				usernameField.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
		dialog.dispose();
		return new String[] { usernameField.getText(), new String(passwordField.getPassword()) };
	}

	/**
	 * Login the proxy. Do not change this function.
	 * 
	 * @return boolean
	 */
	public boolean loginProxy() {
		if (getYESorNO("Using ssh tunnel or not?")) { // if using ssh tunnel
			String[] namePwd = getUsernamePassword("Login cs lab computer");
			String sshUser = namePwd[0];
			String sshPwd = namePwd[1];
			try {
				proxySession = new JSch().getSession(sshUser, proxyHost, proxyPort);
				proxySession.setPassword(sshPwd);
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				proxySession.setConfig(config);
				proxySession.connect();
				proxySession.setPortForwardingL(forwardHost, 0, databaseHost, databasePort);
				forwardPort = Integer.parseInt(proxySession.getPortForwardingL()[0].split(":")[0]);
			} catch (JSchException e) {
				e.printStackTrace();
				return false;
			}
			jdbcHost = forwardHost;
			jdbcPort = forwardPort;
		} else {
			jdbcHost = databaseHost;
			jdbcPort = databasePort;
		}
		return true;
	}

	/**
	 * Login the oracle system. Do not change this function.
	 * 
	 * @return boolean
	 */
	public boolean loginDB() {
		String[] namePwd = getUsernamePassword("Login sqlplus");
		String username = namePwd[0];
		String password = namePwd[1];
		String URL = "jdbc:oracle:thin:@" + jdbcHost + ":" + jdbcPort + "/" + database;

		try {
			System.out.println("Logging " + URL + " ...");
			conn = DriverManager.getConnection(URL, username, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Show the options. If you want to add one more option, put into the options
	 * array above.
	 */
	public void showOptions() {
		System.out.println("Please choose following option:");
		for (int i = 0; i < options.length; ++i) {
			System.out.println("(" + (i + 1) + ") " + options[i]);
		}
	}

	/**
	 * Run the manager
	 */
	public void run() {
		while (noException) {
			showOptions();
			String line = in.nextLine();
			if (line.equalsIgnoreCase("exit"))
				return;
			int choice = -1;
			try {
				choice = Integer.parseInt(line);
			} catch (Exception e) {
				System.out.println("This option is not available");
				continue;
			}
			if (!(choice >= 1 && choice <= options.length)) {
				System.out.println("This option is not available");
				continue;
			}
			if (options[choice - 1].equals("add a flight")) {
				addFlight();
			} else if (options[choice - 1].equals("delete a flight (by flight_number)")) {
				deleteFlight();
			} else if (options[choice - 1].equals("print flight information (by flight_number)")) {
				printFlightByNo();
			} else if (options[choice - 1].equals("select a flight (by DEPARTURE_CITY, DESTINATION_CITY )")) {
				selectFlights();
			} else if (options[choice - 1].equals("Booking Flights (by Customer_ID, flight_number)")) {
				bookFlights();
			} else if (options[choice - 1].equals("Deleting Flights (by Customer_ID , Booking_ID)")) {
				delBook();
			} else if (options[choice - 1].equalsIgnoreCase("gui")) {
				this.JFrameCreate();
			} else if (options[choice - 1].equals("exit")) {
				break;
			}
		}
	}

	private void bookFlights() {
		System.out.println("Please input the customer id:");
		String cust = in.nextLine();
		if (cust.equalsIgnoreCase("exit"))
			return;
		System.out.println("Please input all flights_numbers (splited by , ):");
		String line = in.nextLine();
		if (line.equalsIgnoreCase("exit"))
			return;
		String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();
		try {
			Statement stm = conn.createStatement();
			String sql = "";// to be filled later!
			stm.executeUpdate(sql);
			stm.close();
			System.out.println("Booking Successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void delBook() {
		System.out.println("Please input customer id:");
		String cust = in.nextLine();
		if (cust.equalsIgnoreCase("exit"))
			return;
		System.out.println("Please input booking id:");
		String line = in.nextLine();
		if (line.equalsIgnoreCase("exit"))
			return;
		// String[] values = line.split(",");
		// for (int i = 0; i < values.length; ++i)
		// values[i] = values[i].trim();
		try {
			Statement stm = conn.createStatement();
			String sql = "DELETE FROM BOOKING WHERE CUSTOMER_ID = '" + cust + "' AND ID = '" + line + "'";// to be
																											// filled
																											// later!
			stm.executeUpdate(sql);
			stm.close();
			System.out.println("Booking Successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean delBookGUI(String[] s) {
		try {
			Statement stm = conn.createStatement();
			String sql = "DELETE FROM BOOKING WHERE CUSTOMER_ID = '" + s[0] + "' AND ID = '" + s[1] + "'";// to be
																											// filled
																											// later!
			stm.executeUpdate(sql);
			stm.close();
			return true;
			// System.out.println("Booking Successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Print out the infomation of a flight given a flight_no
	 * 
	 * @param flight_no
	 */
	private void printFlightInfo(String flight_no) {
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT * FROM FLIGHT WHERE FLIGHT_NUMBER = '" + flight_no + "'";
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return;
			String[] heads = { "Flight_number", "Departure_City", "Destination_City", "Departure_Time", "Arrival_Time",
					"Fare", "Seats_Limit" };
			for (int i = 0; i < 7; ++i) { // flight table 6 attributes
				try {
					System.out.println(heads[i] + " : " + rs.getString(i + 1)); // attribute
																				// id
																				// starts
																				// with
																				// 1
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}
	}

	private void printFlightInfoGUI(String flight_no, JTextArea t) {
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT * FROM FLIGHT WHERE FLIGHT_NUMBER = '" + flight_no + "'";
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return;
			String[] heads = { "Flight_number", "Departure_City", "Destination_City", "Departure_Time", "Arrival_Time",
					"Fare", "Seats_Limit" };
			for (int i = 0; i < 7; ++i) { // flight table 6 attributes
				try {
					t.append(heads[i] + " : " + rs.getString(i + 1) + "\n"); // attribute
																				// id
																				// starts
																				// with
																				// 1
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}
	}

	/**
	 * List all flights in the database.
	 */
	private void listAllFlights() {
		System.out.println("All flights in the database now:");
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT Flight_number FROM FLIGHT";
			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0;
			while (rs.next()) {
				System.out.println(rs.getString(1));
				++resultCount;
			}
			System.out.println("Total " + resultCount + " flight(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
	}

	private void listAllFlightGUI(DefaultListModel<String> model) {
		// System.out.println("All flights in the database now:");
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT Flight_number FROM FLIGHT";
			ResultSet rs = stm.executeQuery(sql);

			// int resultCount = 0;
			while (rs.next()) {
				model.addElement(rs.getString(1));
				// ++resultCount;
			}
			// System.out.println("Total " + resultCount + " flight(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
	}

	/**
	 * Select out a flight according to the flight_no.
	 */
	private void printFlightByNo() {
		listAllFlights();
		System.out.println("Please input the flight_no to print info:");
		String line = in.nextLine();
		line = line.trim();
		if (line.equalsIgnoreCase("exit"))
			return;

		printFlightInfo(line);
	}

	/**
	 * Combine selectFlightInZeroStop() and selectFlightInOneStop()
	 */

	private void selectFlights() {
		System.out.println("Please input source:");
		String source = in.nextLine();
		if (source.equalsIgnoreCase("exit"))
			return;

		System.out.println("Please input destination:");
		String dest = in.nextLine();
		if (dest.equalsIgnoreCase("exit"))
			return;

		System.out.println(
				"Please input how many stops:\n(Range can be indicated by %least_stop%-%most_stop%\nFor example: 0-1 stop can be represented by 0-1)");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;

		int[] intValues;
		try {
			String[] stringValues = line.split("-");
			intValues = new int[stringValues.length];
			for (int i = 0; i < stringValues.length; i++) {
				intValues[i] = Integer.valueOf(stringValues[i]);
			}
		} catch (Exception e) {
			System.out.println("Invalid Input!");
			selectFlights();
			return;
		}
		if (intValues.length == 2 && intValues[1] - intValues[0] == 2)
			intValues = new int[] { 0, 1, 2 };
		int totalCount = 0;
		for (int i : intValues) {
			if (i == 0) {
				totalCount += selectFlightsInZeroStop(new String[] { source, dest });
			}
			if (i == 1) {
				totalCount += selectFlightsInOneStop(new String[] { source, dest });
			}
			if (i == 2) {
				totalCount += selectFlightsInTwoStop(new String[] { source, dest });
			}
		}
		System.out.println("Total " + totalCount + " choice(s).");
	}

	/**
	 * Given source and dest, select all the flights can arrive the dest directly.
	 * For example, given HK, Tokyo, you may find HK -> Tokyo Your job to fill in
	 * this function.
	 */
	private int selectFlightsInZeroStop(String[] values) {
		// System.out.println("Please input source, dest:");

		// String line = in.nextLine();

		// if (line.equalsIgnoreCase("exit"))
		// return;

		// String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		try {
			/**
			 * Create the statement and sql
			 */
			Statement stm = conn.createStatement();

			String sql = "SELECT FLIGHT_NUMBER FROM FLIGHT " + "WHERE DEPARTURE_CITY = '" + values[0]
					+ "' AND DESTINATION_CITY = '" + values[1] + "'";

			/**
			 * Formulate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */
			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0; // a counter to count the number of result
									// records
			while (rs.next()) { // this is the result record iterator, see the
								// tutorial for details

				/*
				 * Write your own to print flight information; you may use the printFlightInfo()
				 * function
				 */
				resultCount++;
				printFlightInfo(rs.getString(1));
				System.out.println("=================================================");

			}
			// System.out.println("Total " + resultCount + " choice(s).");
			rs.close();
			stm.close();
			return resultCount;
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
			return 0;
		}
	}

	/**
	 * Given source and dest, select all the flights can arrive the dest in one
	 * stop. For example, given HK, Tokyo, you may find HK -> Beijing, Beijing ->
	 * Tokyo Your job to fill in this function.
	 */
	private int selectFlightsInOneStop(String[] values) {
		// System.out.println("Please input source, dest:");

		// String line = in.nextLine();

		// if (line.equalsIgnoreCase("exit"))
		// return;

		// String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		try {
			/**
			 * Create the statement and sql
			 */
			Statement stm = conn.createStatement();

			String sql = "SELECT F1.FLIGHT_NUMBER, F2.FLIGHT_NUMBER FROM FLIGHT F1, FLIGHT F2 WHERE F1.DESTINATION_CITY = F2.DEPARTURE_CITY AND F1.Arrival_Time < F2.Departure_Time AND F1.DEPARTURE_CITY = '"
					+ values[0] + "' AND F2.DESTINATION_CITY = '" + values[1] + "'";

			/**
			 * Formulate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */
			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0; // a counter to count the number of result
									// records
			while (rs.next()) { // this is the result record iterator, see the
								// tutorial for details

				/*
				 * Write your own to print flight information; you may use the printFlightInfo()
				 * function
				 */
				resultCount++;
				printFlightInfo(rs.getString(1));
				System.out.println("-------------------------------------------------");
				printFlightInfo(rs.getString(2));
				System.out.println("=================================================");

			}
			// System.out.println("Total " + resultCount + " choice(s).");
			rs.close();
			stm.close();
			return resultCount;
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
			return 0;
		}

		/**
		 * try {
		 * 
		 * // Similar to the 'selectFlightsInZeroStop' function; write your own code
		 * here
		 * 
		 * 
		 * } catch (SQLException e) { e.printStackTrace(); noException = false; }
		 */
	}

	private int selectFlightsInTwoStop(String[] values) {
		// System.out.println("Please input source, dest:");

		// String line = in.nextLine();

		// if (line.equalsIgnoreCase("exit"))
		// return;

		// String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		try {
			/**
			 * Create the statement and sql
			 */
			Statement stm = conn.createStatement();

			String sql = "SELECT F1.FLIGHT_NUMBER, F2.FLIGHT_NUMBER, F3.FLIGHT_NUMBER FROM FLIGHT F1, FLIGHT F2, FLIGHT F3 WHERE F1.DESTINATION_CITY = F2.DEPARTURE_CITY AND F2.DESTINATION_CITY = F3.DEPARTURE_CITY AND F1.Arrival_Time < F2.Departure_Time AND F2.Arrival_Time < F3.DEPARTURE_TIME AND F1.DEPARTURE_CITY = '"
					+ values[0] + "' AND F3.DESTINATION_CITY = '" + values[1] + "'";

			/**
			 * Formulate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */
			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0; // a counter to count the number of result
									// records
			while (rs.next()) { // this is the result record iterator, see the
								// tutorial for details

				/*
				 * Write your own to print flight information; you may use the printFlightInfo()
				 * function
				 */
				resultCount++;
				printFlightInfo(rs.getString(1));
				System.out.println("-------------------------------------------------");
				printFlightInfo(rs.getString(2));
				System.out.println("-------------------------------------------------");
				printFlightInfo(rs.getString(3));
				System.out.println("=================================================");

			}
			// System.out.println("Total " + resultCount + " choice(s).");
			rs.close();
			stm.close();
			return resultCount;
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
			return 0;
		}

		/**
		 * try {
		 * 
		 * // Similar to the 'selectFlightsInZeroStop' function; write your own code
		 * here
		 * 
		 * 
		 * } catch (SQLException e) { e.printStackTrace(); noException = false; }
		 */
	}

	/**
	 * Insert data into database
	 * 
	 * @return
	 */
	private void addFlight() {
		/**
		 * A sample input is: CX109, 2015/03/15/13:00:00, 2015/03/15/19:00:00, 2000,
		 * Beijing, Tokyo
		 */
		System.out.println(
				"Please input the flight_number, DEPARTURE_CITY, DESTINATION_CITY, departure_time, arrival_time, fare, seats_limit :");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;
		String[] values = line.split(",");

		if (values.length < 6) {
			System.out.println("The value number is expected to be 6");
			return;
		}
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		try {
			Statement stm = conn.createStatement();
			String sql = "INSERT INTO FLIGHT VALUES(" + "'" + values[0] + "', " + // this
																					// is
																					// flight
																					// no
					"'" + values[1] + "', " + // this is source
					"'" + values[2] + "'," + // this is dest
					"to_date('" + values[3] + "', 'yyyy/mm/dd/hh24:mi:ss'), " + // this
																				// is
																				// depart_time
					"to_date('" + values[4] + "', 'yyyy/mm/dd/hh24:mi:ss'), " + // this
																				// is
																				// arrive_time
					values[5] + ", " + // this is fare
					values[6] + ")";
			stm.executeUpdate(sql);
			stm.close();
			System.out.println("succeed to add flight ");
			printFlightInfo(values[0]);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to add a flight " + line);
			noException = false;
		}
	}

	private boolean addFlightGUI(String[] values) {
		try {
			Statement stm = conn.createStatement();
			String sql = "INSERT INTO FLIGHT VALUES(" + "'" + values[0] + "', " + // this
																					// is
																					// flight
																					// no
					"'" + values[1] + "', " + // this is source
					"'" + values[2] + "'," + // this is dest
					"to_date('" + values[3] + "', 'yyyy/mm/dd/hh24:mi:ss'), " + // this
																				// is
																				// depart_time
					"to_date('" + values[4] + "', 'yyyy/mm/dd/hh24:mi:ss'), " + // this
																				// is
																				// arrive_time
					values[5] + ", " + // this is fare
					values[6] + ")";
			stm.executeUpdate(sql);
			stm.close();
			// System.out.println("succeed to add flight ");
			return true;
			// printFlightInfo(values[0]);
		} catch (SQLException e) {
			e.printStackTrace();
			// System.out.println("fail to add a flight " + line);

			noException = false;
			return false;
		}
	}

	/**
	 * Please fill in this function to delete a flight.
	 */
	public void deleteFlight() {
		listAllFlights();
		System.out.println("Please input the flight_no to delete:");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;
		line = line.trim();

		try {
			Statement stm = conn.createStatement();

			String sql = "DELETE FROM FLIGHT WHERE FLIGHT_NUMBER = '" + line + "'";

			/*
			 * Formuate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */

			stm.executeUpdate(sql); // please pay attention that we use
									// executeUpdate to update the database

			stm.close();

			/*
			 * You may uncomment the statement below after formulating the SQL query above
			 */
			System.out.println("succeed to delete flight " + line);
			/*
			*/
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to delete flight " + line);
			noException = false;
		}
	}

	private boolean deleteFlightGUI(String s) {
		try {
			Statement stm = conn.createStatement();

			String sql = "DELETE FROM FLIGHT WHERE FLIGHT_NUMBER = '" + s + "'";

			/*
			 * Formuate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */

			stm.executeUpdate(sql); // please pay attention that we use
									// executeUpdate to update the database

			stm.close();
			return true;

			/*
			 * You may uncomment the statement below after formulating the SQL query above
			 */
			// System.out.println("succeed to delete flight " + line);
			/*
			*/
		} catch (SQLException e) {
			e.printStackTrace();
			// System.out.println("fail to delete flight " + line);
			noException = false;
			return false;
		}
	}

	/**
	 * Close the manager. Do not change this function.
	 */
	public void close() {
		System.out.println("Thanks for using this manager! Bye...");
		try {
			if (conn != null)
				conn.close();
			if (proxySession != null) {
				proxySession.disconnect();
			}
			in.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor of flight manager Do not change this function.
	 */
	public FlightManager() {
		System.out.println("Welcome to use this manager!");
		in = new Scanner(System.in);
	}

	/**
	 * Main function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FlightManager manager = new FlightManager();
		// manager.JFrameCreate();
		// manager.viewFlightGUI();
		if (!manager.loginProxy()) {
			System.out.println("Login proxy failed, please re-examine your username and password!");
			return;
		}
		if (!manager.loginDB()) {
			System.out.println("Login database failed, please re-examine your username and password!");
			return;
		}
		System.out.println("Login succeed!");
		try {
			manager.run();
		} finally {
			manager.close();
		}
	}

	private void JFrameCreate() {
		JFrame f = new JFrame("Flight Manager") {
		};
		f.setSize(400, 500);
		// f.setVisible(true);
		// f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JPanel mainP = new JPanel();
		mainP.setBorder(new EmptyBorder(5, 5, 5, 5));
		f.setContentPane(mainP);
		mainP.setLayout(new FlowLayout());
		// JPanel upP = new JPanel();
		JButton show = new JButton("Show All Flights Information");
		show.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// f.hide();
				// printFlightByNo();
				// f.show();
				viewFlightGUI();
			}

		});
		mainP.add(show);
		JLabel label1 = new JLabel("              Choose an Operation:  ");
		mainP.add(label1);
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("------");
		comboBox.addItem("Search for a flight");
		comboBox.addItem("Add a flight");
		comboBox.addItem("Delete a flight");
		comboBox.addItem("Book flights");
		comboBox.addItem("Cancel booking");
		mainP.add(comboBox);
		// mainP.add(upP);
		f.setVisible(true);
		JPanel[] subPanels = new JPanel[5];
		for (int i = 0; i < subPanels.length; i++) {
			subPanels[i] = new JPanel();
			// subPanels[i].setBorder(new EmptyBorder(5, 5, 5, 5));
		}

		// Set subPanels[0]
		subPanels[0].setLayout(new GridLayout(5, 2));
		JLabel depart_P_0 = new JLabel("Departure City:");
		JTextField depart_in_P_0 = new JTextField();
		JLabel dest_P_0 = new JLabel("Destination City:");
		JTextField dest_in_P_0 = new JTextField();
		JLabel maxTrans = new JLabel("Maximum Transitions:");
		JTextField maxTrans_in = new JTextField();
		JLabel maxHour = new JLabel("Maximun Hours:");
		JTextField maxHour_in = new JTextField();
		subPanels[0].add(depart_P_0);
		subPanels[0].add(depart_in_P_0);
		subPanels[0].add(dest_P_0);
		subPanels[0].add(dest_in_P_0);
		subPanels[0].add(maxTrans);
		subPanels[0].add(maxTrans_in);
		subPanels[0].add(maxHour);
		subPanels[0].add(maxHour_in);
		JButton b_search = new JButton("Search");
		b_search.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				// to be filled

			}

		});
		subPanels[0].add(b_search);
		JButton b_reset = new JButton("Reset");
		b_reset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				depart_in_P_0.setText("");
				dest_in_P_0.setText("");
				maxTrans_in.setText("");
				maxHour_in.setText("");
			}

		});
		subPanels[0].add(b_reset);
		mainP.add(subPanels[0]);
		// System.out.println("sub 0 Added");

		// set subPanels[1]
		subPanels[1].setLayout(new GridLayout(9, 2));
		JLabel f_no_1 = new JLabel("Flight Number:");
		JTextField f_no_in_1 = new JTextField();
		JLabel dp_city_1 = new JLabel("Departure City:");
		JTextField dp_city_in_1 = new JTextField();
		JLabel ds_city_1 = new JLabel("Destination City:");
		JTextField ds_city_in_1 = new JTextField();
		JLabel dp_time_1 = new JLabel("Departure Time:");
		JTextField dp_time_in_1 = new JTextField();
		JLabel arr_time_1 = new JLabel("Arrival Time:");
		JTextField arr_time_in_1 = new JTextField();
		JLabel fare_1 = new JLabel("Fare:");
		JTextField fare_in_1 = new JTextField();
		JLabel s_l_1 = new JLabel("Seat Limit:");
		JTextField s_l_in_1 = new JTextField();
		JButton add_1 = new JButton("Add");
		add_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// Add
				if (addFlightGUI(new String[] { f_no_in_1.getText(), dp_city_in_1.getText(), ds_city_in_1.getText(),
						dp_time_in_1.getText(), arr_time_in_1.getText(), fare_in_1.getText(), s_l_in_1.getText() })) {
					JOptionPane.showMessageDialog(null, "Successful!");
					f_no_in_1.setText("");
					dp_city_in_1.setText("");
					ds_city_in_1.setText("");
					dp_time_in_1.setText("");
					arr_time_in_1.setText("");
					fare_in_1.setText("");
					s_l_in_1.setText("");
				} else
					JOptionPane.showMessageDialog(null, "Fail!");
			}

		});
		JButton reset_1 = new JButton("Reset");
		reset_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				f_no_in_1.setText("");
				dp_city_in_1.setText("");
				ds_city_in_1.setText("");
				dp_time_in_1.setText("");
				arr_time_in_1.setText("");
				fare_in_1.setText("");
				s_l_in_1.setText("");
			}

		});
		subPanels[1].add(f_no_1);
		subPanels[1].add(f_no_in_1);
		subPanels[1].add(dp_city_1);
		subPanels[1].add(dp_city_in_1);
		subPanels[1].add(ds_city_1);
		subPanels[1].add(ds_city_in_1);
		subPanels[1].add(dp_time_1);
		subPanels[1].add(dp_time_in_1);
		subPanels[1].add(arr_time_1);
		subPanels[1].add(arr_time_in_1);
		subPanels[1].add(fare_1);
		subPanels[1].add(fare_in_1);
		subPanels[1].add(s_l_1);
		subPanels[1].add(s_l_in_1);
		subPanels[1].add(add_1);
		subPanels[1].add(reset_1);
		// subPanels[1] set

		// set subPanels[2]
		subPanels[2].setLayout(new GridLayout(2, 2));
		JLabel del = new JLabel("Flight Number:");
		JTextField del_in = new JTextField();
		subPanels[2].add(del);
		subPanels[2].add(del_in);
		JButton del_b = new JButton("Delete");
		del_b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// del
				if (deleteFlightGUI(del_in.getText())) {
					JOptionPane.showMessageDialog(null, "Successful!");
					del_in.setText("");
				} else
					JOptionPane.showMessageDialog(null, "Fail!");
			}

		});
		subPanels[2].add(del_b);
		JButton reset_2 = new JButton("Reset");
		reset_2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				del_in.setText("");
			}

		});
		subPanels[2].add(reset_2);
		// subPanels[2] set

		// set subPanels[3]
		subPanels[3].setLayout(new GridLayout(4, 2));
		JLabel cust_3 = new JLabel("Customer ID:");
		JTextField cust_in_3 = new JTextField();
		JLabel flights_3 = new JLabel("Flights ID:");
		JTextField flights_in_3 = new JTextField();
		subPanels[3].add(cust_3);
		subPanels[3].add(cust_in_3);
		subPanels[3].add(flights_3);
		subPanels[3].add(flights_in_3);
		JButton book = new JButton("Book");
		book.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// book
			}

		});
		subPanels[3].add(book);
		// subPanels[3] set

		// set subPanels[4]
		subPanels[4].setLayout(new GridLayout(3, 2));
		JLabel cust_4 = new JLabel("Customer ID:");
		JTextField cust_in_4 = new JTextField();
		JLabel b_id = new JLabel("Booking ID:");
		JTextField b_id_in = new JTextField();
		subPanels[4].add(cust_4);
		subPanels[4].add(cust_in_4);
		subPanels[4].add(b_id);
		subPanels[4].add(b_id_in);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// cancel
				if (delBookGUI(new String[] { cust_in_4.getText(), b_id_in.getText() })) {
					JOptionPane.showMessageDialog(null, "Successful!");
					cust_in_4.setText("");
					b_id_in.setText("");
				} else
					JOptionPane.showMessageDialog(null, "Fail!");
			}

		});
		subPanels[4].add(cancel);
		// subPanels[4] set
		comboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				int number = 0;
				switch ((String) arg0.getItem()) {
				case "Search for a flight":
					number = 0;
					break;
				case "Add a flight":
					number = 1;
					break;
				case "Delete a flight":
					number = 2;
					break;
				case "Book flights":
					number = 3;
					break;
				case "Cancel booking":
					number = 4;
					break;
				}
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					mainP.add(subPanels[number]);
					mainP.updateUI();
				} else {
					mainP.remove(subPanels[number]);
					System.out.println("SubPanel " + number + " removed");
				}
			}

		});
	}

	private void viewFlightGUI() {
		JFrame f = new JFrame("View Flights");
		JPanel p = new JPanel();
		DefaultListModel<String> model = new DefaultListModel<String>();
		JList<String> l = new JList<String>(model);
		f.setSize(400, 600);
		p.setLayout(new FlowLayout());
		p.setBorder(new EmptyBorder(5, 5, 5, 5));
		l.setSize(300, 300);
		l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		f.setContentPane(p);
		p.add(l);
		f.setVisible(true);
		// model.addElement("OK");
		// model.addElement("This is a long sentence!");
		listAllFlightGUI(model);
		JTextArea t = new JTextArea();
		p.add(t);
		l.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					// TODO Auto-generated method stub
					t.setText("");
					System.out.println("Changed");
					printFlightInfoGUI(l.getSelectedValue(), t);

				}
			}

		});
	}
}
